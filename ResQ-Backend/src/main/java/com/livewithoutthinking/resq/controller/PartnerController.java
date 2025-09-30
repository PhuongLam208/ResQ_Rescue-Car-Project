package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.PartnerRegistrationRequest;
import com.livewithoutthinking.resq.dto.PartnerServiceUpdateRequest;
import com.livewithoutthinking.resq.dto.RRInfoDto;
import com.livewithoutthinking.resq.entity.Bill;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resq/partner")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private RequestRescueService requestRescueService;
    @Autowired
    private BillService billService;
    @Autowired
    private PartnerRegistrationService partnerRegistrationService;
    @Autowired
    private PaymentService paymentService;

//    @GetMapping("/get-new-rr/{partnerId}")
//    public ResponseEntity<?> getNewRR(@PathVariable Integer partnerId) {
//        Optional<RequestRescue> requestRescue = requestRescueService
//                .getRequestRescueByPartnerIdAndStatus(partnerId,"PENDING");
//        if (requestRescue.isEmpty()) {
//            return ResponseEntity.status(404).body("No request rescue found");
//        }
//        Bill bill = billService.findBillsByReqResQ(requestRescue.get().getRrid());
//        RRInfoDto rrInfoDto = new RRInfoDto();
//        rrInfoDto.setRrId(requestRescue.get().getRrid());
//        rrInfoDto.setPaymentMethod(bill.getMethod());
//        rrInfoDto.setTotal(bill.getTotal());
//        return ResponseEntity.status(200).body(rrInfoDto);
//    }

    @PutMapping("/accept/{rrId}")
    public ResponseEntity<?> partnerAcceptRequestRescue (@PathVariable Integer rrId) {
        Optional<RequestRescue> rr = requestRescueService.getRequestRescueById(rrId);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("Request Rescue Not Found");
        }
        return ResponseEntity.status(200)
                .body(requestRescueService.updateStatus(rr.get(), "ACCEPT"));
    }

    @PutMapping("/denied/{rrId}")
    public ResponseEntity<?> partnerDeniedRequestRescue (@PathVariable Integer rrId) {
        Optional<RequestRescue> rr = requestRescueService.getRequestRescueById(rrId);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("Request Rescue Not Found");
        }
        return ResponseEntity.status(200)
                .body(requestRescueService.updateStatus(rr.get(), "DENIED"));
    }

    @PutMapping("/arrived/{rrId}")
    public ResponseEntity<?> partnerArrivedRequestRescue (@PathVariable("rrId") Integer rrId) {

        Optional<RequestRescue> rr = requestRescueService.getRequestRescueById(rrId);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("Request Rescue Not Found");
        }

        // còn phải check khoảng cách
        // ( xử lý location xong sẽ phải thêm )
        // < 50m mới được nhấn nút

        rr.get().setStatus("ON TRIP");
        rr.get().setUpdatedAt(new Date());
        requestRescueService.saveRequestRescue(rr.get());
        return ResponseEntity.status(200).body(rr);
    }

    @PutMapping("/cancel/{rrId}")
    public ResponseEntity<?> partnerCancelRequestRescue (
            @PathVariable("rrId") Integer rrId,
            @RequestBody Map<String, String> requestBody) {

        int partnerId = Integer.parseInt(requestBody.getOrDefault("partnerId", "0"));

        try {
            Partner partner = partnerService.findById(partnerId);
            partner.deductWalletAmount();
            partnerService.save(partner);

            Optional<RequestRescue> rr = requestRescueService.getRequestRescueById(rrId);
            if (rr.isEmpty()) {
                return ResponseEntity.status(404).body("Request Rescue Not Found");
            }

            rr.get().setStatus("CANCELLED");
            rr.get().setCancelNote(requestBody.get("cancelNote"));
            rr.get().setUpdatedAt(new Date());
            requestRescueService.saveRequestRescue(rr.get());

            return ResponseEntity.status(200).body(rr);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body("Partner Not Found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping("/complete/{rrId}")
    public ResponseEntity<?> partnerCompleteRequestRescue (@PathVariable("rrId") Integer rrId) {

        Optional<RequestRescue> rr = requestRescueService.getRequestRescueById(rrId);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("Request Rescue Not Found");
        }

        return ResponseEntity.status(200)
                .body(requestRescueService.updateStatus(rr.get(), "COMPLETED"));
    }

    @PostMapping("/feedback/save/{rrid}")
    public ResponseEntity<?> saveFeedback(@PathVariable Integer rrid,
                                          @RequestBody Map<String, String> requestBody ) {

        Optional<RequestRescue> rr = requestRescueService.findRRById(rrid);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("RequestRescue not found");
        }
        // lưu đánh giá customer
        feedbackService.saveRR(
                rr.get(),
                "FEEDBACK CUSTOMER",
                Integer.parseInt(requestBody.getOrDefault("CustomerRate", "0")),
                requestBody.get("RescueDescription")
        );
        return ResponseEntity.status(200).body("Feedback saved successfully");

    }

    @GetMapping("/get-payment-amount/{rrId}")
    public ResponseEntity<?> getPaymentAmount(@PathVariable Integer rrId) {

        Optional<RequestRescue> rr = requestRescueService.findRRById(rrId);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("RequestRescue not found");
        }
        try {
            Bill bill = billService.findBillsByReqResQ(rr.get().getRrid());
            Map<String, Double> response = new HashMap<>();
            response.put("totalPrice", bill.getTotalPrice());
            response.put("appFee", bill.getAppFee());
            return ResponseEntity.status(200).body(response);
        }  catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body("Bill Not Found");
        }
    }

    @PutMapping("/receive-money/{partnerId}")
    public ResponseEntity<?> receiveMoney(
            @PathVariable Integer partnerId,
            @RequestBody Map<String, String> requestBody ) {
        String totalReceived = requestBody.getOrDefault("totalReceived", "0");
        String method = requestBody.get("method");
        try {
            Partner partner = partnerService.findById(partnerId);

            if (totalReceived == null || totalReceived.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Missing totalReceived amount");
            }

            double amount;
            try {
                amount = Double.parseDouble(totalReceived);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Invalid amount format");
            }

            if (amount <= 0) {
                return ResponseEntity.badRequest().body("Amount must be greater than 0");
            }

            if(method.equals("CASH")){
                partner.deductWalletAmount(amount);
            }else{
                partner.addWalletAmount(amount);
            }
            partnerService.save(partner);

            return ResponseEntity.status(200).body(partner);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body("Partner Not Found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerPartner(
            @ModelAttribute PartnerRegistrationRequest request
    ) throws IOException {
        return partnerRegistrationService.registerPartner(request);
    }

    @GetMapping("/services")
    public ResponseEntity<?> getServicesByType(@RequestParam("type") String type) {
        return partnerRegistrationService.getServicesByType(type);
    }

    @GetMapping("/registered-types")
    public ResponseEntity<?> getRegisteredPartnerTypes(@RequestParam("userId") Integer userId) {
        return partnerRegistrationService.getRegisteredPartnerTypes(userId);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getPartnerInfo(@RequestParam("userId") Integer userId) {
        return partnerRegistrationService.getPartnerInfo(userId);
    }

    @GetMapping("/services/selected")
    public ResponseEntity<?> getSelectedServiceIds(
            @RequestParam("userId") Integer userId,
            @RequestParam("type") String type) {
        return partnerRegistrationService.getSelectedServiceIds(userId, type);
    }

    @PostMapping("/services/update")
    public ResponseEntity<?> updateSelectedServices(@RequestBody PartnerServiceUpdateRequest request) {
        return partnerRegistrationService.updateSelectedServices(request);
    }

    @GetMapping("/is-registered")
    public ResponseEntity<?> isPartnerRegistered(@RequestParam("userId") Integer userId) {
        boolean exists = partnerRegistrationService.isPartnerRegistered(userId);
        return ResponseEntity.ok(Map.of("registered", exists));
    }

    @GetMapping("/documents")
    public ResponseEntity<?> getPartnerDocuments(@RequestParam("userId") Integer userId) {
        return ResponseEntity.ok(partnerRegistrationService.getPartnerDocumentMap(userId));
    }

    @PutMapping("/online")
    public ResponseEntity<?> updateOnlineStatus(
            @RequestParam Integer userId,
            @RequestParam boolean status) {
        return partnerRegistrationService.updateOnlineStatus(userId, status);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPartnerType(
            @RequestParam Integer userId,
            @RequestParam String type) {
        return partnerRegistrationService.cancelPartnerType(userId, type);
    }


    @GetMapping("/user-fullname")
    public ResponseEntity<?> getUserFullname(@RequestParam Integer userId) {
        return partnerRegistrationService.getUserFullname(userId);
    }

    @PutMapping(value = "/documents/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDocument(
            @RequestParam Integer documentId,
            @RequestParam(required = false) String documentNumber,
            @RequestParam Integer userId,
            @RequestParam(required = false) MultipartFile frontImage,
            @RequestParam(required = false) MultipartFile backImage
    ) {
        return partnerRegistrationService.updateDocument(documentId, documentNumber, userId, frontImage, backImage);
    }

    @GetMapping("/user-avatar")
    public ResponseEntity<?> getUserAvatar(@RequestParam Integer userId) {
        return partnerRegistrationService.getUserAvatar(userId);
    }

    @PutMapping("/updateWalletPoint/{partnerId}")
    public ResponseEntity<String> updateWalletPoint(@PathVariable("partnerId") int partnerId) {
        try {
            boolean updated = partnerService.updatePartnerWalletAmount(partnerId);
            if (updated) {
                return ResponseEntity.ok("Update wallet amount successful.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Update wallet amount failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System error.");
        }
    }

    @GetMapping("/paypalPayment/{partnerId}")
    public ResponseEntity<?> getPartnerPaypalPayment(@PathVariable("partnerId") int partnerId) {
        try{
            return ResponseEntity.ok(paymentService.getPartnerPaypalPayment(partnerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System error.");
        }
    }

}
