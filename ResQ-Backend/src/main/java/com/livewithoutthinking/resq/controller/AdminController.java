package com.livewithoutthinking.resq.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.*;

import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.mapper.DiscountMapper;
import com.livewithoutthinking.resq.mapper.PartnerMapper;
import com.livewithoutthinking.resq.mapper.PaymentMapper;
import com.livewithoutthinking.resq.repository.BillRepository;
import com.livewithoutthinking.resq.repository.NotificationTemplateRepository;
import com.livewithoutthinking.resq.repository.RescueHistoryRepository;
import com.livewithoutthinking.resq.service.*;
import com.livewithoutthinking.resq.service.PartnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/resq/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private ManagerService managerService;
    @Autowired
    private RescueHistoryRepository rescueHistoryRepository;
    @Autowired
    private ScheduleServices scheduleServices;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private BillService billService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private DiscountMapper discountMapper;
    @Autowired
    private DocumentaryService documentaryService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private PersonalDataService personalDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ServicesService servicesService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ExtraServiceSrv extraServiceSrv;
    @Autowired
    private RequestSrvService requestSrvService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RequestRescueService requestRescueService;
    @Autowired
    private PayPalService  payPalService;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;


    /// W ///
    public List<RefundResponse> toResponse(List<RefundRequest> refundRequests) {
        List<RefundResponse> responses = refundRequests.stream()
                .map(r -> {
                    RefundResponse res = new RefundResponse();
                    res.setRefundId(r.getRefundId());
                    res.setStaffName(r.getSenderStaff().getUser().getFullName());
                    Staff staff = r.getSenderStaff();
                    if (staff != null) {
                        res.setRecipientName(staff.getUser().getFullName());
                    } else {
                        res.setRecipientName(null);
                    }
                    res.setUserName(r.getUser().getFullName());
                    res.setAmount(r.getAmount());
                    res.setReason(r.getReason());
                    res.setStatus(r.getStatus());
                    res.setConversationId(r.getConversation().getConversationId());
                    return res;
                }).toList();
        return responses;
    }

    @GetMapping("/staffs")
    public ResponseEntity<List<StaffDto>> findAllStaff() {
        List<Staff> staffs = managerService.findOnlyStaff();

        List<StaffDto> staffDtos = staffs.stream()
                .map(staff -> {
                    StaffDto dto = new StaffDto();
                    dto.setStaffId(staff.getStaffId());
                    dto.setFullName(staff.getUser().getFullName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(staffDtos);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<StaffDto>> findAllManagers() {
        List<Staff> managers = managerService.findAllManager();

        List<StaffDto> managerDtos = managers.stream()
                .map(staff -> {
                    StaffDto dto = new StaffDto();
                    dto.setStaffId(staff.getStaffId());
                    dto.setFullName(staff.getUser().getFullName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(managerDtos);
    }

    //==================REFUND SECTION==================
    @GetMapping("/refunds")
    public ResponseEntity<List<RefundResponse>> getAllRefunds(HttpServletRequest request) {
        List<RefundRequest> requests = adminService.findAllRefundRequests();
        return ResponseEntity.status(200).body(toResponse(requests));
    }

    @GetMapping("/refunds/{id}")
    public ResponseEntity<RefundResponse> findById(
            @PathVariable("id") int id,
            HttpServletRequest request) {
        Optional<RefundRequest> refunds = adminService.findRefundById(id);
        if (refunds.isPresent()) {
            RefundResponse res = new RefundResponse();
            res.setRefundId(refunds.get().getRefundId());
            res.setStaffName(refunds.get().getSenderStaff().getUser().getFullName());
            res.setRecipientName(refunds.get().getRecipientStaff().getUser().getFullName());
            res.setAmount(refunds.get().getAmount());
            res.setReason(refunds.get().getReason());
            res.setStatus(refunds.get().getStatus());

            return ResponseEntity.status(200).body(res);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/refunds/search/{name}")
    public ResponseEntity<List<RefundResponse>> findByName(
            @PathVariable("name") String name,
            HttpServletRequest request) {
        List<RefundRequest> requests = adminService.findRefundByName(name);

        return ResponseEntity.status(200).body(toResponse(requests));
    }

//    @PutMapping("/refunds/received/{id}")
//    public ResponseEntity<RefundResponse> received(
//            @PathVariable("id") int id,
//            HttpServletRequest request) throws Exception {
//        Optional<RefundRequest> refunds = adminService.findRefundById(id);
//
//
//        if (refunds.isPresent()) {
//
//
//            RefundResponse responses = new RefundResponse();
//            responses.setRefundId(id);
//            responses.setStaffName(refunds.get().getSenderStaff().getUser().getFullName());
//            responses.setRecipientName(staffService.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName()).getUser().getFullName());
//            responses.setUserName(refunds.get().getUser().getFullName());
//            responses.setAmount(refunds.get().getAmount());
//            responses.setReason(refunds.get().getReason());
//            responses.setStatus("RECIEVED");
//
//
//            refunds.get().setRecipientStaff(staffService.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName()));
//            refunds.get().setStatus(responses.getStatus());
//            refunds.get().setUpdatedAt(new Date());
//            adminService.saveRefundRequest(refunds.get());
//
//
//            int userId = refunds.get().getUser().getUserId();
//            BigDecimal amount = refunds.get().getAmount();
//            String reason = refunds.get().getReason();
//            String batchId = payPalService.refundToCustomer(userId, amount, reason);
//            try {
//                Thread.sleep(30_000); // 30 giây
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException("Interrupted while waiting for PayPal payout", e);
//            }
//            String paymentStatus = payPalService.getPayoutStatus(batchId);
//            if("SUCCESS".equals(paymentStatus)){
//
//                //CREATE NOTIFICATION
//                NotificationTemplate notiTemplate = notificationService.findByNotiType("REFUND_RESOLVE");
//                Notification noti = new Notification();
//                noti.setNotificationTemplate(notiTemplate);
//                noti.setUser(refunds.get().getUser());
//                noti.setCreatedAt(new Date())   ;
//                noti.setMessage(refunds.get().getReason());
//                notificationService.save(noti);
//                //CREATE BILL
//                Bill bill = new Bill();
//                bill.setStatus("REFUNDED");
//                bill.setTotal(amount.doubleValue());
//                bill.setMethod("PAYPAL");
//                bill.setCurrency("USD");
//                bill.setRequestRescue(refunds.get().getRequestRescue());
//                bill.setCreatedAt(LocalDateTime.now());
//                bill.setAppFee(0);
//                billService.save(bill);
//
//
//            }
//            return ResponseEntity.status(200).body(responses);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
@PutMapping("/refunds/received/{id}")
public ResponseEntity<RefundResponse> received(
        @PathVariable("id") int id,
        @RequestBody String message,
        HttpServletRequest request) throws Exception {
    Optional<RefundRequest> refunds = adminService.findRefundById(id);

    int start = message.indexOf(":\"") + 2;
    int end = message.indexOf("\"", start);
    String value = message.substring(start, end);

    if (refunds.isPresent()) {
        int userId = refunds.get().getUser().getUserId();
        BigDecimal amount = refunds.get().getAmount();
        String reason = refunds.get().getReason();
        System.out.println(request);
        String batchId = payPalService.refundToCustomer(userId, amount, reason);
        try {
            Thread.sleep(30_000); // 30 giây
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for PayPal payout", e);
        }
        String paymentStatus = payPalService.getPayoutStatus(batchId);
        System.out.println(paymentStatus);
        if ("SUCCESS".equals(paymentStatus)) {
            //CREATE BILL
            Bill bill = new Bill();
            bill.setStatus("REFUNDED");
            bill.setTotal(amount.doubleValue());
            bill.setMethod("PAYPAL");
            bill.setCurrency("VND");
            bill.setRequestRescue(refunds.get().getRequestRescue());
            bill.setCreatedAt(LocalDateTime.now());
            bill.setAppFee(0);
            billRepository.save(bill);

            //CREATE NOTIFICATION
            NotificationTemplate notiTemplate = notificationTemplateRepository.findByNotiType("REFUND_RESOLVE");
            Notification noti = new Notification();
            noti.setNotificationTemplate(notiTemplate);
            noti.setUser(refunds.get().getUser());
            noti.setCreatedAt(new Date());
            noti.setMessage(value);
            notificationService.save(noti);

            RefundResponse responses = new RefundResponse();
            responses.setRefundId(id);
            responses.setStaffName(refunds.get().getSenderStaff().getUser().getFullName());
            responses.setRecipientName(staffService.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName()).getUser().getFullName());
            responses.setUserName(refunds.get().getUser().getFullName());
            responses.setAmount(refunds.get().getAmount());
            responses.setReason(refunds.get().getReason());
            responses.setStatus("RECIEVED");
            refunds.get().setRecipientStaff(staffService.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName()));
            refunds.get().setStatus(responses.getStatus());
            refunds.get().setUpdatedAt(new Date());
            adminService.saveRefundRequest(refunds.get());
            return ResponseEntity.status(200).body(responses);
        }
        return ResponseEntity.badRequest().build();

    } else {
        return ResponseEntity.notFound().build();
    }
}

    @GetMapping("/schedule")
    public ResponseEntity<List<ScheduleDto>> getAllSchedules(HttpServletRequest request){
        return ResponseEntity.status(200).body(scheduleServices.reloadSchedule(adminService.findAllShift()));
    }

    @PutMapping("/schedule/update/{id}")
    public ResponseEntity<List<ScheduleDto>> updateSchedule(@PathVariable Integer id, @RequestBody ScheduleDto dto) {
        adminService.updateShift(id, dto);
        return ResponseEntity.status(200).body(scheduleServices.reloadSchedule(adminService.findAllShift()));
    }

    @DeleteMapping("/schedule/delete/{id}")
    public ResponseEntity<List<ScheduleDto>> deleteSchedule(@PathVariable Integer id) {
        adminService.deleteShift(id);
        return ResponseEntity.status(200).body(scheduleServices.reloadSchedule(adminService.findAllShift()));
    }

    @DeleteMapping("/schedule/delete/{id}/{date}")
    public ResponseEntity<?> deleteOneScheduleByDate(
            @PathVariable int id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Optional<Shift> shiftOpt = adminService.findShiftById(id);
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Shift not found");
        }

        Optional<Schedule> schedules = adminService.findScheduleByShiftIdAndWorkingDate(id, date);
        if (schedules.isEmpty()) {
            CancelShift cs = new CancelShift();
            cs.setShift(shiftOpt.get());
            cs.setCanceledDate(date);
            adminService.saveCancelShift(cs);
            return ResponseEntity.ok("Save cancel shift");
        }

        adminService.deleteSchedule(schedules.get().getScheduleId()); // chỉ xóa đúng ngày đó
        return ResponseEntity.ok("Deleted schedule for " + date);
    }


    /// H ///
    //==================RESCUE INFO SECTION==================
    @GetMapping("/rescue-info")
    public List<RescueInfoDTO> getAllRescueInfo() {
        return requestRescueService.findAllRescueInfo();
    }

    @PutMapping("/rescue-info/update-status")
    @Transactional
    public void updateStatus(@RequestBody UpdateStatusRequest request) {
        requestRescueService.updateBillStatusByBillId(request.getBillId(), request.getStatus());
    }

    @GetMapping("/rescue-info/detail/{rrid}")
    public ResponseEntity<RescueDetailDTO> getRescueDetail(@PathVariable int rrid) {
        return requestRescueService.findRescueDetailByRRID(rrid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    /// T ///
    //==================FEEDBACK SECTION==================
    @GetMapping("/feedbacks")
    public ResponseEntity<List<FeedbackDto>> getFeedbacks() {
        return ResponseEntity.status(200).body(feedbackService.findAllDto());
    }
    //==================CUSTOMER SECTION==================
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getCustomers() {
        Role role = roleService.findByName("Customer");
        return ResponseEntity.status(200).body(userService.findByRole(role));
    }

    @GetMapping("/allcustomers")
    public ResponseEntity<List<UserDto>> getAllCustomersAndPartners() {
        Role customerRole = roleService.findByName("Customer");
        Role partnerRole = roleService.findByName("Partner");

        List<User> customers = userService.findByRole(customerRole);
        List<User> partners = userService.findByRole(partnerRole);

        List<UserDto> all = Stream.concat(customers.stream(), partners.stream())
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setUserId(user.getUserId());
                    dto.setFullName(user.getFullName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(all);
    }

    @GetMapping("/customers/search/{fullName}")
    public ResponseEntity<List<User>> getCustomersByName(@PathVariable("fullName") String fullName) {
        return ResponseEntity.status(200).body(userService.findByFullName(fullName));
    }
    //==================PARTNER SECTION==================
    @GetMapping("/partners")
    public ResponseEntity<List<PartnerDto>> getPartners() {
        List<PartnerDto> dtoList = partnerService.findAll()
                .stream()
                .map(PartnerMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/partners/search/{uName}")
    public ResponseEntity<List<Partner>> getPartnersByUser(@PathVariable("uName") String uName) {
        List<User> users = userService.findByFullName(uName);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        }
        System.out.println(users);
        List<Partner> partners = new ArrayList<>();
        for (User user : users) {
            Partner partner = partnerService.findByUser(user.getUserId());
            if (partner != null) {
                partners.add(partner);
            }
        }
        return ResponseEntity.status(200).body(partners);
    }
    //==================BILL SECTION==================
    @GetMapping("/bill/monthly-revenue")
    public ResponseEntity<?> getMonthlyRevenueByPartner(
            @RequestParam("partnerId") int partnerId,
            @RequestParam(value = "year", required = false) Integer year
    ) {
        try {
            List<MonthlyRevenueDto> result;

            if (year != null) {
                // Có truyền năm → lọc theo năm
                result = billService.getMonthlyRevenueByPartner(partnerId, year);
            } else {
                // Không truyền năm → lấy toàn bộ các năm
                result = billService.getAllMonthlyRevenueByPartner(partnerId);
            }

            return ResponseEntity.ok(ApiResponse.success(result, "Revenue loaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.errorServer("Error when loading revenue: " + e.getMessage())
            );
        }
    }

    //==================DASHBOARD SECTION==================
    @PostMapping("/dashboard/revenue")
    public ResponseEntity<ApiResponse<List<DailyRenvenueData>>> getRevenue(
            @RequestBody DataRangeRequest range
    ) {
        List<DailyRenvenueData> revenueList =
                dashboardService.getRevenueByDateRange(range.getStart(), range.getEnd());

        return ResponseEntity.ok(ApiResponse.success(revenueList, "Get revenue successfully"));
    }
    @GetMapping("/dashboard/rescue/daily")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDailyRescue(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Map<String, Long> stats = dashboardService.getDailyRescueStats(date);
        return ResponseEntity.ok(ApiResponse.success(stats, "Get daily rescue stats successfully"));
    }

    @GetMapping("/dashboard/rescue/range")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRescueByRange(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        Map<String, Long> stats = dashboardService.getRescueStatsInRange(start, end);
        return ResponseEntity.ok(ApiResponse.success(stats, "Get range rescue stats successfully"));
    }
    @GetMapping("/dashboard/revenue/total")

    public ResponseEntity<ApiResponse<Double>> getTotalRevenue() {
        Double total = dashboardService.getTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success(total, "Fetched total revenue successfully"));
    }
    @GetMapping("/dashboard/revenue/last-month")
    public ResponseEntity<ApiResponse<Double>> getLastRevenue() {
        Double revenue = dashboardService.getLastMonthRevenue();
        return ResponseEntity.ok(ApiResponse.success(revenue, "Fetched last month revenue successfully"));
    }

    @GetMapping("/dashboard/rescue/this-month")
    public ResponseEntity<ApiResponse<Long>> getThisMonthRescueTotal() {
        Long total = dashboardService.getTotalRescueThisMonth();
        return ResponseEntity.ok(ApiResponse.success(total, "Fetched this month's total rescue successfully"));
    }
    @GetMapping("/dashboard/rescue/last-month")
    public ResponseEntity<ApiResponse<Long>> getLastMonthRescueTotal() {
        Long total = dashboardService.getTotalRescueLastMonth();
        return ResponseEntity.ok(ApiResponse.success(total, "Fetched last month's total rescue successfully"));
    }
    @GetMapping("/dashboard/customer/this-month")
    public ResponseEntity<ApiResponse<Long>> getCustomerThisMonth() {
        Long total = dashboardService.countNewCustomersThisMonth();
        return ResponseEntity.ok(ApiResponse.success(total, "Fetched new customers in this month"));
    }

    @GetMapping("/dashboard/customer/last-month")
    public ResponseEntity<ApiResponse<Long>> getCustomerLastMonth() {
        Long total = dashboardService.countNewCustomersLastMonth();
        return ResponseEntity.ok(ApiResponse.success(total, "Fetched new customers in last month"));
    }
    @GetMapping("/dashboard/customer/returning-this-month")
    public ResponseEntity<ApiResponse<?>> getReturningCustomerCountThisMonth() {
        Long count = dashboardService.getReturningCustomerCountThisMonth();
        return ResponseEntity.ok(ApiResponse.success(count, "Get returning customer count successfully"));
    }

    @GetMapping("/dashboard/customer/returning-last-month")
    public ResponseEntity<ApiResponse<?>> getReturningCustomerCountLastMonth() {
        Long count = dashboardService.getReturningCustomerCountLastMonth();
        return ResponseEntity.ok(ApiResponse.success(count, "Get returning customer count last month successfully"));
    }

    //==================DISCOUNT SECTION==================
    //Get all discounts
    @GetMapping("/discount")
    public ResponseEntity<ApiResponse<List<DiscountDto>>> getAllDiscounts() {
        try {
            List<Discount> discounts = discountService.getDiscounts();
            List<DiscountDto> discountDtos = discounts.stream()
                    .map(discountMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(discountDtos, "Get all discounts successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error when get all discounts: " + e.getMessage()));
        }
    }

    //Get discount by id
    @GetMapping("/discount/{id}")
    public ResponseEntity<ApiResponse<DiscountDto>> getDiscountById(@PathVariable Integer id) {
        try {
            Optional<Discount> optionalDiscount = discountService.findDiscountById(id);
            if (optionalDiscount.isPresent()) {
                DiscountDto dto = discountMapper.toDto(optionalDiscount.get());
                return ResponseEntity.ok(ApiResponse.success(dto, "Get discount successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.errorServer("Discount not found: " + id));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error when get discount: " + e.getMessage()));
        }
    }

    // Save or update discount
    @PostMapping("/discount/save")
    public ResponseEntity<ApiResponse<DiscountDto>> saveDiscount(
            @Valid @RequestBody DiscountDto discountDto,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.toList());

                return ResponseEntity.badRequest()
                        .body(ApiResponse.errorValidation("Get errors from validate",errors));
            }

            Discount discount = discountMapper.toEntity(discountDto);
            Discount savedDiscount = discountService.saveDiscount(discount);
            DiscountDto responseDto = discountMapper.toDto(savedDiscount);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(responseDto, "Discount saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error when save discount: " + e.getMessage()));
        }
    }

    @PutMapping("/discount/{id}")
    public ResponseEntity<ApiResponse<DiscountDto>> updateDiscount(
            @PathVariable Integer id,
            @Valid @RequestBody DiscountDto discountDto,
            BindingResult bindingResult) {

        // Nếu validate lỗi, trả về luôn
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorValidation("Get errors from validate",errors));
        }

        try {
            // Kiểm tra tồn tại
            Optional<Discount> existingDiscountOpt = discountService.findDiscountById(id);
            if (existingDiscountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.errorServer("Discount not found: " + id));
            }

            Discount existingDiscount = existingDiscountOpt.get();

            // Cập nhật các field
            existingDiscount.setName(discountDto.getName());
            existingDiscount.setCode(discountDto.getCode());
            existingDiscount.setAmount(discountDto.getAmount());
            existingDiscount.setType(discountDto.getType());
            existingDiscount.setApplyDate(discountDto.getApplyDate());
            existingDiscount.setQuantity(discountDto.getQuantity());
            existingDiscount.setStatus(discountDto.getStatus());
            existingDiscount.setTypeDis(discountDto.getTypeDis());

            existingDiscount.setUpdatedAt(new Date());

            // Lưu lại
            Discount updatedDiscount = discountService.saveDiscount(existingDiscount);
            DiscountDto responseDto = discountMapper.toDto(updatedDiscount);

            return ResponseEntity.ok(ApiResponse.success(responseDto, "Discount updated successfully"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.errorServer("Duplicate discount code."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error when update discount: " + e.getMessage()));
        }
    }

    //Search Discounts
    @GetMapping("/discount/search")
    public ResponseEntity<ApiResponse<List<Discount>>> searchDiscounts(@RequestParam String name) {
        try{
            List<Discount> discounts = discountService.searchDiscounts(name);
            return ResponseEntity.ok(ApiResponse.success(discounts, "Search discount successfully"));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error when search discounts: " + e.getMessage()));
        }
    }

    //==================DOCUMENTARY SECTION==================
    @PostMapping("/documentary/add")
    public ResponseEntity<ApiResponse<?>> addDocumentary(
            @ModelAttribute DocumentaryDto dto, // nhận toàn bộ DTO
            @RequestParam(required = false) MultipartFile frontImage,
            @RequestParam(required = false) MultipartFile backImage
    ) {
        try {
            var doc = documentaryService.addDocumentary(dto, frontImage, backImage);

            return ResponseEntity.ok(
                    ApiResponse.success(doc.getDocumentId(), "Created successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.errorServer("Error: " + e.getMessage())
            );
        }
    }

    @GetMapping("/documentary")
    public ResponseEntity<ApiResponse<List<DocumentaryDto>>> getAllDocumentaries() {
        return ResponseEntity.ok(ApiResponse.success(documentaryService.getAllDecrypted(), "List fetched"));
    }

    @GetMapping("/documentary/{id}")
    public ResponseEntity<ApiResponse<?>> getDocumentariesById(@PathVariable int id) {
        Optional<DocumentaryDto> optionalDoc = documentaryService.getDecryptedById(id);

        if (optionalDoc.isPresent()) {
            ApiResponse<DocumentaryDto> response = ApiResponse.success(optionalDoc.get(), "Found");
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<?> notFoundResponse = ApiResponse.notfound(null, "Not found");
            return ResponseEntity.status(404).body(notFoundResponse);
        }
    }

    @GetMapping("/documentary/by-partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<DocumentaryDto>>> getByPartnerId(@PathVariable int partnerId) {
        return ResponseEntity.ok(ApiResponse.success(documentaryService.getByPartnerId(partnerId), "Found"));
    }
    @GetMapping("/documentary/image")
    public ResponseEntity<byte[]> getDocumentariesImage(@RequestParam String path) {
        try {
            byte[] data = documentaryService.getDecryptedImage(path);

            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(data);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    //==================PAYMENT SECTION==================
    @GetMapping("/payment/customers/{customerId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getCustomerPaymentsByCustomerId(
            @PathVariable Integer customerId) {
        try {
            List<Payment> payments = paymentService.getCustomerPaymentsByCustomerId(customerId);
            List<PaymentDto> dtos = paymentMapper.mapToPaymentDto(payments);
            return ResponseEntity.ok(ApiResponse.success(dtos, "List payments of customer ID: " + customerId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error getting payment list for customer ID: " + customerId));
        }
    }

    @GetMapping("/payment/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPartnerPayments(@PathVariable Integer partnerId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByPartnerId(partnerId);
            List<PaymentDto> dtos = paymentMapper.mapToPaymentDto(payments);
            return ResponseEntity.ok(ApiResponse.success(dtos, "List all payments of partner"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error getting payment list!"));
        }
    }

    //==================PERSONAL DATA SECTION==================
    //  Add new personal data
    @PostMapping("/personaldoc/add")
    public ResponseEntity<ApiResponse<?>> addPersonalData(
            @RequestParam String citizenNumber,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date expirationDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date issueDate,
            @RequestParam String verificationStatus,
            @RequestParam String issuePlace,
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile frontImage,
            @RequestParam(required = false) MultipartFile backImage,
            @RequestParam(required = false) MultipartFile faceImage
    ) {
        try {
            PersonalData pd = personalDataService.addPersonalData(
                    citizenNumber, expirationDate, issueDate,
                    verificationStatus, issuePlace, type,
                    frontImage, backImage, faceImage
            );
            return ResponseEntity.ok(ApiResponse.success(
                    pd.getPdId(),
                    "Add personal data successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.errorServer("Lỗi: " + e.getMessage())
            );
        }
    }

    //  Get all decrypted
    @GetMapping("/personaldoc")
    public ResponseEntity<ApiResponse<List<PersonalDataDto>>> getAllPersonalData() {
        List<PersonalDataDto> list = personalDataService.getAllDecrypted();
        return ResponseEntity.ok(ApiResponse.success(list, "Get all personal data successful"));
    }

    //  Get by ID
    @GetMapping("/personaldoc/{id}")
    public ResponseEntity<ApiResponse<?>> getPersonalDataById(@PathVariable int id) {
        Optional<PersonalDataDto> optional = personalDataService.getDecryptedById(id);
        return optional
                .<ResponseEntity<ApiResponse<?>>>map(dto -> ResponseEntity.ok(
                        ApiResponse.success(dto, "Get personal data successful")))
                .orElseGet(() -> ResponseEntity.status(404).body(
                        ApiResponse.notfound(null, "Not found personal data")
                ));
    }

    //  Get by UserID
    @GetMapping("/personaldoc/by-user/{userId}")
    public ResponseEntity<ApiResponse<?>> getPersonalDataByUser(@PathVariable int userId) {
        try {
            List<PersonalDataDto> dataList = personalDataService.getPersonalDataByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(dataList, "Get personal data successful"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.errorServer("Error: " + e.getMessage())
            );
        }
    }

    //  Lấy ảnh đã giải mã
    @GetMapping("/personaldoc/image")
    public ResponseEntity<byte[]> getPersonalDataImage(@RequestParam String path) {
        try {
//            byte[] data = personalDataService.getDecryptedImage(path);
            Path fullPath = Paths.get(path);
            byte[] data = Files.readAllBytes(fullPath);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //==================REPORT SECTION==================
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<ReportDto>>> getReports() {
        try {
            List<Report> reports = reportService.showAllReport();
            reports.forEach(report -> {
                if (report.isWithin24H()) {
                    long diff = new Date().getTime() - report.getCreatedAt().getTime();
                    if (diff > 24 * 60 * 60 * 1000) {
                        report.setWithin24H(false);
                        reportService.save(report);
                    }
                }
            });
            List<ReportDto> dtos = reports.stream()
                    .sorted((r1, r2) -> {
                        // Ưu tiên with24h == true lên đầu
                        int cmp = Boolean.compare(r2.isWithin24H(), r1.isWithin24H());
                        if (cmp != 0) return cmp;
                        // Nếu bằng nhau, sắp theo createdAt mới nhất lên đầu
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    })
                    .map(ReportDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos, "Get all report successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error getting reports!"));
        }
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<ApiResponse<Report>> getReportById(@PathVariable  Integer id) {
        try{
            Optional<Report> report = Optional.ofNullable(reportService.getReportById(id));
            if(report.isPresent()){
                return ResponseEntity.ok(ApiResponse.success(report.get(), "Get report successfully!"));
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.errorServer("Error getting report!"));
            }
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error getting report!" +e.getMessage()));
        }
    }

    @PutMapping("/report/{id}/resolve")
    public ResponseEntity<ApiResponse<ReportDto>> resolveReport(
            @PathVariable Integer id,
            @Valid @RequestBody ReportResolveDto reportResolveDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorValidation("Validation failed when resolving report", errors));
        }

        try {
            ReportDto updatedDto = reportService.resolveReport(id, reportResolveDto);
            return ResponseEntity.ok(ApiResponse.success(updatedDto, "Report resolved successfully!"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorServer(ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Unexpected error while resolving report!"));
        }
    }
    @GetMapping("/report/users/search")
    public ResponseEntity<ApiResponse<List<UserSearchDto>>> searchReportByUsers(@RequestParam String keyword) {
        List<UserSearchDto> users = userService.findUsersByUsername(keyword);

        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorServer("No users found with keyword: " + keyword));
        }

        List<UserSearchDto> dtos = users.stream()
                .map(user -> new UserSearchDto(user.getUserId(), user.getFullName(), user.getUsername()))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos, "User search successful"));
    }

    @GetMapping("/report/partners/search")
    public ResponseEntity<ApiResponse<List<PartnerSearchDto>>> searchReportByPartners(@RequestParam String keyword) {
        List<Partner> partners = partnerService.searchByUsernameContainingIgnoreCase(keyword);

        if (partners == null || partners.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorServer("No partners found with keyword: " + keyword));
        }

        List<PartnerSearchDto> dtos = partners.stream()
                .map(p -> new PartnerSearchDto(
                        p.getUser().getFullName(),
                        p.getUser().getUsername(),
                        p.getPartnerId()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos, "Partner search successful"));
    }

    @GetMapping("/report/filter/status")
    public ResponseEntity<ApiResponse<List<Report>>> filterByStatus(@RequestParam("status") String status) {
        try {
            List<Report> reports = reportService.findByStatusIgnoreCase(status);
            if (reports == null || reports.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.errorServer("No reports found with status: " + status));
            }

            return ResponseEntity.ok(ApiResponse.success(reports, "Filtered reports by status successfully!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error filtering reports: " + e.getMessage()));
        }
    }

    @GetMapping("/report/staff/{staffid}")
    public ResponseEntity<ApiResponse<List<Report>>> getReportsByStaff(@PathVariable Integer staffid) {
        try{
            List<Report> reports = reportService.findByStaff_id(staffid);
            if (reports == null || reports.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.errorServer("No reports found with staffid: " + staffid));
            }

            List<ReportDto> dtos = reports.stream().map(ReportDto::new).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(reports, "Get all reports successfully!"));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error filtering reports: " + e.getMessage()));
        }
    }
    @GetMapping("/report/by-partner")
    public ResponseEntity<?> getReportsByPartnerId(@RequestParam("partnerId") Integer partnerId) {
        try {
            List<Report> reports = reportService.getReportsByPartnerId(partnerId);

            List<ReportDto> reportDtos = reports.stream()
                    .map(ReportDto::new) // dùng constructor ReportDto(Report)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(reportDtos, "Reports fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Failed to fetch reports"));
        }
    }

    //==================SERVICE SECTION==================
    @GetMapping("/service")
    public ResponseEntity<ApiResponse<List<Services>>> showAllServices() {
        try{
            List<Services> services = servicesService.findAll();
            return ResponseEntity.ok(ApiResponse.success(services,"Get All Services Successfully!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error getting services!"));
        }
    }

    @GetMapping("/service/{id}")
    public ResponseEntity<ApiResponse<Services>> getService(@PathVariable Integer id) {
        try{
            Optional<Services> service = Optional.ofNullable(servicesService.findById(id));
            if (service.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(service.get(), "Get Service Successfully!"));
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.errorServer("Service not found: " + id));
            }
        }catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error getting service!"));
        }
    }

    @PutMapping("/service/{id}/update-price")
    public ResponseEntity<ApiResponse<Services>> updatePrices(
            @PathVariable int id,
            @RequestBody UpdatePriceRequest request
    ) {
        try {
            Services updated = servicesService.updatePrices(id, request.getFixedPrice(), request.getPricePerKm());
            if (updated != null) {
                return ResponseEntity.ok(ApiResponse.success(updated, "Updated service prices successfully!"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.errorServer("Service not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error updating service prices!"));
        }
    }

    @GetMapping("/service/search/{name}")
    public ResponseEntity<ApiResponse<List<Services>>> searchByName(@PathVariable String name) {
        try {
            List<Services> results = servicesService.searchByName(name);
            return ResponseEntity.ok(ApiResponse.success(results, "Search results for name: " + name));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error searching service!"));
        }
    }

    @GetMapping("/service/filter/{type}")
    public ResponseEntity<ApiResponse<List<Services>>> filterByType(@PathVariable String type) {
        try {
            List<Services> results = servicesService.filterByType(type);
            return ResponseEntity.ok(ApiResponse.success(results, "Filter results for type: " + type));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error filtering service!"));
        }
    }

    //==================USER SECTION==================
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Integer id) {
        User user = userService.findById(id).orElseThrow();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.errorServer("User not found with ID: " + id));
        }

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());

        return ResponseEntity.ok(ApiResponse.success(dto, "User fetched successfully"));
    }

    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getUserTransactions(@PathVariable Integer userId) {
        User user = userService.findById(userId).orElseThrow();

        List<Bill> bills = billService.findBillsByUserAndStatus(userId, "Completed", "COMPLETED");

        List<TransactionDto> dtos = bills.stream().map(bill -> {
            Payment payment = bill.getPayment();

            return new TransactionDto(
                    bill.getBillId(),
                    bill.getServicePrice(),
                    bill.getDistancePrice(),
                    bill.getExtraPrice(),
                    bill.getDiscountAmount(),
                    bill.getTotalPrice(),
                    bill.getTotal(),
                    payment != null ? payment.getMethod() : null,
                    bill.getCreatedAt(),
                    bill.getStatus(),
                    bill.getRequestRescue() != null ? bill.getRequestRescue().getRescueType() : null,
                    bill.getRequestRescue() != null ? bill.getRequestRescue().getStartTime() : null,
                    bill.getRequestRescue() != null ? bill.getRequestRescue().getEndTime() : null
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "User fetched successfully"));
    }
    @GetMapping("/users/{userId}/reports")
    public ResponseEntity<ApiResponse<List<ReportDto>>> getReportsByUserId(@PathVariable Integer userId) {
        List<Report> reports = reportService.getReportsByDefendantCustomer(userId); // Hoặc query phù hợp

        List<ReportDto> dtos = reports.stream().map(report -> {
            ReportDto dto = new ReportDto();

            dto.setName(report.getName());
            dto.setStatus(report.getStatus());
            dto.setCreatedAt(report.getCreatedAt());
            dto.setComplainantName(report.getComplainantCustomer() != null
                    ? report.getComplainantCustomer().getFullName() : null);
            dto.setDefendantName(report.getDefendantCustomer() != null
                    ? report.getDefendantCustomer().getFullName() : null);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "Reports fetched successfully"));
    }

//    @PutMapping("/users/{userId}/block")
//    public ResponseEntity<ApiResponse<String>> blockUser(@PathVariable Integer userId) {
//        userService.blockUser(userId);
//        return ResponseEntity.ok(ApiResponse.ok("User blocked successfully"));
//    }

    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponse<String>> blockUser(@PathVariable Integer id) {
        userService.updateUserStatus(id, "BLOCKED", null);
        notificationService.notifyUser(
                id,
                "Your account has been permanently blocked",
                "Your account has been permanently blocked due to violations of our policy.",
                "BLOCKED"
        );
        return ResponseEntity.ok(ApiResponse.ok("User blocked permanently"));
    }

    // Block 24h
    @PutMapping("/{id}/block-24h")
    public ResponseEntity<ApiResponse<String>> blockUser24h(@PathVariable Integer id) {
        LocalDateTime blockUntil = LocalDateTime.now().plusHours(24);
        userService.updateUserStatus(id, "BLOCKED_24H", blockUntil);
        notificationService.notifyUser(
                id,
                "Your account has been temporarily blocked",
                "Your account has been blocked for 24 hours until " + blockUntil + ".",
                "BLOCKED_24H"
        );
        return ResponseEntity.ok(ApiResponse.ok("User blocked for 24 hours"));
    }


    //==================VEHICLE SECTION==================
    @GetMapping("/vehicle/partnerVehicle/{partnerId}")
    public ResponseEntity<?> getVehiclesPartnerVehicle(@PathVariable int partnerId) {
        try{
            return ResponseEntity.ok(vehicleService.findPartnerVehicles(partnerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }
    @PostMapping("/vehicle/add")
    public ResponseEntity<ApiResponse<?>> addVehicle(
            @RequestParam int userId,
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam int year,
            @RequestParam(required = false) String vehicleStatus,
            @RequestParam(required = false) MultipartFile frontImage,
            @RequestParam(required = false) MultipartFile backImage,
            @RequestParam(required = false) MultipartFile imgTem,
            @RequestParam(required = false) MultipartFile imgTool,
            @RequestParam(required = false) MultipartFile imgDevice
    ) {
        try {
            var vehicle = vehicleService.addVehicle(userId, brand, model, year, vehicleStatus,
                    frontImage, backImage, imgTem, imgTool, imgDevice);
            return ResponseEntity.ok(ApiResponse.success(vehicle.getVehicleId(), "Vehicle created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.errorServer("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/vehicle")
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getAllVehicles() {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getAllVehicles(), "List fetched"));
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<ApiResponse<VehicleDto>> getVehiclesById(@PathVariable int id) {
        return vehicleService.getById(id)
                .map(v -> ResponseEntity.ok(ApiResponse.success(v, "Found")))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.notfound(null, "Not found")));
    }

    @GetMapping("/vehicle/by-user/{userId}")
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehiclesByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getByUserId(userId), "Found"));
    }

    @GetMapping("/vehicle/by-partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehiclesByPartnerId(@PathVariable int partnerId) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getByPartnerId(partnerId), "Found"));
    }
    @GetMapping("/vehicle/image")
    public ResponseEntity<byte[]> getVehiclesImage(@RequestParam String path) {
        try {
            byte[] data = vehicleService.getDecryptedImage(path);

            // Xác định loại file từ path (file thật, đã giải mã sẵn)
            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(data);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /// P ///
    //==================FEEDBACK SECTION==================
    @GetMapping("/feedbacks/searchFeedbackByRR/{rrId}")
    public ResponseEntity<FeedbackDto> searchFeedbackByRR(@PathVariable("rrId") int rrId) {
        return ResponseEntity.status(200).body(feedbackService.searchByRRid(rrId));
    }

    @GetMapping("/feedbacks/searchFeedbackByPartner/{partnerId}")
    public ResponseEntity<List<FeedbackDto>> searchFeedbackByPartner(@PathVariable("partnerId") int partnerId) {
        return ResponseEntity.status(200).body(feedbackService.searchByPartner(partnerId));
    }

    @GetMapping("/feedbacks/averageRate/{partnerId}")
    public ResponseEntity<Double> averageRate(@PathVariable("partnerId") int partnerId) {
        return ResponseEntity.status(200).body(feedbackService.averageRate(partnerId));
    }

    //==================USER SECTION==================
    //--CUSTOMER--//
    @GetMapping("/customers/dto")
    public ResponseEntity<List<UserDto>> getAllCustomers() {
        return ResponseEntity.status(200).body(customerService.findAllCustomers());
    }

    @GetMapping("/customer/searchCustomerById/{customerId}")
    public ResponseEntity<UserDto> searchCustomerById(@PathVariable("customerId") int customerId) {
        return ResponseEntity.status(200).body(customerService.searchCustomerById(customerId));
    }

    @GetMapping("/customers/searchCustomers/{keyword}")
    public ResponseEntity<List<UserDto>> searchCustomers(@PathVariable("keyword") String keyword) {
        return ResponseEntity.status(200).body(customerService.searchCustomers(keyword));
    }

    @GetMapping("/customers/customerDashboard/{userId}")
    public ResponseEntity<UserDashboard> customerDashboard(@PathVariable("userId") int userId) {
        return ResponseEntity.status(200).body(customerService.customerDashboard(userId));
    }

    @PostMapping("/customers/createCustomer")
    public ResponseEntity<?> createCustomer(
            @RequestPart String userDtoString,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserDto userDto = mapper.readValue(userDtoString, UserDto.class);
            Map<String, String> errors = validateUserDto(userDto);
            String existedMessage = userExists(userDto);
            if(existedMessage != null){
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflictData(null, existedMessage));
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ResponseEntity.ok(customerService.createNew(userDto, avatar));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }

    @GetMapping("/staffs-dto")
    public ResponseEntity<List<StaffDto>> getStaffsDto() {
        return ok(staffService.findAllStaffs());
    }

    @GetMapping("/staffs/searchStaffs/{keyword}")
    public ResponseEntity<List<StaffDto>> searchStaffs(@PathVariable("keyword") String keyword) {
        return ok(staffService.searchStaffs(keyword));
    }

    @PostMapping("/staffs/createStaff")
    public ResponseEntity<?> createStaff(
            @RequestPart String userDtoString,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserDto userDto = mapper.readValue(userDtoString, UserDto.class);
            Map<String, String> errors = validateUserDto(userDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            String existedMessage = userExists(userDto);
            if (existedMessage != null) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflictData(null, existedMessage));
            }
            return ResponseEntity.ok(staffService.createNewStaff(userDto, avatar));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating staff: " + e.getMessage());
        }
    }



    @PutMapping("/staffs/{staffId}")
    public ResponseEntity<?> updateStaff(
            @PathVariable int staffId,
            @RequestPart String userDtoString,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            UserDto userDto = mapper.readValue(userDtoString, UserDto.class);
            if (userDto.getUserId() == 0) {
                userDto.setUserId(staffId);
            }
            Optional<User> oldUserOpt = userService.findById(staffId);
            if (oldUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cannot fidn user with ID: " + staffId);
            }
            if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
                userDto.setPassword(oldUserOpt.get().getPassword());
            }
            System.out.println(oldUserOpt.get().getPassword());
            System.out.println(userDto.getPassword());
            Map<String, String> errors = validateUserDto(userDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            UserDto updated = userService.updateStaff(userDto, avatar);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating staff: " + e.getMessage());
        }
    }

    @GetMapping("/managers-dto")
    public ResponseEntity<List<StaffDto>> getManagers() {
        return ok(managerService.findAllManagers());
    }

    @GetMapping("/managers/searchManagers/{keyword}")
    public ResponseEntity<List<StaffDto>> searchManagers(@PathVariable("keyword") String keyword) {
        return ok(managerService.searchManagers(keyword));
    }

    @PostMapping("/managers/createManager")
    public ResponseEntity<?> createNewManager(
            @RequestPart String userDtoString,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserDto userDto = mapper.readValue(userDtoString, UserDto.class);
            Map<String, String> errors = validateUserDto(userDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            String existedMessage = userExists(userDto);
            if(existedMessage != null && !existedMessage.isEmpty()){
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflictData(null, existedMessage));
            }

            Staff newManager = managerService.createNew(userDto, avatar);
            return ResponseEntity.ok(newManager);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating manager: " + e.getMessage());
        }
    }

    @PutMapping("/managers/{managerId}")
    public ResponseEntity<?> updateManager(
            @PathVariable int managerId,
            @RequestPart String userDtoString,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            UserDto userDto = mapper.readValue(userDtoString, UserDto.class);
            if (userDto.getUserId() == 0) {
                userDto.setUserId(managerId);
            }
            Optional<User> oldUserOpt = userService.findById(managerId);
            if (oldUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cannot fidn user with ID: " + managerId);
            }
            if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
                userDto.setPassword(oldUserOpt.get().getPassword());
            }
            Map<String, String> errors = validateUserDto(userDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            UserDto updated = userService.updateStaff(userDto, avatar);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating manager: " + e.getMessage());
        }
    }
    //==================PARTNER SECTION==================
    @GetMapping("/partners/searchPartners/{keyword}")
    public ResponseEntity<List<PartnerDto>> searchPartners(@PathVariable("keyword") String keyword) {
        return ok(partnerService.searchPartners(keyword));
    }

    @GetMapping("/partners/findPartnerById/{partnerId}")
    public ResponseEntity<Optional<PartnerDto>> findPartnerById(@PathVariable("partnerId") int partnerId) {
        return ok(partnerService.findPartnerById(partnerId));
    }

    @GetMapping("/partners/partnerDashboard/{partnerId}")
    public ResponseEntity<UserDashboard> partnerDashboard(@PathVariable("partnerId") int partnerId) {
        return ok(partnerService.partnerDashboard(partnerId));
    }

    @GetMapping("/partners/approvePartner/{partnerId}")
    public ResponseEntity<Boolean> approvePartner(@PathVariable("partnerId") int partnerId) {
        return ok(partnerService.approvePartner(partnerId));
    }

    //==================SERVICE SECTION==================
    @GetMapping("/services/searchBySrvType/{keyword}")
    public ResponseEntity<List<ServiceDto>> searchBySrvType(@PathVariable("keyword") String keyword) {
        return ok(servicesService.findByServiceType(keyword));
    }

    //==================EXTRA SERVICE SECTION==================
    @GetMapping("/extraSrv/searchByReqResQ/{rrId}")
    public ResponseEntity<ExtraService> getExtraSrvByReqResQ(@PathVariable("rrId") int rrId) {
        return ok(extraServiceSrv.findExtraSrvByReqResQ(rrId));
    }

    //==================REQUEST RESQUE SECTION==================
    @GetMapping("/payment/refundPayment/{refundId}")
    public ResponseEntity<?> refundPayment(@PathVariable int refundId) {
        try {
            boolean hasPayment = paymentService.getRefundPayment(refundId);
            return ResponseEntity.ok(hasPayment);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.errorServer("Error: " + e.getMessage()));
        }
    }
    @GetMapping("/reqResQs")
    public ResponseEntity<List<RequestResQDto>> getReqResQs() {
        return ok(requestRescueService.findAll());
    }

    @GetMapping("/reqResQs/{rrId}")
    public ResponseEntity<Optional<RequestResQDto>> findById(@PathVariable("rrId") int rrId) {
        return ok(requestRescueService.findById(rrId));
    }

    @GetMapping("/reqResQs/searchByUser/{userId}")
    public ResponseEntity<List<RequestResQDto>> searchRRByUser(@PathVariable("userId") int userId) {
        return ok(requestRescueService.searchByUser(userId));
    }

    @GetMapping("/reqResQs/searchByPartner/{partId}")
    public ResponseEntity<List<RequestResQDto>> searchByPartner(@PathVariable("partId") int partId) {
        return ok(requestRescueService.searchByPartner(partId));
    }

    @GetMapping("/reqResQs/searchRequestResQ/{keyword}")
    public ResponseEntity<List<RequestResQDto>> searchRequestResQ(@PathVariable("keyword") String keyword) {
        return ok(requestRescueService.searchRR(keyword));
    }

    @GetMapping("/reqResQs/searchWithUser/{userId}/{keyword}")
    public ResponseEntity<List<RequestResQDto>> searchRequestResQWithUser(@PathVariable("userId") int userId, @PathVariable("keyword") String keyword) {
        return ok(requestRescueService.searchRRWithUser(userId, keyword));
    }

    @GetMapping("/reqResQs/searchWithPartner/{partId}/{keyword}")
    public ResponseEntity<List<RequestResQDto>> searchRequestResQWithPartner(@PathVariable("partId") int partId, @PathVariable("keyword") String keyword) {
        return ok(requestRescueService.searchRRWithPartner(partId, keyword));
    }

    @GetMapping("/reqResQs/existedRecords/{rrId}")
    public ResponseEntity<RecordStatusDto> checkIsExistedRecords(@PathVariable("rrId") int rrId) {
        return ok(requestRescueService.existedRecords(rrId));
    }

    @PostMapping("/reqResQs/createRequest")
    public ResponseEntity<?> createRequest(
            @RequestPart String requestDtoString,
            @RequestPart(required = false) String selectedServices) {
        try{
            System.out.println(selectedServices);
            ObjectMapper mapper = new ObjectMapper();
            RequestResQDto dto = mapper.readValue(requestDtoString, RequestResQDto.class);
            Map<String, String> errors = validateRequestDto(dto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            RequestRescue requestRescue = requestRescueService.createNew(dto);
            List<Integer> serviceIds = new ArrayList<>();
            if (selectedServices != null && !selectedServices.isEmpty()) {
                serviceIds = mapper.readValue(selectedServices, new TypeReference<List<Integer>>() {});
            }
            requestSrvService.createRequestServices(serviceIds,requestRescue);
            return ResponseEntity.ok(requestRescue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating request: " + e.getMessage());
        }
    }

    @PutMapping("/reqResQs/{requestId}")
    public ResponseEntity<?> updateManager(
            @PathVariable int requestId,
            @RequestPart String requestDtoString) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            RequestResQDto resQDto = mapper.readValue(requestDtoString, RequestResQDto.class);
            if (resQDto.getRrid() == 0) {
                resQDto.setRrid(requestId);
            }
            Map<String, String> errors = validateRequestDto(resQDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            RequestRescue updated = requestRescueService.updateRequest(resQDto);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating request: " + e.getMessage());
        }
    }

    //RequestServices
    @GetMapping("/requestServices/getByResquest/{rrid}")
    public ResponseEntity<List<RequestService>> getReqSrvByResquest(@PathVariable("rrid") int rrid) {
        return ok(requestSrvService.getReqSrvByResquest(rrid));
    }


    //Document
    @GetMapping("/documents/getUnverifiedPartnerDoc/{partnerId}")
    public ResponseEntity<List<Documentary>> getUnverifiedPartnerDoc(@PathVariable("partnerId") int partnerId) {
        return ok(documentaryService.getUnverifiedPartnerDoc(partnerId));
    }

    @PutMapping("/documents/updatePartnerDoc/{partnerId}")
    public ResponseEntity<?> updatePartnerDoc(@PathVariable("partnerId") int partnerId,
                                              @RequestBody VerifiedUserDto rejectData) {
        try {
            Map<String, String> errors = new HashMap<>();
            System.out.println("Reason:"+rejectData.getReason());
            if(rejectData.getReason() == null || rejectData.getReason().trim().isEmpty()) {
                errors.put("reason","Reason is required!");
            }
            if(rejectData.getDocumentTypes().size() <= 0) {
                errors.put("selectedDocuments","Please select at least one invalid document!");
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            documentaryService.rejectPartner(rejectData.getDocumentTypes(), partnerId, rejectData.getReason());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Err"+e.getMessage());
        }
    }

    //Personal Data
    @GetMapping("/personalDatas/getUnverifiedUserData/{customerId}")
    public ResponseEntity<PersonalData> getUnverifiedUserData(@PathVariable("customerId") int customerId) {
        return ok(personalDataService.getUnverifiedUserData(customerId));
    }

    @PutMapping("/personalDatas/approvedCustomer/{customerId}")
    public ResponseEntity<?> approveCustomer(@PathVariable("customerId") int customerId) {
        try{
            return ok(personalDataService.approvedCustomer(customerId));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Err"+e.getMessage());
        }
    }

    @PutMapping("/personalDatas/rejectedCustomer/{customerId}")
    public ResponseEntity<?> rejectCustomer(@PathVariable("customerId") int customerId,
                                            @RequestBody VerifiedUserDto rejectData) {
        try{
            Map<String, String> errors = new HashMap<>();
            if(rejectData.getReason() == null || rejectData.getReason().trim().isEmpty()) {
                errors.put("reason","Reason is required!");
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            personalDataService.rejectedCustomer(customerId, rejectData.getReason());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Err"+e.getMessage());
        }

    }

    //Payment
    @GetMapping("/payments/getCustomerPayments/{customerId}")
    public ResponseEntity<List<PaymentDto>> getCustomerPayments(@PathVariable("customerId") int customerId) {
        return ok(paymentService.customerPayments(customerId));
    }

    //==================SUPPORT METHOD==================
    //Support
    private <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    //Validation
    private Map<String, String> validateUserDto(UserDto dto) {
        Map<String, String> errors = new LinkedHashMap<>();

        // Full name
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            errors.put("fullName", "Full name is required");
        } else if (dto.getFullName().trim().length() < 5){
            errors.put("fullName", "Full name must be at least 5 characters");
        } else if (!dto.getFullName().matches("^[A-Za-zÀ-ỹà-ỹ\\s]+$")) {
            errors.put("fullName", "Full name must not contain numbers or special characters");
        }

        // Username
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            errors.put("userName", "Username is required");
        }else if(!dto.getUsername().matches("^[a-zA-Z0-9]+$")) {
            errors.put("userName", "Username must not contain whitespace or special characters");
        }

        //Address
        if(dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
            errors.put("address", "Address is required");
        }

        // Email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!dto.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            errors.put("email", "Invalid email format");
        }

        // Phone number
        if (dto.getSdt() == null || dto.getSdt().trim().isEmpty()) {
            errors.put("sdt", "Phone number is required");
        } else if (!dto.getSdt().matches("^(0[0-9]{9})$")) {
            errors.put("sdt", "Phone number must start with 0 and contain 9 digits");
        }

        // Password
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        } else if (!dto.getPassword().matches("^.{8,}$")) {
            errors.put("password", "Password must have at least 8 characters");
        }

        return errors;
    }

    private String userExists(UserDto dto) {
        List<User> users = userService.findAll();
        for (User user : users) {
            if (Objects.equals(user.getSdt(), dto.getSdt())) {
                return "User with this phone number already exists";
            }
            if (Objects.equals(user.getUsername(), dto.getUsername())) {
                return "User with this username already exists";
            }
            if (Objects.equals(user.getEmail(), dto.getEmail())) {
                return "User with this email already exists";
            }
            if (Objects.equals(user.getAddress(), dto.getAddress())) {
                return "User with this address already exists";
            }
        }
        return null;
    }




    private Map<String, String> validateRequestDto(RequestResQDto dto) {
        Map<String, String> errors = new LinkedHashMap<>();
        if(dto.getCustomerId() <= 0) {
            errors.put("customerId", "Customer is required");
        }
        if(dto.getULocation() == null || dto.getULocation().trim().isEmpty()) {
            errors.put("ulocation", "Customer's location is required");
        }
        if(dto.getRescueType() == null || dto.getRescueType().trim().isEmpty()) {
            errors.put("rescueType", "Service is required");
        }
        if(!"resFix".equalsIgnoreCase(dto.getRescueType()) && (dto.getDestination() == null || dto.getDestination().trim().isEmpty())) {
            errors.put("destination", "Destination is required");
        }
        return errors;
    }
}
