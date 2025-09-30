package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.Bill;
import com.livewithoutthinking.resq.entity.Discount;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.service.*;
import com.livewithoutthinking.resq.util.DistanceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resq/pcrescue")
public class ProcessRescueController {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private RequestSrvService requestSrvService;

    @Autowired
    private UserDiscountService userDiscountService;

    @Autowired
    private RequestRescueService requestRescueService;

    @Autowired
    private RescueAssignmentService rescueAssignmentService;

    @PostMapping("/nearby-partners")
    public ResponseEntity<ApiResponse<List<NearbyPartnerDTO>>> getNearbyPartners(@RequestBody LocationRequest request) {
        List<NearbyPartnerDTO> result = partnerService.findNearbyPartners(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Get partners nearby customer"));
    }

    @GetMapping("/service")
    public ResponseEntity<ApiResponse<List<ServiceDto>>> getServicesByType(@RequestParam String type) {
        List<ServiceDto> services = servicesService.findByServiceType(type);
        return ResponseEntity.ok(ApiResponse.success(services, "Get all services by type " + type));
    }

    @PostMapping("/rescue/request")
    public ResponseEntity<ApiResponse<BillResponse>> createRescueRequest(@RequestBody CreateRescueRequestDTO dto) {
        if (dto.getPaymentMethod() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.errorServer("Missing paymentMethod"));
        }

        BillResponse billResponse = requestSrvService.createRequestAndBill(dto);
        return ResponseEntity.ok(ApiResponse.success(billResponse, "Request created successfully"));
    }
    @GetMapping("/with-bill/{rrid}")
    public ResponseEntity<ApiResponse<BillResponse>> getRequestRescueWithBill(@PathVariable Integer rrid) {
        BillResponse rr = requestSrvService.getRequestRescueWithBill(rrid);
        return ResponseEntity.ok(ApiResponse.success(rr, "Request with bill"));
    }
    // Cập nhật phương thức thanh toán và mã giảm giá
    @PostMapping("/rescue/update-bill")
    public ResponseEntity<ApiResponse<BillResponse>> updateBill(@RequestBody UpdateBillDto dto) {
        try {
            BillResponse response = requestSrvService.updateDiscountAndPayment(dto);
            return ResponseEntity.ok(ApiResponse.success(response, "Updated bill successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.errorServer(e.getMessage()));
        }
    }

    // Hoàn tất cứu hộ
    @PostMapping("/rescue/complete")
    public ResponseEntity<ApiResponse<String>> completeRescue(@RequestParam int rrid) {
        try {
            requestSrvService.completeRescueRequest(rrid);
            return ResponseEntity.ok(ApiResponse.ok("Rescue request marked as COMPLETED"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.errorServer(e.getMessage()));
        }
    }

    // Hủy cứu hộ
    @PostMapping("/rescue/cancel")
    public ResponseEntity<ApiResponse<String>> cancelRescue(@RequestParam int rrid,
                                                            @RequestParam(required = false) String note) {
        try {
            requestSrvService.cancelRescue(rrid, note);
            return ResponseEntity.ok(ApiResponse.ok("Rescue request cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.errorServer(e.getMessage()));
        }
    }
    @GetMapping("/discounts/available/{userId}")
    public ResponseEntity<ApiResponse<List<Discount>>> getAvailableDiscounts(@PathVariable Integer userId) {
        List<Discount> discounts = userDiscountService.getAvailableDiscountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(discounts, "Get all available discounts"));
    }


    /**
     * Gởi yêu cầu cứu hộ sang partner phù hợp
     */
    @PostMapping("/dispatch")
    public ResponseEntity<ApiResponse<String>> dispatchRescueRequest(@RequestBody DispatchRequestDto request) {
        boolean dispatched = rescueAssignmentService.updateAndDispatchRequest(
                request.getRrid(), request.getLat(), request.getLon());

        return dispatched
                ? ResponseEntity.ok(ApiResponse.ok("Rescue request dispatched to nearby partners."))
                : ResponseEntity.badRequest().body(ApiResponse.errorServer("Rescue request dispatching failed"));
    }


    /**
     * Partner chấp nhận yêu cầu cứu hộ
     */
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<RescueRequestAcceptedDto>> acceptRescueRequest(
            @RequestBody AcceptRequestDto requestDto) {

        int rrid = requestDto.getRrid();
        int partnerId = requestDto.getPartnerId();

        RescueRequestAcceptedDto dto = rescueAssignmentService.acceptRequest(rrid, partnerId);

        if (dto != null) {
            return ResponseEntity.ok(ApiResponse.success(dto, "Get accepted request successfully"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.errorServer("Failed to accept rescue request."));
        }
    }
    @PostMapping("/deny")
    public ResponseEntity<?> denyRequest(@RequestParam int rrid, @RequestParam int partnerId) {
        rescueAssignmentService.denyRequest(rrid, partnerId);
        return ResponseEntity.ok("Request denied and moved to next partner.");
    }
    /**
     * API tính khoảng cách giữa 2 tọê độ
     */
    @GetMapping("/distance")
    public ResponseEntity<Double> calculateDistance(@RequestParam double lat1,
                                                    @RequestParam double lon1,
                                                    @RequestParam double lat2,
                                                    @RequestParam double lon2) {
        double distance = DistanceUtils.calculateDistance(lat1, lon1, lat2, lon2);
        return ResponseEntity.ok(distance);
    }

    @GetMapping("/completed/user/{userId}")
    public ResponseEntity<ApiResponse<List<RequestResQDto>>> getUserCompletedRescues(@PathVariable Integer userId) {
        List<RequestResQDto> rescues = requestRescueService.getCompletedRescuesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(rescues, "Completed rescues for user"));
    }

    // Lấy danh sách cuốc đã hoàn thành theo partnerId
    @GetMapping("/completed/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<RequestResQDto>>> getCompletedByPartner(@PathVariable int partnerId) {
        List<RequestResQDto> list = requestRescueService.getCompletedRescuesByPartner(partnerId);
        return ResponseEntity.ok(ApiResponse.success(list, "Rescue requests completed for partner"));
    }
}
