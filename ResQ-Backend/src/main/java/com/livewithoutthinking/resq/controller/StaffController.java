package com.livewithoutthinking.resq.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.mapper.PartnerMapper;
import com.livewithoutthinking.resq.mapper.ReportMapper;
import com.livewithoutthinking.resq.repository.RescueHistoryRepository;
import com.livewithoutthinking.resq.repository.StaffRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.service.*;
import com.livewithoutthinking.resq.service.PartnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/resq/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ScheduleServices scheduleServices;
    @Autowired
    private RoleService roleService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RequestSrvService requestSrvService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ServicesService servicesService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RequestRescueService requestRescueService;

    //==================REFUND SECTION==================
    @PostMapping("/refunds/save")
    public ResponseEntity<RefundRequest> saveRefund (
            @RequestBody RefundRequestDto dto,
            HttpServletRequest request ){

        RequestRescue rr = staffService.findRRById(dto.getRrid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu hoàn tiền"));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setRequestRescue(rr);
        refundRequest.setSenderStaff(staffService.findByUser_Username(SecurityContextHolder.getContext().getAuthentication().getName()));
        refundRequest.setUser(userService.findById(dto.getUserId()).orElse(null));
        refundRequest.setAmount(dto.getAmount());
        refundRequest.setReason(dto.getReason());
        refundRequest.setCreatedAt(new Date());
        refundRequest.setUpdatedAt(new Date());
        refundRequest.setStatus("PENDING");

        return ResponseEntity.status(201).body(staffService.saveRR(refundRequest));
    }

    //==================SCHEDULE SECTION==================
    @GetMapping("/schedule")
    public ResponseEntity<List<ScheduleDto>> getSchedule() {
        return ResponseEntity.status(200).body(scheduleServices.reloadSchedule(staffService.findMineShift()));
    }

    //==================REPORT SECTION==================
    @PostMapping("/report/create")
    public ResponseEntity<ApiResponse<?>> createReport(
            @Valid @ModelAttribute ReportDto reportDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(ApiResponse.errorValidation("Validation failed", errors));
        }

        try {
            reportDto.setStatus("pending");

            Report report = reportMapper.toEntity(reportDto);

            Staff staff = staffService.findById(reportDto.getStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found with id: " + reportDto.getStaffId()));
            report.setStaff(staff);

            Report reportCreated = reportService.addNewReport(report);

            ReportDto dto = new ReportDto(reportCreated);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(dto, "Report created successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Error creating report! " + e.getMessage()));
        }
    }

    @GetMapping("/for-report")
    public ResponseEntity<?> getRequestRescue(
            @RequestParam(value = "rrid", required = false) Integer rrid) {
        try {
            if (rrid != null) {
                RequestRescueDto dto = requestRescueService.getRequestRescueByRrid(rrid);
                if (dto != null) {
                    return ResponseEntity.ok(ApiResponse.success(dto, "Load request rescue successfully"));
                } else {
                    return ResponseEntity.ok(ApiResponse.errorServer("Not found request rescue with rrid = " + rrid));
                }
            } else {
                List<RequestRescueDto> list = requestRescueService.getAllRequestRescue();
                return ResponseEntity.ok(ApiResponse.success(list, "Load all request rescues successfully"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.errorServer("Error when loading request rescues: " + e.getMessage())
            );
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

    @GetMapping("/customers")
    public ResponseEntity<List<UserDto>> getCustomers() {
        List<User> customerList = userService.findByRole(roleService.findByName("CUSTOMER"));
        return ResponseEntity.status(200).body(toUserDto(customerList));
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


    @GetMapping("/partners")
    public ResponseEntity<List<UserDto>> getPartners() {
        List<User> customerList = userService.findByRole(roleService.findByName("PARTNER"));
        return ResponseEntity.status(200).body(toUserDto(customerList));
    }

    //==================REQUEST RESCUE SECTION==================
    @GetMapping("/reqResQs/searchByUser/{userId}")
    public ResponseEntity<List<RequestResQDto>> searchRRByUser(@PathVariable("userId") int userId) {
        return ResponseEntity.status(200).body(requestRescueService.searchByUser(userId));
    }

    @GetMapping("/reqResQs/searchByPartner/{partId}")
    public ResponseEntity<List<RequestResQDto>> searchByPartner(@PathVariable("partId") int partId) {
        return ResponseEntity.status(200).body(requestRescueService.searchByPartner(partId));
    }

    @GetMapping("/reqResQs/getCusRequestForCancel/{customerId}")
    public ResponseEntity<?> getCusRequestForCancel(@PathVariable("customerId") int customerId) {
        try {
            return ResponseEntity.ok(requestRescueService.getCusRequestForCancel(customerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating request: " + e.getMessage());
        }
    }

    @PostMapping("/reqResQs/createRequest")
    public ResponseEntity<?> createRequest(
            @RequestPart String requestDtoString,
            @RequestPart(required = false) String selectedServices) {
        try{
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

    @PostMapping("/reqResQs/cancelRequest/{requestId}")
    public ResponseEntity<?> cancelRequest(@PathVariable int requestId, @RequestBody Map<String, String> body) {
       try{
           String reason = body.get("reason");
           return ResponseEntity.ok(requestRescueService.cancelRequest(requestId, reason));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating request: " + e.getMessage());
        }
    }

    //==================SERVICE SECTION==================
    @GetMapping("/services/searchBySrvType/{keyword}")
    public ResponseEntity<List<ServiceDto>> searchBySrvType(@PathVariable("keyword") String keyword) {
        return ResponseEntity.ok(servicesService.findByServiceType(keyword));
    }

    //==================PAYMENT SECTION==================
    @GetMapping("/payments/getCustomerPayments/{customerId}")
    public ResponseEntity<List<PaymentDto>> getCustomerPayments(@PathVariable("customerId") int customerId) {
        return ResponseEntity.ok(paymentService.customerPayments(customerId));
    }

    //==================BLOCKED SECTION==================
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

    //==================SUPPORT METHOD==================
    public List<UserDto> toUserDto(List<User> users) {
        List<UserDto> dtoList = new ArrayList<>();
        if (users == null) return dtoList;

        for (User user : users) {
            UserDto dto = new UserDto();
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setSdt(user.getSdt());
            dto.setStatus(user.getStatus());
            dto.setDob(user.getDob());
            dto.setGender(user.getGender());
            dto.setAddress(user.getAddress());
            dto.setAvatar(user.getAvatar());
            dto.setPhoneVerified(user.isPhoneVerified());
            dto.setPassword(user.getPassword());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setUpdatedAt(user.getUpdatedAt());
            dto.setLanguage(user.getLanguage());
            dto.setAppColor(user.getAppColor());
            dto.setLoyaltyPoint(user.getLoyaltyPoint());

            if (user.getRole() != null) {
                dto.setRole(user.getRole().getRoleId());
                dto.setRoleName(user.getRole().getRoleName());
            }

            // Tùy vào nhu cầu bạn có thể set thêm currentPassword hoặc totalRescues
            dtoList.add(dto);
        }

        return dtoList;
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
            if(user.getSdt().equals(dto.getSdt())){
                return "User with this phone number already exists";
            }
            if(user.getUsername().equals(dto.getUsername())){
                return "User with this username already exists";
            }
            if(user.getEmail().equals(dto.getEmail())){
                return "User with this email already exists";
            }
            if(user.getAddress().equals(dto.getAddress())){
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
