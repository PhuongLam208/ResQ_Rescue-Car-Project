package com.livewithoutthinking.resq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livewithoutthinking.resq.dto.*;
import com.livewithoutthinking.resq.entity.Discount;
import com.livewithoutthinking.resq.entity.RequestRescue;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.entity.UserRank;
import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;


@RestController
@RequestMapping("/api/resq/customer")
public class CustomerController {

    @Autowired
    private UserService userService;
    @Autowired
    private RequestRescueService requestRescueService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private PersonalDataService personalDataService;
    @Autowired
    private DocumentaryService documentaryService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserDiscountService userDiscountService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserRankService userRankService;

    /// W ///
    @GetMapping("/home-profile/{userId}")
    public ResponseEntity<?> getHomeProfileInfo(@PathVariable Integer userId) {
        try {
            HomeProfileDto dto = userService.getUserInfo(userId);
            return ResponseEntity.status(200).body(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @GetMapping("/pd-images/{userId}")
    public ResponseEntity<?> getPersonalDocImages(@PathVariable int userId) {
        try {
            PDImageDto dto = personalDataService.getLatestImageStatusByUserId(userId);
            if (dto == null) {
                return ResponseEntity.status(404).body("Personal data not found");
            }

            Map<String, String> result = new HashMap<>();
            result.put("frontImageUrl", dto.getFrontImageUrl());
            result.put("backImageUrl", dto.getBackImageUrl());
            result.put("verificationStatus", dto.getVerificationStatus());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    @PutMapping("/change-password/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Integer userId, @RequestBody Map<String, String> requestBody) {
        String oldPassword = requestBody.get("oldPassword");
        String newPassword = requestBody.get("newPassword");
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            if (passwordEncoder.matches(oldPassword, user.get().getPassword())) {
                user.get().setPassword(passwordEncoder.encode(newPassword));
                userService.saveUser(user.get());
                return ResponseEntity.status(200).body("Password changed successfully");
            }
            return ResponseEntity.status(400).body("Incorrect current password! Please enter " +
                    "the correct current password to change to a new one!");
        }
        return ResponseEntity.status(400).body("User not found");
    }

    @PostMapping("/feedback/save/{rrid}")
    public ResponseEntity<?> saveFeedback(@PathVariable Integer rrid,
                                          @RequestBody Map<String, String> requestBody ) {
        Optional<RequestRescue> rr = requestRescueService.findRRById(rrid);
        if (rr.isEmpty()) {
            return ResponseEntity.status(404).body("RequestRescue not found");
        }
        // lưu đánh giá request rescue
        feedbackService.saveRR(
                rr.get(),
                "FEEDBACK REQUEST RESCUE",
                Integer.parseInt(requestBody.getOrDefault("RescueRate", "0")),
                requestBody.get("RescueDescription")
        );

        // lưu đánh giá partner
        feedbackService.saveRR(
                rr.get(),
                "FEEDBACK PARTNER",
                Integer.parseInt(requestBody.getOrDefault("PartnerRate", "0")),
                requestBody.get("RescueDescription")
        );

        return ResponseEntity.status(200).body("Feedback saved successfully");

    }

    @PutMapping("/change-phone/{userId}")
    public ResponseEntity<?> changePhone(
            @PathVariable int userId,
            @RequestParam String phoneNumber) {
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        user.get().setSdt(phoneNumber);
        userService.saveUser(user.get());
        return ResponseEntity.status(200).body("Phone changed successfully");

    }

    /// H ///
    @PostMapping("/{userId}/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @PathVariable Integer userId,
            @RequestParam("file") MultipartFile file
    ) {
        String url = userService.uploadAvatar(userId, file);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    /// P ///
    // Profile
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomer(@PathVariable("customerId") int customerId) {
        try {
            return ok(customerService.getCustomer(customerId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/updateCustomer/{customerId}")
    public ResponseEntity<?> updateCustomer(@PathVariable("customerId") int customerId,
                                            @RequestBody String userDtoString) {
        try {
            UserDto dto = objectMapper.readValue(userDtoString, UserDto.class);
            if (dto.getUserId() == 0) {
                dto.setUserId(customerId);
            }
            Map<String, String> errors = validateProfile(dto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ok(customerService.updateCustomer(dto));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error updating staff: " + e.getMessage());
        }
    }

    // Vehicle
    @GetMapping("/vehicles/{customerId}")
    public ResponseEntity<?> getVehicles(@PathVariable("customerId") int customerId) {
        try {
            return ok(vehicleService.getByUserId(customerId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/vehicles/noDoc/{customerId}")
    public ResponseEntity<?> getVehiclesNoDoc(@PathVariable("customerId") int customerId) {
        try {
            return ok(vehicleService.getVehiclesNoDoc(customerId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/vehicles/createNew")
    public ResponseEntity<?> createNewVehicle(
            @RequestPart("vehicleDtoString") String vehicleDtoString,
            @RequestPart(value = "frontImage", required = false) MultipartFile frontImage,
            @RequestPart(value = "backImage", required = false) MultipartFile backImage) {
        try {
            VehicleDto vehicleDto = objectMapper.readValue(vehicleDtoString, VehicleDto.class);

            Map<String, String> errors = validateVehicle(vehicleDto);
            if (frontImage == null || frontImage.isEmpty()) {
                errors.put("frontImage", "Front image is required");
            }
            if (backImage == null || backImage.isEmpty()) {
                errors.put("backImage", "Back image is required");
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ResponseEntity.ok(vehicleService.addCustomerVehicle(vehicleDto, frontImage, backImage));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    @PutMapping("/vehicles/updateVehicle/{vehicleId}")
    public ResponseEntity<?> getVehicle(@PathVariable("vehicleId") int vehicleId,
                                        @RequestPart("vehicleDtoString") String vehicleDtoString,
                                        @RequestPart(value = "frontImage", required = false) MultipartFile frontImage,
                                        @RequestPart(value = "backImage", required = false) MultipartFile backImage) {
        try {
            VehicleDto vehicleDto = objectMapper.readValue(vehicleDtoString, VehicleDto.class);

            if (vehicleDto.getVehicleId() != vehicleId || vehicleDto.getVehicleId() == 0) {
                vehicleDto.setVehicleId(vehicleId);
            }
            Map<String, String> errors = validateVehicle(vehicleDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ResponseEntity.ok(vehicleService.updateCustomerVehicle(vehicleDto, frontImage, backImage));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    @DeleteMapping("/vehicles/{sVehicleId}")
    public ResponseEntity<Map<String, String>> deleteVehicle(@PathVariable("sVehicleId") String sVehicleId) {
        try {
            Integer vehicleId = Integer.parseInt(sVehicleId);
            vehicleService.deleteVehicle(vehicleId);
            return ResponseEntity.ok(Map.of("message", "Deleted vehicle sucessfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "ID is invalid: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }
    }


    // Personal Data
    @GetMapping("/personaldata/{customerId}")
    public ResponseEntity<?> getPeronalData(@PathVariable("customerId") int customerId) {
        try {
            return ok(personalDataService.getCustomerPd(customerId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/personaldata/createNew")
    public ResponseEntity<?> createNewPeronalData(
            @RequestPart("personalDataString") String personalDataString,
            @RequestPart("userId") String userId,
            @RequestPart(value = "frontImage", required = false) MultipartFile frontImage,
            @RequestPart(value = "backImage", required = false) MultipartFile backImage,
            @RequestPart(value = "faceImage", required = false) MultipartFile faceImage) {
        try {
            PersonalDataDto personalDataDto = objectMapper.readValue(personalDataString, PersonalDataDto.class);
            System.out.println(frontImage);
            System.out.println(faceImage);
            Map<String, String> errors = validatePersonalData(personalDataDto);
            if (frontImage == null || frontImage.isEmpty()) {
                errors.put("frontImage", "Front image is required");
            }
            if (backImage == null || backImage.isEmpty()) {
                errors.put("backImage", "Back image is required");
            }
            if (faceImage == null || faceImage.isEmpty()) {
                errors.put("faceImage", "Face image is required");
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ResponseEntity.ok(personalDataService.addPersonalData(personalDataDto, Integer.parseInt(userId), frontImage, backImage, faceImage));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    @PutMapping("/personaldata/updatePd/{pdId}")
    public ResponseEntity<?> updatePersonalData(@PathVariable("pdId") String pdId,
                                                @RequestPart("personalDataDtoString") String personalDataDtoString,
                                                @RequestPart(value = "frontImage", required = false) MultipartFile frontImage,
                                                @RequestPart(value = "backImage", required = false) MultipartFile backImage,
                                                @RequestPart(value = "faceImage", required = false) MultipartFile faceImage) {
        try {
            PersonalDataDto personalDataDto = objectMapper.readValue(personalDataDtoString, PersonalDataDto.class);
            Integer personalDataId = Integer.parseInt(pdId);
            if (personalDataDto.getPdId() != personalDataId || personalDataDto.getPdId() == 0) {
                personalDataDto.setPdId(personalDataId);
            }
            System.out.println(personalDataDto);
            Map<String, String> errors = validatePersonalData(personalDataDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ResponseEntity.ok(personalDataService.updatePersonalData(personalDataDto, frontImage, backImage, faceImage));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    //Documents
    @GetMapping("/documents/{customerId}")
    public ResponseEntity<?> getDocument(@PathVariable("customerId") int customerId) {
        try {
            return ok(documentaryService.getByUserId(customerId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/documents/createNew")
    public ResponseEntity<?> createNewDocument(
            @RequestPart("documentString") String documentString,
            @RequestPart("userIdString") String userIdString,
            @RequestPart(value = "frontImage", required = false) MultipartFile frontImage,
            @RequestPart(value = "backImage", required = false) MultipartFile backImage) {
        try {
            DocumentaryDto documentaryDto = objectMapper.readValue(documentString, DocumentaryDto.class);

            Map<String, String> errors = validateDocument(documentaryDto);
            if (frontImage == null || frontImage.isEmpty()) {
                errors.put("frontImage", "Front image is required");
            }
            if (backImage == null || backImage.isEmpty()) {
                errors.put("backImage", "Back image is required");
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ResponseEntity.ok(documentaryService.addCusDoc(documentaryDto, Integer.parseInt(userIdString), frontImage, backImage));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    @PutMapping("/documents/updateDocument/{sDocumentId}")
    public ResponseEntity<?> getDocument(@PathVariable("sDocumentId") String sDocumentId,
                                         @RequestPart("documentDtoString") String documentDtoString,
                                         @RequestPart(value = "frontImage", required = false) MultipartFile frontImage,
                                         @RequestPart(value = "backImage", required = false) MultipartFile backImage) {
        try {
            DocumentaryDto documentDto = objectMapper.readValue(documentDtoString, DocumentaryDto.class);
            Integer documentId = Integer.parseInt(sDocumentId);
            if (documentDto.getDocumentId() != documentId || documentDto.getDocumentId() == 0) {
                documentDto.setDocumentId(documentId);
            }
            Map<String, String> errors = validateDocument(documentDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            System.out.println(documentDto);
            return ResponseEntity.ok(documentaryService.updateCusDoc(documentDto, frontImage, backImage));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    @DeleteMapping("/documents/{sDocumentId}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable("sDocumentId") String sDocumentId) {
        try {
            Integer documentId = Integer.parseInt(sDocumentId);
            documentaryService.deleteDocument(documentId);
            return ResponseEntity.ok(Map.of("message", "Deleted document sucessfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "ID is invalid: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }
    }

    //Discount
    @GetMapping("/discounts/appDiscounts/{userId}")
    public ResponseEntity<?> getAppDiscounts(@PathVariable("userId") int userId) {
        try{
            return ok(discountService.getAppDiscounts(userId));
        }catch(Exception e){
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }
    }

    @GetMapping("/discounts/rankDiscounts/{userId}")
    public ResponseEntity<?> getRankDiscounts(@PathVariable("userId") int userId) {
        try{
            return ok(discountService.getRankedDiscounts(userId));
        }catch(Exception e){
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }

    }

    @GetMapping("/discounts/myDiscounts/{userId}")
    public ResponseEntity<?> getMyDiscount(@PathVariable("userId") int userId) {
        try {
            return ok(userDiscountService.findByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body("Error parsing vehicleDtoString: " + e.getMessage());
        }
    }

    @PostMapping("/discounts/claimDiscount")
    public ResponseEntity<?> claimDiscount(@RequestBody Map<String, Integer> body) {
        int discountId = body.get("discountId");
        int userId = body.get("userId");
        Optional<Discount> discount = discountService.findDiscountById(discountId);
        if(discount.get().getTypeDis().equalsIgnoreCase("hoi_vien")){
            UserRank ur = userRankService.findUserRankByUserId(userId);
            if (ur != null && ur.getChangeLimitLeft() <= 0) {
                return ResponseEntity.status(404).body("No change left for this month");
            }
        }
        discountService.claimDiscount(discountId, userId);
        return ResponseEntity.ok("Claimed successfully");
    }


    //Payment
    @GetMapping("/payments/{userId}")
    public ResponseEntity<?> getPayments(@PathVariable("userId") int userId) {
        try {
            return ok(paymentService.appPayments(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/payments/createNew/{userId}")
    public ResponseEntity<?> createPayment(@PathVariable("userId") int userId,
                                           @RequestParam String paymentDtoString) {
        try{
            PaymentDto paymentDto = objectMapper.readValue(paymentDtoString, PaymentDto.class);

            Map<String, String> errors = validatePayment(paymentDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ok(paymentService.createPayment(userId, paymentDto));
        }catch(Exception e){
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }
    }

    @PutMapping("/payments/updatePayment/{sPaymentId}")
    public ResponseEntity<?> updatePayment(@PathVariable("sPaymentId") String sPaymentId,
                                           @RequestPart String paymentDtoString) {
        try{
            PaymentDto paymentDto = objectMapper.readValue(paymentDtoString, PaymentDto.class);
            Integer paymentId = Integer.parseInt(sPaymentId);
            if (paymentDto.getPaymentId() != paymentId) {
                paymentDto.setPaymentId(paymentId);
            }
            Map<String, String> errors = validatePayment(paymentDto);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest(errors));
            }
            return ok(paymentService.updatePayment(paymentDto));
        }catch(Exception e){
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable("paymentId") int paymentId) {
        try {
            System.out.println(paymentId);
            paymentService.deletePayment(paymentId);
            System.out.println("Through");
            return ResponseEntity.ok(Map.of("message", "Deleted payment sucessfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "ID is invalid: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "System error: " + e.getMessage())
            );
        }
    }


    //Support
    private <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getImage(@RequestParam String path) throws IOException {
        Path imagePath = Paths.get(path);
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(imagePath.toUri());
        String contentType = Files.probeContentType(imagePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    //Validate
    private Map<String, String> validateProfile(UserDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            errors.put("username", "Username is required");
        } else if (!dto.getUsername().matches("^[a-zA-Z0-9]+$")) {
            errors.put("userName", "Username must not contain whitespace or special characters");
        }
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!dto.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            errors.put("email", "Invalid email format");
        }
        return errors;
    }

    private Map<String, String> validateVehicle(VehicleDto dto) {
        Map<String, String> errors = new LinkedHashMap<>();
        String plateNoPattern = "^[0-9]{2}[A-Z]{1,2}-[0-9]{4,5}$";
        if (dto.getPlateNo() == null || dto.getPlateNo().isEmpty()) {
            errors.put("plateNo", "Plate no is required");
        } else if (!dto.getPlateNo().matches(plateNoPattern)) {
            errors.put("plateNo", "Invalid plate no format");
        }
        if (dto.getBrand() == null || dto.getBrand().isEmpty()) {
            errors.put("brand", "Brand is required");
        }
        if (dto.getModel() == null || dto.getModel().isEmpty()) {
            errors.put("model", "Model is required");
        }
        int currentYear = LocalDate.now().getYear();
        if (dto.getYear() < 1886 || dto.getYear() > currentYear) {
            errors.put("year", "Year must be between 1886 and " + currentYear);
        }
        return errors;
    }

    private Map<String, String> validatePersonalData(PersonalDataDto dto) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (dto.getCitizenNumber() == null || dto.getCitizenNumber().isEmpty()) {
            errors.put("citizenNumber", "Citizen number is required");
        } else if ("Identity Card".equals(dto.getType())) {
            String citizenNumber = dto.getCitizenNumber();
            if (citizenNumber.length() < 9 || citizenNumber.length() > 12 || !citizenNumber.matches("\\d+")) {
                errors.put("citizenNumber", "Citizen number must be 9-12 digits and contain only numbers for Identity Card");
            }
        }
        if (dto.getIssuePlace() == null || dto.getIssuePlace().isEmpty()) {
            errors.put("issuePlace", "Issued place is required");
        }
        if (dto.getIssueDate() == null) {
            errors.put("issueDate", "Issued date is required");
        } else if (!dto.getIssueDate().before(new Date())) {
            errors.put("issueDate", "Issued date must not be after today");
        }
        if (dto.getExpirationDate() == null) {
            errors.put("expirationDate", "Expiration date is required");
        } else if (!dto.getExpirationDate().after(dto.getIssueDate())) {
            errors.put("expirationDate", "Expiration date must be after issued date");
        } else if (!dto.getExpirationDate().after(new Date())) {
            errors.put("expirationDate", "Expiration date must be after today");
        }

        return errors;
    }

    private Map<String, String> validateDocument(DocumentaryDto dto) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (dto.getDocumentNumber() == null || dto.getDocumentNumber().isEmpty()) {
            errors.put("documentNumber", "Document number is required");
        } else if (!dto.getDocumentNumber().matches("^C\\d{6,7}$")) {
            errors.put("documentNumber", "Document number must start with 'C' followed by 6 or 7 digits (e.g. C1234567)");
        }
        if (dto.getExpiryDate() == null) {
            errors.put("expiryDate", "Expiry date is required");
        } else if (!dto.getExpiryDate().isAfter(LocalDate.now())) {
            errors.put("expiryDate", "Expiry date must be after today");
        }
        return errors;
    }

    private Map<String, String> validatePayment(PaymentDto paymentDto) {
        Map<String, String> errors = new LinkedHashMap<>();
        if(paymentDto.getPaypalEmail() == null || paymentDto.getPaypalEmail().isEmpty()) {
            errors.put("paypalEmail", "Paypal email is required");
        } else if (!paymentDto.getPaypalEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.put("paypalEmail", "Invalid Paypal email format");
        }
        if(paymentDto.getName()!=null && paymentDto.getName().isEmpty()){
            errors.put("name", "Name is required");
        }
        return errors;
    }

}
