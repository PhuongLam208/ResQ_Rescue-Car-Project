package com.livewithoutthinking.resq.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.mapper.PartnerMapper;
import com.livewithoutthinking.resq.service.*;
import com.livewithoutthinking.resq.service.PartnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/resq/manager")
public class ManagerController {
    @Autowired
    private ManagerService managerService;
    @Autowired
    private ScheduleServices scheduleServices;
    @Autowired
    private ReportService reportService;
    @Autowired
    private UserService userService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private PersonalDataService personalDataService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private ExtraServiceSrv extraServiceSrv;
    @Autowired
    private DocumentaryService documentaryService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private RequestSrvService requestSrvService;
    @Autowired
    private RequestRescueService requestRescueService;


    //==================SCHEDULE SECTION==================
    @GetMapping("/schedule")
    public ResponseEntity<List<ScheduleDto>> findMineSchedule() {
        return ResponseEntity.status(200).body(scheduleServices.reloadSchedule(managerService.findMineShift()));
    }

    @PutMapping("/schedule/update/{id}")
    public ResponseEntity<List<ScheduleDto>> updateSchedule(@PathVariable Integer id, @RequestBody ScheduleDto dto) {
        managerService.updateShift(id, dto);
        return ResponseEntity.status(200).body(scheduleServices.reloadSchedule(managerService.findMineShift()));
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

            return ok(ApiResponse.success(dtos, "Get all report successfully!"));
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
                return ok(ApiResponse.success(report.get(), "Get report successfully!"));
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
            return ok(ApiResponse.success(updatedDto, "Report resolved successfully!"));
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

        return ok(ApiResponse.success(dtos, "User search successful"));
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

        return ok(ApiResponse.success(dtos, "Partner search successful"));
    }

    @GetMapping("/report/filter/status")
    public ResponseEntity<ApiResponse<List<Report>>> filterByStatus(@RequestParam("status") String status) {
        try {
            List<Report> reports = reportService.findByStatusIgnoreCase(status);
            if (reports == null || reports.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.errorServer("No reports found with status: " + status));
            }

            return ok(ApiResponse.success(reports, "Filtered reports by status successfully!"));
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
            return ok(ApiResponse.success(reports, "Get all reports successfully!"));
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

            return ok(ApiResponse.success(reportDtos, "Reports fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.errorServer("Failed to fetch reports"));
        }
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
    //==================STAFF SECTION==================
    @GetMapping("/staffs")
    public ResponseEntity<List<Staff>> findOnlyStaff() {
        return ResponseEntity.status(200).body(managerService.findOnlyStaff());
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
            System.out.println(userDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            System.out.println("Validated");
            String existedMessage = userExists(userDto);
            if(existedMessage != null && !existedMessage.isEmpty()){
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflictData(null, existedMessage));
            }
            Staff newStaff = staffService.createNewStaff(userDto, avatar);
            return ok(newStaff);
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
            return ok(updated);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating staff: " + e.getMessage());
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getCustomers() {
        Role role = roleService.findByName("Customer");
        return ResponseEntity.status(200).body(userService.findByRole(role));
    }

    //==================CUSTOMER SECTION==================
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

    //==================PARTNER SECTION==================
    @GetMapping("/partners")
    public ResponseEntity<List<PartnerDto>> getPartners() {
        List<PartnerDto> dtoList = partnerService.findAll()
                .stream()
                .map(PartnerMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

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

    //==================PD SECTION==================
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

    //==================FEEDBACK SECTION==================
    @GetMapping("/feedbacks")
    public ResponseEntity<List<FeedbackDto>> getFeedbacks() {
        return ResponseEntity.status(200).body(feedbackService.findAllDto());
    }

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

    //==================REQUEST RESCUE SECTION==================
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

    //==================EXTRA SERVICE SECTION==================
    @GetMapping("/extraSrv/searchByReqResQ/{rrId}")
    public ResponseEntity<ExtraService> getExtraSrvByReqResQ(@PathVariable("rrId") int rrId) {
        return ok(extraServiceSrv.findExtraSrvByReqResQ(rrId));
    }

    //==================DOCUMENTARY SECTION==================
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

    //==================VEHICLE SECTION==================
    @GetMapping("/vehicle/partnerVehicle/{partnerId}")
    public ResponseEntity<?> getVehiclesPartnerVehicle(@PathVariable int partnerId) {
        try{
            return ResponseEntity.ok(vehicleService.findPartnerVehicles(partnerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }

    @GetMapping("/vehicle/by-user/{userId}")
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehiclesByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getByUserId(userId), "Found"));
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

    // UnBlock 24h ???
    // Bổ sung gỡ block24h

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
        if(dto.getTotal() <= 0) {
            errors.put("specificSrv", "Specific service is required");
        }
        if(dto.getTotal() <= 0) {
            errors.put("specificSrv", "Specific service is required");
        }
        if(dto.getPaymentMethod() == null || dto.getPaymentMethod().trim().isEmpty()) {
            errors.put("paymentMethod", "Payment method is required");
        }
        if(!"resFix".equalsIgnoreCase(dto.getRescueType()) && (dto.getDestination() == null || dto.getDestination().trim().isEmpty())) {
            errors.put("destination", "Destination is required");
        }
        return errors;
    }
}
