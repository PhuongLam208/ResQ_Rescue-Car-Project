package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.mapper.RequestResQMapper;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.RequestRescueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class RequestRescueServiceImpl implements RequestRescueService {
    @Autowired
    private RequestRescueRepository requestRescueRepo;
    @Autowired
    private BillRepository billRepo;
    @Autowired
    private ReportRepository reportRepo;
    @Autowired
    private FeedbackRepository feedbackRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PaymentRepository paymentRepo;
    @Autowired
    private NotificationRepository notificationRepo;
    @Autowired
    private NotificationTemplateRepository notifTemplateRepo;

    public Optional<RequestRescue> getRequestRescueById(int id) {
        return requestRescueRepo.findById(id);
    }

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public Optional<RequestRescue> findRRById(int rrId) {
        return requestRescueRepo.findById(rrId);
    }

    @Override
    public RequestRescue saveRequestRescue(RequestRescue requestRescue) {
        return requestRescueRepo.save(requestRescue);
    }

    @Override
    public RequestRescue updateStatus(RequestRescue rr, String status) {
        rr.setStatus(status);
        rr.setUpdatedAt(new Date());
        return requestRescueRepo.save(rr);
    }

    public List<RequestResQDto> findAll(){
        List<RequestRescue> listAll = requestRescueRepo.findAll();
        listAll.sort(Comparator.comparing(RequestRescue::getCreatedAt).reversed());
        List<RequestResQDto> rrDtos = new ArrayList<>();
        for (RequestRescue rr : listAll) {
            RequestResQDto dto = RequestResQMapper.toDTO(rr);
            Bill rrBill = billRepo.findBillsByReqResQ(rr.getRrid());
            if(rrBill != null){
                dto.setTotalPrice(rrBill.getTotalPrice());
                dto.setTotal(rrBill.getTotal());
                dto.setAppFee(rrBill.getAppFee());
                dto.setCurrency(rrBill.getCurrency());
                dto.setPaymentStatus(rrBill.getStatus());
                if(rrBill.getPayment() != null){
                    dto.setPaymentMethod(rrBill.getPayment().getName());
                }else{
                    dto.setPaymentMethod("N/A");
                }
            }else{
                dto.setTotalPrice(0);
                dto.setTotal(0);
                dto.setAppFee(0);
                dto.setPaymentStatus("N/A");
            }
            rrDtos.add(dto);
        }
        return rrDtos;
    }


    public Optional<RequestResQDto> findById(int rrId){
        Optional<RequestRescue> rrById = requestRescueRepo.findById(rrId);
        Optional<RequestResQDto> rrDto = Optional.empty();
        if (rrById.isPresent()) {
            rrDto = Optional.of(RequestResQMapper.toDTO(rrById.get()));
            Bill rrBill = billRepo.findBillsByReqResQ(rrById.get().getRrid());
            if(rrBill != null){
                rrDto.get().setTotalPrice(rrBill.getTotalPrice());
                rrDto.get().setTotal(rrBill.getTotal());
                rrDto.get().setCurrency(rrBill.getCurrency());
                rrDto.get().setAppFee(rrBill.getAppFee());
                rrDto.get().setPaymentStatus(rrBill.getStatus());
                if(rrBill.getPayment() != null){
                    rrDto.get().setPaymentMethod(rrBill.getPayment().getName());
                }else{
                    rrDto.get().setPaymentMethod("N/A");
                }
            }else{
                rrDto.get().setTotalPrice(0);
                rrDto.get().setTotal(0);
                rrDto.get().setAppFee(0);
                rrDto.get().setPaymentStatus("N/A");
            }
        }
        return rrDto;
    }

    public List<RequestResQDto> searchByUser(int userId){
        List<RequestRescue> listResult = requestRescueRepo.searchByUser(userId);
        List<RequestResQDto> rrDtos = new ArrayList<>();
        for (RequestRescue rr : listResult) {
            Bill rrBill = billRepo.findBillsByReqResQ(rr.getRrid());
            RequestResQDto dto = RequestResQMapper.toDTO(rr);
            if(rrBill != null){
                dto.setTotalPrice(rrBill.getTotalPrice());
                dto.setTotal(rrBill.getTotal());
                dto.setCurrency(rrBill.getCurrency());
                dto.setAppFee(rrBill.getAppFee());
                dto.setPaymentStatus(rrBill.getStatus());
                if(rrBill.getPayment() != null){
                    dto.setPaymentMethod(rrBill.getPayment().getName());
                }else{
                    dto.setPaymentMethod("N/A");
                }
            }else{
                dto.setTotalPrice(0);
                dto.setTotal(0);
                dto.setAppFee(0);
                dto.setPaymentStatus("N/A");
            }
            rrDtos.add(dto);
        }
        return rrDtos;
    }

    public List<RequestResQDto> searchByPartner(int partnerId){
        List<RequestRescue> listResult = requestRescueRepo.searchByPartner(partnerId);
        List<RequestResQDto> rrDtos = new ArrayList<>();
        for (RequestRescue rr : listResult) {
            Bill rrBill = billRepo.findBillsByReqResQ(rr.getRrid());
            RequestResQDto dto = RequestResQMapper.toDTO(rr);
            if(rrBill != null){
                dto.setTotalPrice(rrBill.getTotalPrice());
                dto.setTotal(rrBill.getTotal());
                dto.setCurrency(rrBill.getCurrency());
                dto.setAppFee(rrBill.getAppFee());
                dto.setPaymentStatus(rrBill.getStatus());
                if(rrBill.getPayment() != null){
                    dto.setPaymentMethod(rrBill.getPayment().getName());
                }else{
                    dto.setPaymentMethod("N/A");
                }
            }else{
                dto.setTotalPrice(0);
                dto.setTotal(0);
                dto.setAppFee(0);
                dto.setPaymentStatus("N/A");
            }
            rrDtos.add(dto);
        }
        return rrDtos;
    }

    public List<RequestResQDto> searchRR(String keyword){
        List<RequestRescue> listResult = requestRescueRepo.searchRR("%"+keyword+"%");
        List<RequestResQDto> rrDtos = new ArrayList<>();

        for (RequestRescue rr : listResult) {
            RequestResQDto dto = RequestResQMapper.toDTO(rr);
            Bill rrBill = billRepo.findBillsByReqResQ(rr.getRrid());
            if(rrBill != null){
                dto.setTotalPrice(rrBill.getTotalPrice());
                dto.setTotal(rrBill.getTotal());
                dto.setAppFee(rrBill.getAppFee());
                dto.setCurrency(rrBill.getCurrency());
                dto.setPaymentStatus(rrBill.getStatus());
            }
            rrDtos.add(dto);
        }
        return rrDtos;
    }

    public List<RequestResQDto> searchRRWithUser(int userId, String keyword){
        List<RequestRescue> listResult = requestRescueRepo.searchRRWithUser(userId, "%"+keyword+"%");
        List<RequestResQDto> rrDtos = new ArrayList<>();
        for (RequestRescue rr : listResult) {
            RequestResQDto dto = RequestResQMapper.toDTO(rr);
            Bill rrBill = billRepo.findBillsByReqResQ(rr.getRrid());
            if(rrBill != null){
                dto.setTotalPrice(rrBill.getTotalPrice());
                dto.setTotal(rrBill.getTotal());
                dto.setAppFee(rrBill.getAppFee());
                dto.setCurrency(rrBill.getCurrency());
                dto.setPaymentStatus(rrBill.getStatus());
            }
            rrDtos.add(dto);
        }
        return rrDtos;
    }

    public List<RequestResQDto> searchRRWithPartner(int partnerId, String keyword){
        List<RequestRescue> listResult = requestRescueRepo.searchRRWithPartner(partnerId, "%"+keyword+"%");
        List<RequestResQDto> rrDtos = new ArrayList<>();
        for (RequestRescue rr : listResult) {
            RequestResQDto dto = RequestResQMapper.toDTO(rr);
            Bill rrBill = billRepo.findBillsByReqResQ(rr.getRrid());
            if(rrBill != null){
                dto.setTotalPrice(rrBill.getTotalPrice());
                dto.setTotal(rrBill.getTotal());
                dto.setAppFee(rrBill.getAppFee());
                dto.setCurrency(rrBill.getCurrency());
                dto.setPaymentStatus(rrBill.getStatus());
            }
            rrDtos.add(dto);
        }
        return rrDtos;
    }

    public RecordStatusDto existedRecords(int requestId){
        RecordStatusDto status = new RecordStatusDto();
        List<Feedback> feedbackList = feedbackRepo.searchByRR(requestId);
        if(!feedbackList.isEmpty()){
            status.setHasFeedbacks(true);
        }
        List<Report> reportList = reportRepo.findByRequestRescue(requestId);
        if(!reportList.isEmpty()){
            for(Report r : reportList){
                if(r.getComplainantCustomer() != null){
                    status.setHasCustomerReport(true);
                }
                if(r.getComplainantPartner() != null){
                    status.setHasPartnerReport(true);
                }
            }
        }
        return status;
    }

    public RequestRescue createNew(RequestResQDto requestDto){
        RequestRescue newRescue = RequestResQMapper.toEntity(requestDto);
        User customer = userRepo.findUserById(requestDto.getCustomerId()).orElseThrow();
        Payment payment = paymentRepo.customerPaymentId(customer.getUserId(), requestDto.getPaymentMethod());
        requestDto.setTotalPrice(requestDto.getTotal());

        newRescue.setUser(customer);
        newRescue.setStatus("PENDING");
        RequestRescue savedRescue = requestRescueRepo.save(newRescue);

        Double appFee = (requestDto.getTotal() * 15) / 100;

        Bill newBill = new Bill();
        newBill.setRequestRescue(savedRescue);
        newBill.setCreatedAt(LocalDateTime.now(ZONE));
        newBill.setPayment(payment);
        newBill.setAppFee(appFee);
        newBill.setTotalPrice(requestDto.getTotalPrice());
        newBill.setTotal(requestDto.getTotal());
        newBill.setStatus("PENDING");
        billRepo.save(newBill);

        NotificationTemplate notiTemplate = notifTemplateRepo.findByNotiType("NOTI");
        Notification noti = new Notification();
        noti.setCreatedAt(new Date());
        noti.setMessage("New Request Rescue has been created at " +
                new java.text.SimpleDateFormat("h:mma").format(new Date()));
        noti.setUser(customer);
        noti.setNotificationTemplate(notiTemplate);
        notificationRepo.save(noti);

        return newRescue;
    }

    public RequestRescue updateRequest(RequestResQDto requestDto){
        RequestRescue request = requestRescueRepo.findById(requestDto.getRrid())
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if(requestDto.getULocation() != null){
            request.setULocation(requestDto.getULocation());
        }
        if(requestDto.getDestination() != null){
            request.setDestination(requestDto.getDestination());
        }
        Bill bill = billRepo.findBillsByReqResQ(requestDto.getRrid());
        if(requestDto.getPaymentMethod() != null){
            Payment payment = paymentRepo.customerPaymentId(requestDto.getCustomerId(), requestDto.getPaymentMethod());
            bill.setPayment(payment);
        }
        if(requestDto.getTotal() > 0){
            bill.setTotal(requestDto.getTotal());
        }
        billRepo.save(bill);
        return requestRescueRepo.save(request);

    }

    public List<RequestRescueDto> getAllRequestRescue() {
        return requestRescueRepo.findAllRequestRescueDtos();
    }

    public RequestRescueDto getRequestRescueByRrid(Integer rrid) {
        return requestRescueRepo.findRequestRescueDtoByRrid(rrid);
    }

    public List<RequestResQDto> getCusRequestForCancel(int customerId){
        List<RequestRescue> result = requestRescueRepo.searchByUser(customerId);
        List<RequestResQDto> rescueDtos = new ArrayList<>();
        for(RequestRescue r : result){
            if(r.getStatus().equalsIgnoreCase("pending") || r.getStatus().equalsIgnoreCase("on trip")) {
                RequestResQDto dto = RequestResQMapper.toDTO(r);
                rescueDtos.add(dto);
            }
        }
        return rescueDtos;
    }

    public boolean cancelRequest(int requestId, String reason){
        boolean result = false;
        RequestRescue request = requestRescueRepo.findById(requestId).orElseThrow();
        if(request != null) {
            request.setStatus("CANCELLED");
            requestRescueRepo.save(request);

            NotificationTemplate notiTemplate = notifTemplateRepo.findByNotiType("NOTI");
            //Notification for Customer
            Notification noti = new Notification();
            noti.setCreatedAt(new Date());
            noti.setMessage(reason);
            noti.setUser(request.getUser());
            noti.setNotificationTemplate(notiTemplate);
            notificationRepo.save(noti);

            //Notification for Partner
            if(request.getPartner() != null){
                Notification notiPartner = new Notification();
                notiPartner.setCreatedAt(new Date());
                notiPartner.setMessage(reason);
                notiPartner.setUser(request.getPartner().getUser());
                notiPartner.setNotificationTemplate(notiTemplate);
                notificationRepo.save(notiPartner);
            }
            return true;
        }
        return result;
    }

    @Override
    public List<RequestResQDto> getCompletedRescuesByUserId(int userId) {
        List<RequestRescue> result = requestRescueRepo.findByUser_UserIdAndStatusOrderByCreatedAtDesc(userId, "COMPLETED");
        List<RequestResQDto> rescueDtos = new ArrayList<>();
        for(RequestRescue r : result){
            if(r.getStatus().equalsIgnoreCase("COMPLETED")){
                RequestResQDto dto = RequestResQMapper.toDTO(r);
                rescueDtos.add(dto);
            }
        }
        return rescueDtos;
    }

    @Override
    public List<RequestResQDto> getCompletedRescuesByPartner(int partnerId) {
        List<RequestRescue> result = requestRescueRepo.findByPartner_PartnerIdAndStatusOrderByCreatedAtDesc(partnerId, "COMPLETED");
        List<RequestResQDto> rescueDtos = new ArrayList<>();
        for(RequestRescue r : result){
            if(r.getStatus().equalsIgnoreCase("COMPLETED")){
                RequestResQDto dto = RequestResQMapper.toDTO(r);
                rescueDtos.add(dto);
            }
        }
        return rescueDtos;
    }

    @Override
    public Optional<RequestRescue> getRequestRescueByPartnerIdAndStatus(int partnerId, String status) {
        return requestRescueRepo.findRequestRescueByPartner_PartnerIdAndStatus(partnerId, status);
    }

    @Override
    public List<RescueInfoDTO> findAllRescueInfo() {
        return requestRescueRepo.findAllRescueInfo();
    }

    @Override
    public void updateBillStatusByBillId(int billId, String status) {
        requestRescueRepo.updateBillStatusByBillId(billId, status);
    }

    @Override
    public Optional<RescueDetailDTO> findRescueDetailByRRID(int rrid) {
        return requestRescueRepo.findRescueDetailByRRID(rrid);
    }

}
