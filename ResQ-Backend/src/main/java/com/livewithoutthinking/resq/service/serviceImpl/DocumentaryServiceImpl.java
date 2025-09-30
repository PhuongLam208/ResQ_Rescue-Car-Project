package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.DocumentaryDto;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.DocumentaryService;
import com.livewithoutthinking.resq.service.UploadService;
import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class DocumentaryServiceImpl implements DocumentaryService {
    @Autowired
    private DocumentaryRepository documentaryRepository;
    @Autowired
    private PartnerRepository partnerRepo;
    @Autowired
    private NotificationTemplateRepository notiTempRepo;
    @Autowired
    private NotificationRepository notiRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UploadService uploadService;

    public List<Documentary> findByPartnerId_PartnerId(Integer partnerId) {
        return documentaryRepository.findByPartner_PartnerId(partnerId);
    }
    public List<Documentary> getUnverifiedPartnerDoc(Integer partnerId) {
        return documentaryRepository.getUnverifiedPartnerDoc(partnerId);
    }

    public boolean rejectPartner(List<String> documentTypes, int partnerId, String reason) {
        boolean isUpdated = false;
        List<Documentary> unverifiedDocs = documentaryRepository.getUnverifiedPartnerDoc(partnerId);
        Set<String> documentTypeSet = new HashSet<>(documentTypes);

        for (Documentary unverified : unverifiedDocs) {
            String type = unverified.getDocumentType();
            if (documentTypeSet.contains(type)) {
                unverified.setDocumentStatus("REJECTED");
            } else {
                unverified.setDocumentStatus("APPROVED");
            }
            documentaryRepository.save(unverified);
            isUpdated = true;
        }

        Partner partner = partnerRepo.findPartnerById(partnerId);
        if(partner.getResFix() == 2){
            partner.setResFix(4);
        }
        if(partner.getResTow() == 2){
            partner.setResTow(4);
        }
        if(partner.getResDrive() == 2){
            partner.setResDrive(4);
        }
        partnerRepo.save(partner);

        if (isUpdated) {
            Notification notification = new Notification();
            NotificationTemplate notiTemplate = notiTempRepo.findByNotiType("DOCUMENT_REJECT");
            notification.setNotificationTemplate(notiTemplate);
            notification.setUser(partner.getUser());
            notification.setMessage(reason);
            notification.setCreatedAt(new Date());
            notiRepo.save(notification);
        }
        return isUpdated;
    }

    public Documentary addDocumentary(DocumentaryDto dto,
                                      MultipartFile frontImage,
                                      MultipartFile backImage) throws Exception {
        if (dto == null) throw new IllegalArgumentException("DTO cannot be null");

        Documentary doc = new Documentary();

        // Encrypt sensitive fields
        doc.setDocumentType(AESEncryptionUtil.encrypt(dto.getDocumentType()));
        doc.setDocumentNumber(AESEncryptionUtil.encrypt(dto.getDocumentNumber()));
        doc.setDocumentStatus(dto.getDocumentStatus());
        doc.setExpiryDate(dto.getExpiryDate());

        // Associate partner (only set ID to avoid full fetch)
        Partner partner = new Partner();
        partner.setPartnerId(dto.getPartnerId());
        doc.setPartner(partner);

        // Handle image encryption and saving (nullable)
        if (frontImage != null && !frontImage.isEmpty()) {
            doc.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        }

        if (backImage != null && !backImage.isEmpty()) {
            doc.setBackImage(uploadService.saveEncryptedFile(backImage));
        }

        // Timestamps
        Date now = new Date();
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);

        return documentaryRepository.save(doc);
    }

    public List<DocumentaryDto> getAllDecrypted() {
        return documentaryRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<DocumentaryDto> getDecryptedById(int id) {
        return documentaryRepository.findById(id).map(this::toDto);
    }

    public List<DocumentaryDto> getByPartnerId(int partnerId) {
        return documentaryRepository.findByPartner_PartnerId(partnerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public byte[] getDecryptedImage(String decryptedPath) throws IOException {
        File file = new File(decryptedPath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + decryptedPath);
        }
        return Files.readAllBytes(file.toPath());
    }


    public DocumentaryDto toDto(Documentary doc) {
        DocumentaryDto dto = new DocumentaryDto();

        dto.setDocumentId(doc.getDocumentId());
        if(doc.getPartner() !=null){
            dto.setPartnerId(doc.getPartner().getPartnerId());
        }
        dto.setDocumentType(decryptSafe(doc.getDocumentType()));
        dto.setDocumentNumber(decryptSafe(doc.getDocumentNumber()));
        dto.setDocumentStatus(doc.getDocumentStatus());
        dto.setExpiryDate(doc.getExpiryDate());

        dto.setFrontImageUrl("/admin/documentary/image?path=" + decryptPathSafe(doc.getFrontImage()));
        dto.setBackImageUrl("/admin/documentary/image?path=" + decryptPathSafe(doc.getBackImage()));
        return dto;
    }

    public String decryptSafe(String val) {
        try {
            return AESEncryptionUtil.decrypt(val);
        } catch (Exception e) {
            return "***DECRYPT_FAIL***";
        }
    }

    public String decryptPathSafe(String fullPath) {
        final String prefix = "secure_uploads/";
        if (fullPath == null || !fullPath.startsWith(prefix)) return fullPath;

        try {
            String encrypted = fullPath.substring(prefix.length());
            return prefix + AESEncryptionUtil.decrypt(encrypted);
        } catch (Exception e) {
            return prefix + "INVALID_FILE";
        }
    }

    // === CUSTOMER ADD ===
    public Documentary addCusDoc(DocumentaryDto documentaryDto, int userId,
                                 MultipartFile frontImage, MultipartFile backImage) throws Exception {
        Documentary doc = new Documentary();
        User user = userRepo.findUserById(userId)
                .orElseThrow(() -> new Exception("User Not Found"));
        // Encrypt sensitive fields
        doc.setDocumentType(AESEncryptionUtil.encrypt(documentaryDto.getDocumentType()));
        doc.setDocumentNumber(AESEncryptionUtil.encrypt(documentaryDto.getDocumentNumber()));
        doc.setDocumentStatus("PENDING");
        doc.setExpiryDate(documentaryDto.getExpiryDate());
        doc.setUser(user);
        doc.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        doc.setBackImage(uploadService.saveEncryptedFile(backImage));
        doc.setCreatedAt(new Date());
        documentaryRepository.save(doc);
        return doc;
    }

    // === CUSTOMER UPDATE ===
    public Documentary updateCusDoc(DocumentaryDto documentaryDto, MultipartFile frontImage,
                                    MultipartFile backImage) throws Exception {
        Documentary doc = documentaryRepository.findById(documentaryDto.getDocumentId())
                .orElseThrow(() -> new Exception("Document Not Found"));
        doc.setDocumentType(AESEncryptionUtil.encrypt(documentaryDto.getDocumentType()));
        doc.setDocumentNumber(AESEncryptionUtil.encrypt(documentaryDto.getDocumentNumber()));
        doc.setDocumentStatus("PENDING");
        doc.setExpiryDate(documentaryDto.getExpiryDate());
        if (frontImage != null && !frontImage.isEmpty()) {
            doc.setFrontImage(uploadService.saveEncryptedFile(frontImage));
        }
        if (backImage != null && !backImage.isEmpty()) {
            doc.setBackImage(uploadService.saveEncryptedFile(backImage));
        }
        doc.setUpdatedAt(new Date());
        documentaryRepository.save(doc);
        return doc;
    }

    // === CUSTOMER DELETE ===
    public void deleteDocument(int documentId){
        Documentary document = documentaryRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        documentaryRepository.delete(document);
    }

    public List<DocumentaryDto> getByUserId(int userId) throws Exception {
        List<DocumentaryDto> result = new ArrayList<>();
        List<Documentary> listDocs =  documentaryRepository.findByUser_UserId(userId);
        for (Documentary doc : listDocs) {
            DocumentaryDto dto = new DocumentaryDto();
            dto.setDocumentId(doc.getDocumentId());
            dto.setDocumentType(decryptSafe(doc.getDocumentType()));
            dto.setDocumentNumber(decryptSafe(doc.getDocumentNumber()));
            dto.setExpiryDate(doc.getExpiryDate());
            System.out.println(doc.getFrontImage());
            dto.setFrontImageUrl("/admin/documentary/image?path=" + decryptPathSafe(doc.getFrontImage()));
            dto.setBackImageUrl("/admin/documentary/image?path=" + decryptPathSafe(doc.getFrontImage()));
            result.add(dto);
        }
        return result;
    }
}
