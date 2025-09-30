package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.PartnerRegistrationRequest;
import com.livewithoutthinking.resq.dto.PartnerServiceUpdateRequest;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.PartnerRegistrationService;
import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerRegistrationServiceImpl implements PartnerRegistrationService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final DocumentaryRepository documentaryRepository;
    private final ServiceRepository servicesRepository;
    private final PartnerServiceRepository partnerServiceRepository;
    private final VehicleRepository vehicleRepository;


    private final String uploadDir = System.getProperty("user.dir") + "/secure_uploads/";

    @Override
    public ResponseEntity<?> registerPartner(PartnerRegistrationRequest request) {
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Partner partner = partnerRepository.findByUser_UserId(request.getUserId())
                    .orElseGet(() -> {
                        Partner p = new Partner();
                        p.setUser(user);
                        p.setVerificationStatus(false);
                        p.setCreatedAt(new Date());
                        return p;
                    });

            if (request.getResFix() == 2 && partner.getResFix() == 0) partner.setResFix(2);
            if (request.getResTow() == 2 && partner.getResTow() == 0) partner.setResTow(2);
            if (request.getResDrive() == 2 && partner.getResDrive() == 0) partner.setResDrive(2);

            partner.setUpdatedAt(new Date());
            partner = partnerRepository.save(partner);

            // FIX
            if (request.getResFix() == 2 && request.getLicenseNumber() != null) {
                saveDocument(partner, "resfix", "Giấy phép hành nghề",
                        request.getLicenseNumber(),
                        request.getDocumentFront(),
                        request.getDocumentBack(),
                        request.getLicenseExpiryDate());


            }

            // TOW
            if (request.getResTow() == 2) {
                if (request.getTowLicenseNumber() != null) {
                    saveDocument(partner, "restow", "Giấy phép lái xe",
                            request.getTowLicenseNumber(),
                            request.getTowLicenseFront(), request.getTowLicenseBack(),request.getTowLicenseExpiryDate());
                }
                if (request.getTowInspectionNumber() != null) {
                    saveDocument(partner, "restow", "Giấy đăng kiểm",
                            request.getTowInspectionNumber(),
                            request.getTowInspectionFront(), request.getTowInspectionBack(),request.getTowInspectionExpiryDate());
                }
                if (request.getTowSpecialPermitNumber() != null) {
                    saveDocument(partner, "restow", "Giấy phép kinh doanh vận tải",
                            request.getTowSpecialPermitNumber(),
                            request.getTowSpecialPermitFront(), request.getTowSpecialPermitBack(),request.getTowSpecialPermitExpiryDate());
                }

                if (request.getDriveVehicleImage() != null || request.getDriveLicensePlateImage() != null) {
                    Vehicle vehicle = partner.getVehicle();
                    if (vehicle == null) {
                        vehicle = new Vehicle();
                        vehicle.setUser(user);
                        vehicle.setCreatedAt(new Date());
                    }

                    if (request.getDriveVehicleImage() != null && !request.getDriveVehicleImage().isEmpty()) {
                        String frontFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(request.getDriveVehicleImage().getOriginalFilename());
                        File frontFile = new File(uploadDir, frontFilename);
                        request.getDriveVehicleImage().transferTo(frontFile);
                        vehicle.setFrontImage("secure_uploads/" + frontFilename);
                    }

                    if (request.getDriveLicensePlateImage() != null && !request.getDriveLicensePlateImage().isEmpty()) {
                        String backFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(request.getDriveLicensePlateImage().getOriginalFilename());
                        File backFile = new File(uploadDir, backFilename);
                        request.getDriveLicensePlateImage().transferTo(backFile);
                        vehicle.setBackImage("secure_uploads/" + backFilename);
                    }

                    vehicle.setUpdatedAt(new Date());
                    vehicle = vehicleRepository.save(vehicle);

                    partner.setVehicle(vehicle);              // gán vehicle vào Partner
                    partnerRepository.save(partner); // Gắn lại vào partner
                }


            }

            // DRIVE
            if (request.getResDrive() == 2 && request.getDriveLicenseNumber() != null) {
                if (request.getDriveLicenseNumber() != null) {
                    saveDocument(partner, "resdrive", "Giấy phép lái xe",
                            request.getDriveLicenseNumber(),
                            request.getDriveLicenseFront(), request.getDriveLicenseBack(),request.getDriveLicenseExpiryDate());
                }

            }

            // DỊCH VỤ
            if (request.getSelectedServiceIds() != null) {
                for (Integer serviceId : request.getSelectedServiceIds()) {
                    Services service = servicesRepository.findById(serviceId)
                            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));
                    boolean exists = partnerServiceRepository.existsByPartnerAndServices(partner, service);
                    if (!exists) {
                        PartnerService ps = new PartnerService();
                        ps.setPartner(partner);
                        ps.setServices(service);
                        partnerServiceRepository.save(ps);
                    }
                }
            }

            return ResponseEntity.ok("Đăng ký partner thành công");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi xử lý file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> getServicesByType(String type) {
        List<Services> services = servicesRepository.findByServiceType(type);
        return ResponseEntity.ok(services);
    }

    @Override
    public ResponseEntity<?> getRegisteredPartnerTypes(Integer userId) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(userId);

        if (partnerOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "resFix", 0,
                    "resTow", 0,
                    "resDrive", 0,
                    "verificationStatus", false
            ));
        }

        Partner partner = partnerOpt.get();
        return ResponseEntity.ok(Map.of(
                "resFix", partner.getResFix(),
                "resTow", partner.getResTow(),
                "resDrive", partner.getResDrive(),
                "verificationStatus", partner.isVerificationStatus()
        ));
    }

    private void saveDocument(Partner partner, String resType, String documentType, String number,
                              MultipartFile frontFile, MultipartFile backFile, String expiryDateStr) throws Exception {
        if ((frontFile == null || frontFile.isEmpty()) && (backFile == null || backFile.isEmpty())) return;

        String frontImage = null;
        String backImage = null;

        if (frontFile != null && !frontFile.isEmpty()) {
            String frontFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(frontFile.getOriginalFilename());
            File frontSave = new File(uploadDir, frontFilename);
            frontFile.transferTo(frontSave);
            frontImage = "secure_uploads/" + frontFilename;
        }

        if (backFile != null && !backFile.isEmpty()) {
            String backFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(backFile.getOriginalFilename());
            File backSave = new File(uploadDir, backFilename);
            backFile.transferTo(backSave);
            backImage = "secure_uploads/" + backFilename;
        }

        Documentary doc = new Documentary();
        doc.setPartner(partner);
        doc.setResType(resType.toLowerCase());
        doc.setDocumentType(AESEncryptionUtil.encrypt(documentType != null ? documentType : ""));
        doc.setDocumentNumber(AESEncryptionUtil.encrypt(number != null ? number : ""));
        doc.setFrontImage(frontImage);
        doc.setBackImage(backImage);
        doc.setDocumentStatus("PENDING"); // thêm dòng này

        // ✅ Gán ngày hết hạn nếu có
        if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
            try {
                LocalDate expiry = LocalDate.parse(expiryDateStr);
                doc.setExpiryDate(expiry);
            } catch (Exception e) {
                // Log cảnh báo nếu cần
                System.err.println("⚠️ Không thể parse expiryDate: " + expiryDateStr);
            }
        }

        doc.setCreatedAt(new Date());
        doc.setUpdatedAt(new Date());

        documentaryRepository.save(doc);
    }




    @Override
    public ResponseEntity<?> getPartnerInfo(Integer userId) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(userId);

        if (partnerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Partner not found for userId=" + userId);
        }

        Partner partner = partnerOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("partnerId", partner.getPartnerId());
        response.put("userId", partner.getUser().getUserId());
        response.put("isOnline", partner.isOnWorking());
        response.put("walletAmount", partner.getWalletAmount());

        //response.put("partnerAddress", partner.getPartnerAddress());
        response.put("verificationStatus", partner.isVerificationStatus());
        response.put("avgTime", partner.getAvgTime());
        response.put("createdAt", partner.getCreatedAt());
        response.put("updatedAt", partner.getUpdatedAt());

        // Logic tag cho từng loại
        response.put("resFix", getStatusMap(partner.getResFix()));
        response.put("resTow", getStatusMap(partner.getResTow()));
        response.put("resDrive", getStatusMap(partner.getResDrive()));

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getStatusMap(int resStatus) {
        String tag;
        boolean clickable = true;

        switch (resStatus) {
            case 1:
                tag = "Active";
                clickable = false;
                break;
            case 2:
                tag = "Pending";
                break;
            case 4:
                tag = "Rejected";
                break;
            default:
                tag = "Not register";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("status", resStatus);
        map.put("tag", tag);
        map.put("clickable", clickable);
        return map;
    }

    // Service Implementation (2 methods added)
    @Override
    public ResponseEntity<?> getSelectedServiceIds(Integer userId, String type) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(userId);
        if (partnerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Partner not found");
        }

        List<PartnerService> selected = partnerServiceRepository.findByPartnerAndServiceType(partnerOpt.get(), type);
        List<Integer> selectedIds = selected.stream()
                .map(ps -> ps.getServices().getServiceId())
                .toList();

        return ResponseEntity.ok(selectedIds);
    }

    @Override
    public ResponseEntity<?> updateSelectedServices(PartnerServiceUpdateRequest request) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(request.getUserId());
        if (partnerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Partner not found");
        }

        Partner partner = partnerOpt.get();
        List<PartnerService> existing = partnerServiceRepository.findByPartnerAndServiceType(partner, request.getType());
        partnerServiceRepository.deleteAll(existing);

        for (Integer serviceId : request.getServiceIds()) {
            Services service = servicesRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            PartnerService ps = new PartnerService();
            ps.setPartner(partner);
            ps.setServices(service);
            partnerServiceRepository.save(ps);
        }

        return ResponseEntity.ok("Cập nhật dịch vụ thành công");
    }

    @Override
    public boolean isPartnerRegistered(Integer userId) {
        return partnerRepository.findByUser_UserId(userId).isPresent();
    }

    @Override
    public Map<String, Object> getPartnerDocumentMap(Integer userId) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(userId);
        if (partnerOpt.isEmpty()) {
            return Map.of("error", "Partner not found");
        }

        Partner partner = partnerOpt.get();
        List<Documentary> docs = documentaryRepository.findByPartner(partner);

        Map<String, Object> result = new HashMap<>();
        result.put("resFix", mapDocumentListByResType(docs, "resfix"));
        result.put("resTow", mapRestowDocumentsWithVehicle(docs, partner.getVehicle()));
        result.put("resDrive", mapDocumentListByResType(docs, "resdrive"));

        return result;
    }

    private List<Map<String, Object>> mapRestowDocumentsWithVehicle(List<Documentary> docs, Vehicle vehicle) {
        List<Map<String, Object>> restowDocs = docs.stream()
                .filter(d -> "restow".equalsIgnoreCase(d.getResType()))
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doc.getDocumentId());
                    map.put("number", decryptSafe(doc.getDocumentNumber()));
                    map.put("type", decryptSafe(doc.getDocumentType()));
                    map.put("frontImage", buildUrl(doc.getFrontImage()));
                    map.put("backImage", buildUrl(doc.getBackImage()));
                    map.put("status", doc.getDocumentStatus());
                    map.put("expiryDate", doc.getExpiryDate());
                    return map;
                })
                .collect(Collectors.toList());

        // Gộp ảnh vehicle thành 1 dòng
        if (vehicle != null) {
            Map<String, Object> vehicleImageGroup = new HashMap<>();
            vehicleImageGroup.put("type", "Ảnh xe kéo và biển số (từ vehicle)");
            vehicleImageGroup.put("frontImage", buildUrl(vehicle.getFrontImage()));
            vehicleImageGroup.put("backImage", buildUrl(vehicle.getBackImage()));
            restowDocs.add(vehicleImageGroup);
        }

        return restowDocs;
    }


    private List<Map<String, Object>> mapDocumentListByResType(List<Documentary> docs, String resType) {
        return docs.stream()
                .filter(d -> resType.equalsIgnoreCase(d.getResType()))
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doc.getDocumentId());
                    map.put("number", decryptSafe(doc.getDocumentNumber()));
                    map.put("type", decryptSafe(doc.getDocumentType()));
                    map.put("frontImage", buildUrl(doc.getFrontImage()));
                    map.put("backImage", buildUrl(doc.getBackImage()));
                    map.put("status", doc.getDocumentStatus());
                    map.put("expiryDate", doc.getExpiryDate());
                    return map;
                })
                .toList();
    }


    private String buildUrl(String path) {
        return path != null ? path : "";
    }


    private String decryptSafe(String encrypted) {
        try {
            return AESEncryptionUtil.decrypt(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void appendTowImage(Map<String, Object> map, String image) {
        if (!map.containsKey("frontImage")) {
            map.put("frontImage", image);
        } else if (!map.containsKey("backImage")) {
            map.put("backImage", image);
        }
    }

    @Override
    public ResponseEntity<?> getPartnerDocuments(Integer userId) {
        Map<String, Object> result = getPartnerDocumentMap(userId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<?> cancelPartnerType(Integer userId, String type) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(userId);
        if (partnerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Partner not found");
        }

        Partner partner = partnerOpt.get();
        String typeLower = type.toLowerCase(); // "resfix", "restow", "resdrive"

        // 1. Xoá giấy tờ theo loại
        List<Documentary> docs = documentaryRepository.findByPartner(partner);
        List<Documentary> toDelete = docs.stream()
                .filter(doc -> typeLower.equalsIgnoreCase(doc.getResType()))
                .toList();
        documentaryRepository.deleteAll(toDelete);

        // 2. Xoá dịch vụ theo loại
        List<PartnerService> partnerServices = partnerServiceRepository.findByPartnerAndServiceType(partner, typeLower);
        partnerServiceRepository.deleteAll(partnerServices);

        // 3. Nếu loại là restow, thì xoá thêm vehicle (nếu có)
        if (typeLower.equals("restow")) {
            Vehicle vehicle = partner.getVehicle();
            if (vehicle != null) {
                partner.setVehicle(null); // Gán null trong bảng Partner
                partnerRepository.save(partner); // Cập nhật trước khi xoá

                vehicleRepository.delete(vehicle); // Xoá khỏi bảng Vehicle
            }
        }

        // 4. Cập nhật trạng thái partner
        switch (typeLower) {
            case "resfix" -> partner.setResFix(0);
            case "restow" -> partner.setResTow(0);
            case "resdrive" -> partner.setResDrive(0);
            default -> {
                return ResponseEntity.badRequest().body("Invalid partner type: " + type);
            }
        }

        partner.setUpdatedAt(new Date());
        partnerRepository.save(partner);

        return ResponseEntity.ok("Đã huỷ đăng ký & xoá giấy tờ + dịch vụ" +
                (typeLower.equals("restow") ? " + phương tiện" : "") +
                " của loại: " + type);
    }




    @Override
    public ResponseEntity<?> updateOnlineStatus(Integer userId, boolean status) {
        Optional<Partner> partnerOpt = partnerRepository.findByUser_UserId(userId);
        if (partnerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Partner not found");
        }

        Partner partner = partnerOpt.get();
        partner.setOnWorking(status);
        partner.setUpdatedAt(new Date());

        partnerRepository.save(partner);
        return ResponseEntity.ok("Online status updated");
    }

    @Override
    public ResponseEntity<?> updateDocument(Integer documentId, String documentNumber,
                                            Integer userId,
                                            MultipartFile frontImage, MultipartFile backImage) {
        // Nếu là cập nhật ảnh phương tiện
        if (documentId == -1) {
            return updateVehicleImages(userId, frontImage, backImage);
        }

        // Nếu là cập nhật giấy tờ bình thường
        Optional<Documentary> docOpt = documentaryRepository.findById(documentId);
        if (docOpt.isEmpty()) return ResponseEntity.badRequest().body("Không tìm thấy giấy tờ");

        Documentary doc = docOpt.get();

        try {
            if (documentNumber != null) {
                doc.setDocumentNumber(AESEncryptionUtil.encrypt(documentNumber));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi mã hóa số giấy tờ: " + e.getMessage());
        }

        doc.setUpdatedAt(new Date());

        try {
            if (frontImage != null && !frontImage.isEmpty()) {
                String frontFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(frontImage.getOriginalFilename());
                File frontSave = new File(uploadDir, frontFilename);
                frontImage.transferTo(frontSave);
                doc.setFrontImage("secure_uploads/" + frontFilename);
            }

            if (backImage != null && !backImage.isEmpty()) {
                String backFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(backImage.getOriginalFilename());
                File backSave = new File(uploadDir, backFilename);
                backImage.transferTo(backSave);
                doc.setBackImage("secure_uploads/" + backFilename);
            }

            documentaryRepository.save(doc);
            return ResponseEntity.ok("Cập nhật giấy tờ thành công");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi khi cập nhật file: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> getUserFullname(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        String fullname = userOpt.get().getFullName();
        return ResponseEntity.ok(Map.of("fullname", fullname));
    }

    private ResponseEntity<?> updateVehicleImages(Integer userId, MultipartFile frontImage, MultipartFile backImage) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findByUser_UserId(userId);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy phương tiện cho userId=" + userId);
            }

            Vehicle vehicle = vehicleOpt.get();

            if (frontImage != null && !frontImage.isEmpty()) {
                String frontFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(frontImage.getOriginalFilename());
                File frontSave = new File(uploadDir, frontFilename);
                frontImage.transferTo(frontSave);
                vehicle.setFrontImage("secure_uploads/" + frontFilename);
            }

            if (backImage != null && !backImage.isEmpty()) {
                String backFilename = UUID.randomUUID() + "_" + StringUtils.cleanPath(backImage.getOriginalFilename());
                File backSave = new File(uploadDir, backFilename);
                backImage.transferTo(backSave);
                vehicle.setBackImage("secure_uploads/" + backFilename);
            }

            vehicle.setUpdatedAt(new Date());
            vehicleRepository.save(vehicle);

            return ResponseEntity.ok("Cập nhật ảnh phương tiện thành công");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi xử lý file ảnh phương tiện: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getUserAvatar(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        String avatarPath = user.getAvatar(); // Có thể là null

        if (avatarPath != null && !avatarPath.isEmpty()) {
            return ResponseEntity.ok(Map.of("avatar", "/" + avatarPath));
        } else {
            // Có thể trả path ảnh mặc định hoặc rỗng
            return ResponseEntity.ok(Map.of("avatar", "/uploads/avatar/default_avatar.png"));
        }
    }




}
