package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.config.RescueRequestSender;
import com.livewithoutthinking.resq.dto.RescueRequestAcceptedDto;
import com.livewithoutthinking.resq.dto.RescueRequestNotificationDto;
import com.livewithoutthinking.resq.entity.Bill;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.entity.Vehicle;
import com.livewithoutthinking.resq.repository.BillRepository;
import com.livewithoutthinking.resq.repository.PartnerRepository;
import com.livewithoutthinking.resq.repository.RequestRescueRepository;
import com.livewithoutthinking.resq.service.RescueAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RescueAssignmentServiceImpl implements RescueAssignmentService {


    @Autowired
    private final PartnerRepository partnerRepository;

    @Autowired
    private final RequestRescueRepository requestRescueRepository;

    @Autowired
    private final BillRepository billRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Autowired
    private RescueRequestSender rescueRequestSender;

    private final Map<Integer, Timer> rescueTimers = new HashMap<>();


    // Tính khoảng cách

    public class DistanceUtils {
        public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            final int R = 6371; // km
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }
    }

    @Override
    public boolean updateAndDispatchRequest(int rrid, double lat, double lon) {
        Optional<RequestRescue> optionalRR = requestRescueRepository.findById(rrid);
        if (optionalRR.isEmpty()) return false;

        RequestRescue rr = optionalRR.get();
        rr.setStatus("WAITING");
        rr.setPartner(null);
        requestRescueRepository.save(rr);

        List<Partner> partners = partnerRepository.findAllAvailablePartners().stream()
                .filter(p -> DistanceUtils.calculateDistance(lat, lon, p.getLatitude(), p.getLongitude()) <= 5)
                .sorted(Comparator.comparingDouble(p -> DistanceUtils.calculateDistance(lat, lon, p.getLatitude(), p.getLongitude())))
                .collect(Collectors.toList());

        if (!partners.isEmpty()) {
            processPartnerQueue(rr, partners, 0);
            return true;
        }
        return false;
    }

    @Override
    public void processPartnerQueue(RequestRescue rr, List<Partner> partners, int currentIndex) {
        if (currentIndex >= partners.size()) {
            rr.setStatus("FAILED");
            requestRescueRepository.save(rr);

            // Gửi thông điệp nếu không tìm thấy partner
            String message = "{\"type\":\"REQUEST_TIMEOUT\",\"rrid\":" + rr.getRrid() + "}";
            int userId = rr.getUser().getUserId(); // Giả sử có phương thức getUser() trong RequestRescue
            rescueRequestSender.sendMessageToUser(userId, message);
            return;
        }

        Partner current = partners.get(currentIndex);

        // Kiểm tra trạng thái của partner
        if (current.getStatus().equals("ACTIVE")) {
            String message = "{\"type\":\"PARTNER_ASSIGNED\",\"rrid\":" + rr.getRrid() + ",\"partnerName\":\"" + current.getUser().getFullName() + "\"}";
            int userId = rr.getUser().getUserId(); // Giả sử có phương thức getUser() trong RequestRescue
            rescueRequestSender.sendMessageToUser(userId, message);

            // Tạo DTO cho thông báo
            Bill bill = billRepository.findBillsByReqResQ(rr.getRrid());
            RescueRequestNotificationDto dto = new RescueRequestNotificationDto();
            dto.setRrid(rr.getRrid());
            dto.setStartLatitude(rr.getUserLatitude());
            dto.setStartLongitude(rr.getUserLongitude());

            dto.setEndLatitude(rr.getDestLatitude());
            dto.setEndLatitude(rr.getDestLongitude());


            dto.setUserFullName(rr.getUser().getFullName());
            dto.setFrom(rr.getStartAddress());
            dto.setTo(rr.getEndAddress());
            dto.setServiceType(rr.getRescueType());

            if (bill != null) {
                dto.setFinalPrice(bill.getTotal());
                dto.setDiscountAmount(bill.getDiscountAmount());
                dto.setPaymentMethod(bill.getMethod());
            }

            rescueRequestSender.sendRescueRequestToPartner(current.getPartnerId(), dto);

            // Hủy timer cũ nếu có
            if (rescueTimers.containsKey(rr.getRrid())) {
                rescueTimers.get(rr.getRrid()).cancel();
            }

            // Tạo timer mới
            Timer timer = new Timer();
            rescueTimers.put(rr.getRrid(), timer);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Optional<RequestRescue> optRR = requestRescueRepository.findById(rr.getRrid());
                    if (optRR.isPresent() && optRR.get().getPartner() == null) {
                        String timeoutMessage = "{\"type\":\"REQUEST_TIMEOUT\",\"rrid\":" + rr.getRrid() + "}";
                        int userId = rr.getUser().getUserId(); // Giả sử có phương thức getUser() trong RequestRescue
                        rescueRequestSender.sendMessageToUser(userId, timeoutMessage);
                        processPartnerQueue(rr, partners, currentIndex + 1);
                    }
                }
            }, 30000);
        } else {
            // Nếu partner không hoạt động, tiếp tục với partner tiếp theo
            processPartnerQueue(rr, partners, currentIndex + 1);
        }
    }


    @Override
    public RescueRequestAcceptedDto acceptRequest(int rrid, int partnerId) {
        Optional<RequestRescue> optionalRR = requestRescueRepository.findById(rrid);
        Optional<Partner> optionalPartner = partnerRepository.findById(partnerId);

        if (optionalRR.isPresent() && optionalPartner.isPresent()) {
            RequestRescue rr = optionalRR.get();

            if (rr.getPartner() == null) {
                Partner partner = optionalPartner.get();

                rr.setPartner(partner);
                rr.setStatus("ACCEPTED");
                requestRescueRepository.save(rr);

                // Tính khoảng cách
                double distanceKm = DistanceUtils.calculateDistance(
                        rr.getUserLongitude(), rr.getUserLatitude(),
                        partner.getLatitude(), partner.getLongitude()
                );
                String message = "{\"type\":\"PARTNER_ACCEPTED\",\"rrid\":" + rr.getRrid() + ",\"partnerId\":" + partner.getPartnerId() + ",\"distanceKm\":" + distanceKm + "}";
                rescueRequestSender.sendMessageToUser(rr.getRrid(), message);

                // Lấy vehicle có status = true
                Vehicle vehicle = partner.getVehicle();
                String frontImage = vehicle != null ? vehicle.getFrontImage() : null;
                String model = vehicle != null ? vehicle.getModel() : null;
                String brand = vehicle != null ? vehicle.getBrand() : null;
                String vehicleStatus = vehicle != null ? vehicle.getVehicleStatus() : null;

                RescueRequestAcceptedDto dto = new RescueRequestAcceptedDto(
                        rr.getRrid(),
                        partner.getPartnerId(),
                        partner.getLatitude(),
                        partner.getLongitude(),
                        distanceKm,
                        frontImage,
                        model,
                        brand,
                        vehicleStatus
                );

                // Gửi WebSocket
                rescueRequestSender.sendRescueRequestToPartnerAccept(rr.getRrid(), dto);

                // Dừng timer nếu có
                if (rescueTimers.containsKey(rr.getRrid())) {
                    rescueTimers.get(rr.getRrid()).cancel();
                    rescueTimers.remove(rr.getRrid());
                }

                return dto;
            }
        }
        return null; // Hoặc ném exception nếu không hợp lệ
    }

    @Override
    public void denyRequest(int rrid, int partnerId) {
        Optional<RequestRescue> optionalRR = requestRescueRepository.findById(rrid);
        if (optionalRR.isPresent()) {
            RequestRescue rr = optionalRR.get();

            // Lấy danh sách partner lại dựa theo khoảng cách
            List<Partner> partners = partnerRepository.findAllAvailablePartners().stream()
                    .filter(p -> DistanceUtils.calculateDistance(rr.getUserLatitude(), rr.getUserLongitude(), p.getLatitude(), p.getLongitude()) <= 5)
                    .sorted(Comparator.comparingDouble(p -> DistanceUtils.calculateDistance(rr.getUserLatitude(), rr.getUserLongitude(), p.getLatitude(), p.getLongitude())))
                    .collect(Collectors.toList());

            int currentIndex = -1;
            for (int i = 0; i < partners.size(); i++) {
                if (partners.get(i).getPartnerId() == partnerId) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex != -1 && rr.getPartner() == null) {
                String message = "{\"type\":\"REQUEST_CANCELLED\",\"rrid\":" + rr.getRrid() + "}";
                int userId = rr.getUser().getUserId(); // Giả sử có phương thức getUser() trong RequestRescue
                rescueRequestSender.sendMessageToUser(userId, message);
                if (rescueTimers.containsKey(rrid)) {
                    rescueTimers.get(rrid).cancel();
                    rescueTimers.remove(rrid);
                }

                processPartnerQueue(rr, partners, currentIndex + 1);
            }
        }
    }


}

