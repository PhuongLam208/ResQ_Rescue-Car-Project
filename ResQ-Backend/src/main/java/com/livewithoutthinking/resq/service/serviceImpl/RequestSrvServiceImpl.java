package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.BillBreakdown;
import com.livewithoutthinking.resq.dto.BillResponse;
import com.livewithoutthinking.resq.dto.CreateRescueRequestDTO;
import com.livewithoutthinking.resq.dto.UpdateBillDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.RequestSrvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequestSrvServiceImpl implements RequestSrvService {
    @Autowired
    private RequestServiceRepository requestServiceRepo;
    @Autowired
    private ServiceRepository serviceRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDiscountRepository userDiscountRepository;
    @Autowired
    private RequestRescueRepository requestRescueRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    public List<RequestService> createRequestServices(List<Integer> services, RequestRescue request) {
        List<RequestService> result = new ArrayList<>();
        for (Integer serviceId : services) {
            Services service = serviceRepo.findById(serviceId).orElse(null);
            if (service != null) {
                RequestService requestService = new RequestService();
                requestService.setRequest(request);
                requestService.setService(service);
                result.add(requestServiceRepo.save(requestService));
            }
        }
        return result;
    }
    public BillResponse getRequestRescueWithBill(Integer rrid) {
        RequestRescue rr = requestRescueRepository.findWithBillById(rrid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy RequestRescue với rrid: " + rrid));

        Bill bill = rr.getBill();
        if (bill == null) {
            throw new RuntimeException("Không tìm thấy hóa đơn cho yêu cầu cứu hộ rrid: " + rrid);
        }

        // Tạo breakdown
        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setServicePrice(bill.getServicePrice());
        breakdown.setDistancePrice(bill.getDistancePrice());
        breakdown.setAppFee(bill.getAppFee());
        breakdown.setDiscount(bill.getDiscountAmount());

        // Tạo response
        BillResponse response = new BillResponse();
        response.setRrid(rr.getRrid());
        response.setBillId(bill.getBillId());
        response.setTotal(bill.getTotal());
        response.setCurrency(bill.getCurrency());
        response.setMethod(bill.getMethod());
        response.setStartAddress(rr.getStartAddress());
        response.setEndAddress(rr.getEndAddress());
        response.setBreakdown(breakdown);

        return response;
    }

    public List<RequestService> getReqSrvByResquest(int rrId){
        return requestServiceRepo.getReqSrvByResquest(rrId);
    }

    @Override
    public BillResponse createRequestAndBill(CreateRescueRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId().intValue())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Services> serviceList = serviceRepo.findByServiceType(dto.getRescueType());
        if (serviceList.isEmpty()) {
            throw new RuntimeException("No services found for type: " + dto.getRescueType());
        }
        Services service = serviceList.get(0);

// 2. Tính giá dựa trên khoảng cách
        double distanceKm = dto.getDistanceKm();
        double basePrice = service.getFixedPrice();
        double pricePerKm = service.getPricePerKm();
        double freeKm = 5.0;

        double initialTotal;
        double distancePrice = 0;

        if (distanceKm <= freeKm) {
            initialTotal = basePrice;
        } else {
            distancePrice = (distanceKm - freeKm) * pricePerKm;
            initialTotal = basePrice + distancePrice;
        }
        // Áp dụng mã giảm giá nếu có
        Discount discount = null;
        double discountAmount = 0;
        if (dto.getPromoCode() != null && !dto.getPromoCode().isEmpty()) {
            Optional<UserDiscount> userDiscounts = userDiscountRepository
                    .findAvailableDiscountByUserIdAndCode(dto.getUserId(), dto.getPromoCode());

            UserDiscount userDiscount = userDiscounts.stream()
                    .filter(ud -> !ud.isUsed() && ud.getDiscount().getQuantity() > 0)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid or used discount code"));

            discount = userDiscount.getDiscount();

            if ("Percent".equalsIgnoreCase(discount.getType())) {
                discountAmount = initialTotal * discount.getAmount().doubleValue() / 100.0;
            } else if ("Money".equalsIgnoreCase(discount.getType())) {
                discountAmount = discount.getAmount().doubleValue();
            }

            // Cập nhật mã giảm giá
            discount.setQuantity(discount.getQuantity() - 1);
            userDiscount.setUsed(true);
            userDiscountRepository.save(userDiscount);
        }

        double finalTotal = initialTotal - discountAmount;
        if (finalTotal < 0) finalTotal = 0;

        // Tạo request cứu hộ
        RequestRescue request = new RequestRescue();
        request.setUser(user);
        request.setRescueType(dto.getRescueType());
        request.setStatus("PENDING");
        request.setUserLatitude(dto.getStartLat());
        request.setUserLongitude(dto.getStartLng());
        request.setULocation(dto.getStartLat() + "," + dto.getStartLng());
        request.setStartAddress(dto.getStartAddress()); // <-- THÊM DÒNG NÀY
        request.setStartTime(new Date()); // Cân nhắc dùng LocalDateTime.now() thay cho new Date()
        request.setCreatedAt(new Date()); // Sử dụng LocalDateTime
        request.setUpdatedAt(new Date()); // Sử dụng LocalDateTime
        request.setDiscount(discount);

        // Chỉ set đích đến nếu là ResTow hoặc ResDrive
        if ("ResTow".equalsIgnoreCase(dto.getRescueType()) || "ResDrive".equalsIgnoreCase(dto.getRescueType())) {
            if (dto.getEndLat() == null || dto.getEndLng() == null) {
                throw new RuntimeException("End location is required for " + dto.getRescueType());
            }
            request.setDestination(dto.getEndLat() + "," + dto.getEndLng());
            request.setDestLatitude(dto.getEndLat());
            request.setDestLongitude(dto.getEndLng());
            request.setEndAddress(dto.getEndAddress()); // <-- THÊM DÒNG NÀY
        } else {
            // Nếu không có đích đến, đảm bảo các trường liên quan đến đích đến được set null hoặc giá trị mặc định
            request.setDestination("No have");
            request.setDestLatitude(0.0);
            request.setDestLongitude(0.0);
            request.setEndAddress("No have");
        }


        requestRescueRepository.save(request);

        // Chi tiết giá (BillBreakdown)
        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setServicePrice(basePrice); // Đảm bảo kiểu dữ liệu khớp (double)
        breakdown.setDistancePrice(distanceKm * pricePerKm); // Đảm bảo kiểu dữ liệu khớp (double)
        breakdown.setAppFee(finalTotal * 0.10);
        breakdown.setDiscount(discountAmount); // Đảm bảo kiểu dữ liệu khớp (double)

        // Tạo hóa đơn
        Bill bill = new Bill();
        bill.setRequestRescue(request);
        bill.setDiscountAmount(discountAmount);
        bill.setTotal(finalTotal);
        bill.setAppFee(finalTotal * 0.10);
        bill.setStatus("PENDING");
        bill.setMethod(dto.getPaymentMethod());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        billRepository.save(bill);

        // Trả về response
        BillResponse response = new BillResponse();
        response.setRrid(request.getRrid());
        response.setBillId(bill.getBillId());
        response.setTotal(finalTotal); // Đảm bảo kiểu dữ liệu khớp (double)
        response.setCurrency("VND");
        response.setMethod(dto.getPaymentMethod());
        response.setStartAddress(request.getStartAddress());
        response.setEndAddress(request.getEndAddress());

        response.setBreakdown(breakdown);

        return response;
    }

    @Override
    public BillResponse updateDiscountAndPayment(UpdateBillDto dto) {
        Bill bill = billRepository.findById(dto.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        RequestRescue request = bill.getRequestRescue();
        Discount discount = null;
        double discountAmount = 0;
        double clientTotal = dto.getTotalPrice();

        if (dto.getDiscountId() != null) {
            discount = discountRepository.findById(dto.getDiscountId())
                    .orElseThrow(() -> new RuntimeException("Discount not found"));

            // Check số lượng và UserDiscount chưa dùng
            if (discount.getQuantity() <= 0)
                throw new RuntimeException("Discount out of quantity");

            UserDiscount userDiscount = userDiscountRepository
                    .findByUser_UserIdAndDiscount_DiscountIdAndIsUsedFalse(request.getUser().getUserId(), discount.getDiscountId())
                    .orElseThrow(() -> new RuntimeException("You already used this discount or it’s not available"));

            // Trừ số lượng và đánh dấu đã dùng
            discount.setQuantity(discount.getQuantity() - 1);
            userDiscount.setUsed(true);
            userDiscountRepository.save(userDiscount);
            discountRepository.save(discount);

            // Tính giảm giá
            if ("Percent".equalsIgnoreCase(discount.getType())) {
                discountAmount = clientTotal * discount.getAmount().doubleValue() / 100.0;
            } else if ("Money".equalsIgnoreCase(discount.getType())) {
                discountAmount = discount.getAmount().doubleValue();
            }

            request.setDiscount(discount);
        } else {
            request.setDiscount(null);
        }

        double finalTotal = Math.max(0, clientTotal - discountAmount);
        double appFee = finalTotal * 0.10;

        bill.setDiscountAmount(discountAmount);
        bill.setTotal(finalTotal);
        bill.setAppFee(appFee);
        bill.setMethod(dto.getPaymentMethod());
        bill.setUpdatedAt(LocalDateTime.now());

        request.setUpdatedAt(new Date());

        requestRescueRepository.save(request);
        billRepository.save(bill);

        // Tạo response
        BillResponse response = new BillResponse();
        response.setRrid(request.getRrid());
        response.setBillId(bill.getBillId());
        response.setTotal(finalTotal);
        response.setCurrency("VND");
        response.setMethod(dto.getPaymentMethod());
        response.setStartAddress(request.getStartAddress());
        response.setEndAddress(request.getEndAddress());

        BillBreakdown breakdown = new BillBreakdown();
        breakdown.setAppFee(appFee);
        breakdown.setDiscount(discountAmount);
        response.setBreakdown(breakdown);

        return response;
    }


    @Transactional
    @Override
    public void completeRescueRequest(int rrid) {
        // 1. Tìm RequestRescue
        RequestRescue request = requestRescueRepository.findById(rrid)
                .orElseThrow(() -> new RuntimeException("RequestRescue not found"));

        // 2. Nếu đã hoàn thành rồi thì không cần làm lại
        if ("COMPLETED".equalsIgnoreCase(request.getStatus())) {
            return;
        }

        // 3. Cập nhật trạng thái
        request.setStatus("COMPLETED");
        request.setUpdatedAt(new Date());

        // 4. Lưu
        requestRescueRepository.save(request);
    }

    @Override
    public void cancelRescue(int rrid, String cancelNote) {
        RequestRescue request = requestRescueRepository.findById(rrid)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus("CANCEL");
        request.setCancelNote(cancelNote != null ? cancelNote : "Cancelled by user");
        request.setUpdatedAt(new Date());

        Discount discount = request.getDiscount();
        if (discount != null) {
            // +1 lại số lượng Discount
            discount.setQuantity(discount.getQuantity() + 1);
            discountRepository.save(discount);

            // Đánh dấu lại UserDiscount là chưa dùng
            Optional<UserDiscount> userDiscountOpt = userDiscountRepository
                    .findByUser_UserIdAndDiscount_DiscountIdAndIsUsedTrue(request.getUser().getUserId(), discount.getDiscountId());
            userDiscountOpt.ifPresent(userDiscount -> {
                userDiscount.setUsed(false);
                userDiscountRepository.save(userDiscount);
            });
        }

        requestRescueRepository.save(request);
    }
    public List<BillResponse> getCompletedBillsByUserId(int userId) {
        List<Bill> bills = billRepository.findSuccessfulBillsByUserId(userId);

        return bills.stream().map(bill -> {
            RequestRescue request = bill.getRequestRescue();
            Services service = serviceRepository.findByServiceType(request.getRescueType()).get(0);

            // Breakdown
            BillBreakdown breakdown = new BillBreakdown();
            breakdown.setServicePrice(service != null ? service.getFixedPrice() : 0);
            breakdown.setDistancePrice(service != null ? service.getPricePerKm() : 0); // optional: real distance * pricePerKm
            breakdown.setAppFee(bill.getAppFee());
            breakdown.setDiscount(bill.getDiscountAmount());

            // BillResponse
            BillResponse response = new BillResponse();
            response.setRrid(request.getRrid());
            response.setBillId(bill.getBillId());
            response.setTotal(bill.getTotalPrice());
            response.setCurrency("VND");
            response.setMethod(bill.getMethod());
            response.setStartAddress(request.getStartAddress());
            response.setEndAddress(request.getEndAddress());
            response.setBreakdown(breakdown);

            return response;
        }).collect(Collectors.toList());
    }

}
