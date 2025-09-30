package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.PDImageDto;
import com.livewithoutthinking.resq.dto.PersonalDataDto;
import com.livewithoutthinking.resq.entity.Notification;
import com.livewithoutthinking.resq.entity.NotificationTemplate;
import com.livewithoutthinking.resq.entity.PersonalData;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.repository.NotificationRepository;
import com.livewithoutthinking.resq.repository.NotificationTemplateRepository;
import com.livewithoutthinking.resq.repository.PersonalDataRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.service.PersonalDataService;
import com.livewithoutthinking.resq.service.UploadService;
import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PersonalDataServiceImpl implements PersonalDataService {
    @Autowired
    private PersonalDataRepository personalDataRepository;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private NotificationTemplateRepository notiTemplateRepo;
    @Autowired
    private NotificationRepository notiRepo;
    @Autowired
    private UploadService uploadService;

    public PersonalData getUnverifiedUserData(int customerId) {
        User user = userRepo.findUserById(customerId).orElseThrow();
        PersonalData personalData = user.getPersonalData();
        if (personalData != null && "PENDING".equals(personalData.getVerificationStatus())) {
            return personalData;
        }
        return null;
    }

    public boolean approvedCustomer(int customerId){
        boolean approved = false;
        User user = userRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        if(user != null && user.getPersonalData().getVerificationStatus().equals("PENDING")){
            user.getPersonalData().setVerificationStatus("APPROVED");
            user.setStatus("ACTIVE");
            userRepo.save(user);
            personalDataRepository.save(user.getPersonalData());
            approved = true;
        }
        if(approved){
            Notification notification = new Notification();
            NotificationTemplate notiTemplate = notiTemplateRepo.findByNotiType("DOCUMENT_APPROVE");
            notification.setNotificationTemplate(notiTemplate);
            notification.setUser(user);
            notification.setMessage("We have successfully verified your personal data. Your account is now active.");
            notification.setCreatedAt(new Date());
            notiRepo.save(notification);
        }
        return approved;
    };

    public boolean rejectedCustomer(int customerId, String reason){
        boolean rejected = false;
        User user = userRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        if(user != null && user.getPersonalData().getVerificationStatus().equals("PENDING")){
            user.getPersonalData().setVerificationStatus("REJECTED");
            personalDataRepository.save(user.getPersonalData());
            rejected = true;
        }
        if(rejected){
            Notification notification = new Notification();
            NotificationTemplate notiTemplate = notiTemplateRepo.findByNotiType("DOCUMENT_REJECT");
            notification.setNotificationTemplate(notiTemplate);
            notification.setUser(user);
            notification.setMessage(reason);
            notification.setCreatedAt(new Date());
            notiRepo.save(notification);
        }
        return rejected;
    }

    // === CREATE ===
    public PersonalData addPersonalData(String citizenNumber, Date expirationDate, Date issueDate,
                                        String verificationStatus, String issuePlace, String type,
                                        MultipartFile frontImage, MultipartFile backImage, MultipartFile faceImage) throws Exception {

        PersonalData pd = new PersonalData();
        pd.setCitizenNumber(encryptSafe(citizenNumber));
        pd.setIssuePlace(encryptSafe(issuePlace));
        pd.setType(encryptSafe(type));
        pd.setVerificationStatus(verificationStatus);
        pd.setExpirationDate(expirationDate != null ? new java.sql.Date(expirationDate.getTime()) : null);
        pd.setIssueDate(issueDate != null ? new java.sql.Date(issueDate.getTime()) : null);
        pd.setVerifiedAt(new Timestamp(System.currentTimeMillis()));

        // Save + Encrypt filename only
        pd.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        pd.setBackImage(uploadService.saveEncryptedFile(backImage));
        pd.setFaceImage(uploadService.saveEncryptedFile(faceImage));

        return personalDataRepository.save(pd);
    }

    // === READ ALL ===
    public List<PersonalDataDto> getAllDecrypted() {
        return personalDataRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // === READ BY ID ===
    public Optional<PersonalDataDto> getDecryptedById(int id) {
        return personalDataRepository.findById(id)
                .map(this::toDto);
    }

    // === CUSTOMER ADD ===
    public PersonalData addPersonalData(PersonalDataDto dto, int userId,
                                        MultipartFile frontImage, MultipartFile backImage,
                                        MultipartFile faceImage) throws Exception {
        User user = userRepo.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PersonalData pd = new PersonalData();
        pd.setExpirationDate(dto.getExpirationDate());
        pd.setIssueDate(dto.getIssueDate());
        pd.setUser(user);
        // Encrypt sensitive fields (nếu dữ liệu là raw từ client)
        pd.setCitizenNumber(encryptSafe(dto.getCitizenNumber()));
        pd.setIssuePlace(encryptSafe(dto.getIssuePlace()));
        pd.setType(encryptSafe(dto.getType()));
        pd.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        pd.setBackImage(uploadService.saveEncryptedFile(backImage));
        pd.setFaceImage(uploadService.saveEncryptedFile(faceImage));
        pd.setVerificationStatus("PENDING");
        personalDataRepository.save(pd);
        return pd;
    }

    // === CUSTOMER UPDATE ===
    public PersonalData updatePersonalData(PersonalDataDto dto,
                                           MultipartFile frontImage, MultipartFile backImage,
                                           MultipartFile faceImage) throws Exception {
        PersonalData pd = personalDataRepository.findById(dto.getPdId())
                .orElseThrow(() -> new RuntimeException("Personal data not found"));
        pd.setExpirationDate(dto.getExpirationDate());
        pd.setIssueDate(dto.getIssueDate());

        // Encrypt sensitive fields (nếu dữ liệu là raw từ client)
        pd.setCitizenNumber(encryptSafe(dto.getCitizenNumber()));
        pd.setIssuePlace(encryptSafe(dto.getIssuePlace()));
        pd.setType(encryptSafe(dto.getType()));
        pd.setVerificationStatus("PENDING");

        User user = userRepo.findByPersonalData(pd.getPdId())
                .orElseThrow(() -> new RuntimeException("Personal data not found"));
        if(user != null) {
            user.setStatus("WAITING");
            userRepo.save(user);
        }

        if(frontImage != null && !frontImage.isEmpty()){
            pd.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        }
        if (backImage != null && !backImage.isEmpty()) {
            pd.setBackImage(uploadService.saveEncryptedFile(backImage));
        }
        if (faceImage != null && !faceImage.isEmpty()) {
            pd.setFaceImage(uploadService.saveEncryptedFile(faceImage));
        }
        personalDataRepository.save(pd);
        return pd;
    }

    // === IMAGE DECRYPT ===
    public byte[] getDecryptedImage(String path) throws Exception {
        return uploadService.readAndDecryptFile(path);
    }
    public List<PersonalDataDto> getPersonalDataByUserId(int userId) {
        Optional<PersonalData> list = personalDataRepository.findByUser_UserId(userId);
        return list.stream()
                .map(this::toDto)
                .toList();
    }
    // === MAPPER TO DTO ===
    public PersonalDataDto toDto(PersonalData pd) {
        PersonalDataDto dto = new PersonalDataDto();

        dto.setPdId(pd.getPdId());
        dto.setVerificationStatus(pd.getVerificationStatus());
        dto.setExpirationDate(pd.getExpirationDate());
        dto.setIssueDate(pd.getIssueDate());
        dto.setVerifiedAt(pd.getVerifiedAt());

        // Decrypt sensitive fields
        dto.setCitizenNumber(decryptSafe(pd.getCitizenNumber()));
        dto.setIssuePlace(decryptSafe(pd.getIssuePlace()));
        dto.setType(decryptSafe(pd.getType()));

        // Decrypt image path (filename only)
        dto.setFrontImageUrl(buildImageUrl(pd.getFrontImage()));
        dto.setBackImageUrl(buildImageUrl(pd.getBackImage()));
        dto.setFaceImageUrl(buildImageUrl(pd.getFaceImage()));

        return dto;
    }

    public PDImageDto getLatestImageStatusByUserId(int userId) {
        Optional<PersonalData> list = personalDataRepository.findByUser_UserId(userId);
        if (list.isEmpty()) return null;

        // Ưu tiên bản VERIFIED gần nhất
        Optional<PersonalData> verified = list.stream()
                .filter(data -> data.getVerificationStatus().equals("VERIFIED"))
                .max(Comparator.comparing(PersonalData::getVerifiedAt));

        // Nếu không có VERIFIED, lấy bản EXPIRED mới nhất
        Optional<PersonalData> expired = list.stream()
                .filter(data -> data.getVerificationStatus().equals("EXPIRED"))
                .max(Comparator.comparing(PersonalData::getVerifiedAt));

        // Ưu tiên VERIFIED, fallback sang EXPIRED
        PersonalData selected = verified.orElse(expired.orElse(null));
        if (selected == null) return null;

        PDImageDto dto = new PDImageDto();
        dto.setFrontImageUrl(selected.getFrontImage());
        dto.setBackImageUrl(selected.getBackImage());
        dto.setVerificationStatus(selected.getVerificationStatus());

        return dto;
    }

    @Override
    public Optional<PersonalDataDto> getCustomerPd(int userId) {
        return personalDataRepository.findByUserId(userId)
                .map(this::toDto);
    }


    // === HELPERS ===

    public String encryptSafe(String plain) {
        try {
            return plain != null ? AESEncryptionUtil.encrypt(plain) : null;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed for value: " + plain, e);
        }
    }

    public String decryptSafe(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) return "";
        try {
            return AESEncryptionUtil.decrypt(encrypted);
        } catch (Exception e) {
            System.err.println("Decrypt failed: " + encrypted + " => " + e.getMessage());
            return "***DECRYPT_ERR***";
        }
    }

    public String buildImageUrl(String encryptedPath) {
        final String prefix = "secure_uploads/";
        if (encryptedPath == null || !encryptedPath.startsWith(prefix)) return "/admin/personaldoc/image?path=" + prefix + "INVALID_FILE";

        try {
            String encryptedFilename = encryptedPath.substring(prefix.length());
            String decryptedFilename = AESEncryptionUtil.decrypt(encryptedFilename);
            return "/admin/personaldoc/image?path=" + prefix + decryptedFilename;
        } catch (Exception e) {
            System.err.println("Image path decrypt failed: " + encryptedPath);
            return "/admin/personaldoc/image?path=" + prefix + "INVALID_FILE";
        }
    }

}
