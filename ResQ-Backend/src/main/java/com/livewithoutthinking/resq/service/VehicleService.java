package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.DocumentaryDto;
import com.livewithoutthinking.resq.dto.VehicleDto;
import com.livewithoutthinking.resq.entity.Documentary;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.entity.Vehicle;
import com.livewithoutthinking.resq.repository.DocumentaryRepository;
import com.livewithoutthinking.resq.repository.PartnerRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.repository.VehicleRepository;
import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UploadService uploadService;
    private final UserRepository userRepository;
    private final DocumentaryRepository documentaryRepository;
    @Autowired
    private DocumentaryService documentaryService;
    @Autowired
    private PartnerRepository partnerRepository;

    public VehicleService(VehicleRepository vehicleRepository, UploadService uploadService, UserRepository userRepository, DocumentaryRepository documentaryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.uploadService = uploadService;
        this.userRepository = userRepository;
        this.documentaryRepository = documentaryRepository;
    }

    // === CREATE ===
    public Vehicle addVehicle(int userId, String brand, String model, int year, String vehicleStatus,
                              MultipartFile frontImage, MultipartFile backImage,
                              MultipartFile imgTem, MultipartFile imgTool, MultipartFile imgDevice) throws Exception {

        Vehicle v = new Vehicle();

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        v.setUser(user);

        v.setBrand(encryptSafe(brand));
        v.setModel(encryptSafe(model));
        v.setYear(year);
        v.setVehicleStatus(vehicleStatus);
        v.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // Upload and store encrypted filenames
        v.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        v.setBackImage(uploadService.saveEncryptedFile(backImage));
        v.setImgTem(uploadService.saveEncryptedFile(imgTem));
        v.setImgTool(uploadService.saveEncryptedFile(imgTool));
        v.setImgDevice(uploadService.saveEncryptedFile(imgDevice));

        return vehicleRepository.save(v);
    }

    // === CUSTOMER CREATE ===
    public Vehicle addCustomerVehicle(VehicleDto vehicleDto, MultipartFile frontImage, MultipartFile backImage) throws Exception {
        Vehicle v = new Vehicle();
        User user = userRepository.findById(vehicleDto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        v.setUser(user);
        v.setPlateNo(encryptSafe(vehicleDto.getPlateNo()));
        v.setBrand(encryptSafe(vehicleDto.getBrand()));
        v.setModel(encryptSafe(vehicleDto.getModel()));
        v.setYear(vehicleDto.getYear());
        v.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        v.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        v.setBackImage(uploadService.saveEncryptedFile(backImage));
        v.setDocumentStatus("PENDING");
        vehicleRepository.save(v);
        return v;
    }
    public VehicleDto findPartnerVehicles(int partnerId) {
        Partner partner = partnerRepository.findPartnerById(partnerId);
        Optional<VehicleDto> v = vehicleRepository.findById(partner.getVehicle().getVehicleId())
                .map(this::toDto);
        return v.get();
    }
    public List<VehicleDto> getVehiclesNoDoc(int userId) {
        List<Vehicle> vehicles = vehicleRepository.findByUser_UserId(userId);
        List<Documentary> documentaries = documentaryRepository.findByUser_UserId(userId);

        Set<String> decryptedDocumentTypes = documentaries.stream()
                .map(d -> decryptSafe(d.getDocumentType()))
                .collect(Collectors.toSet());

        List<VehicleDto> vehicleDtos = new ArrayList<>();

        for (Vehicle v : vehicles) {
            String plateNo = decryptSafe(v.getPlateNo());
            boolean matched = false;

            for (String doc : decryptedDocumentTypes) {
                if (doc.contains(plateNo)) {
                    System.out.println("Document: " + doc);
                    System.out.println("Matched: " + plateNo);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                vehicleDtos.add(toDto(v));
            }
        }

        return vehicleDtos;
    }
    // === CUSTOMER UPDATE ===
    public Vehicle updateCustomerVehicle(VehicleDto vehicleDto, MultipartFile frontImage, MultipartFile backImage) throws Exception
    {
        Vehicle v = vehicleRepository.findById(vehicleDto.getVehicleId()).orElseThrow(() -> new RuntimeException("Vehicle not found"));
        User user = userRepository.findById(vehicleDto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        if(v != null){
            v.setUser(user);
            v.setPlateNo(encryptSafe(vehicleDto.getPlateNo()));
            v.setBrand(encryptSafe(vehicleDto.getBrand()));
            v.setModel(encryptSafe(vehicleDto.getModel()));
            v.setYear(vehicleDto.getYear());
            v.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            if(frontImage != null && !frontImage.isEmpty()){
                v.setFrontImage(uploadService.saveEncryptedFile(frontImage));
            }
            if(backImage != null && !backImage.isEmpty()){
                v.setBackImage(uploadService.saveEncryptedFile(backImage));
            }
            v.setDocumentStatus("PENDING");
        }
        vehicleRepository.save(v);
        return v;
    }

    // === CUSTOMER DELETE ====
    public void deleteVehicle(int vehicleId){
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        String plateNo = decryptSafe(v.getPlateNo());
        List<DocumentaryDto> listDoc = documentaryService.getAllDecrypted();
        for(DocumentaryDto d : listDoc){
            if(d.getDocumentType().contains(plateNo)){
                Documentary doc = documentaryRepository.findById(d.getDocumentId())
                        .orElseThrow(() -> new RuntimeException("Document not found"));
                documentaryRepository.delete(doc);
            }
        }
        vehicleRepository.delete(v);
    }

    // === READ ALL ===
    public List<VehicleDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // === READ BY USER ID ===
    public List<VehicleDto> getByUserId(int userId) {
        return vehicleRepository.findByUser_UserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    // === READ BY PARTNER ID ===
    public List<VehicleDto> getByPartnerId(int partnerId) {
        return vehicleRepository.findByUser_UserId(partnerId).stream()
                .map(this::toDto)
                .toList();
    }

    // === READ BY ID ===
    public Optional<VehicleDto> getById(int id) {
        return vehicleRepository.findById(id)
                .map(this::toDto);
    }

    // === IMAGE DECRYPT ===
    public byte[] getDecryptedImage(String path) throws Exception {
        return uploadService.readAndDecryptFile(path);
    }

    // === DTO MAPPER ===
    public VehicleDto toDto(Vehicle v) {
        VehicleDto dto = new VehicleDto();

        dto.setVehicleId(v.getVehicleId());
        dto.setUserId(v.getUser() != null ? v.getUser().getUserId() : null);
        dto.setYear(v.getYear());
        dto.setVehicleStatus(v.getVehicleStatus());

        dto.setBrand(decryptSafe(v.getBrand()));
        dto.setModel(decryptSafe(v.getModel()));
        dto.setPlateNo(decryptSafe(v.getPlateNo()));

        dto.setFrontImage(buildImageUrl(v.getFrontImage()));
        dto.setBackImage(buildImageUrl(v.getBackImage()));
        dto.setImgTem(buildImageUrl(v.getImgTem()));
        dto.setImgTool(buildImageUrl(v.getImgTool()));
        dto.setImgDevice(buildImageUrl(v.getImgDevice()));

        return dto;
    }

    // === HELPERS ===

    private String encryptSafe(String plain) {
        try {
            return plain != null ? AESEncryptionUtil.encrypt(plain) : null;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed for value: " + plain, e);
        }
    }

    private String decryptSafe(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) return "";
        try {
            return AESEncryptionUtil.decrypt(encrypted);
        } catch (Exception e) {
            System.err.println("❌ Decrypt failed: " + encrypted + " => " + e.getMessage());
            return "***DECRYPT_ERR***";
        }
    }

    private String buildImageUrl(String encryptedPath) {
        final String prefix = "secure_uploads/";
        if (encryptedPath == null || !encryptedPath.startsWith(prefix)) {
            return "/admin/vehicle/image?path=" + prefix + "INVALID_FILE";
        }

        try {
            String encryptedFilename = encryptedPath.substring(prefix.length());
            String decryptedFilename = AESEncryptionUtil.decrypt(encryptedFilename);
            return "/admin/vehicle/image?path=" + prefix + decryptedFilename;
        } catch (Exception e) {
            System.err.println("⚠️ Image filename decrypt failed: " + encryptedPath);
            return "/admin/vehicle/image?path=" + prefix + "INVALID_FILE";
        }
    }


}
