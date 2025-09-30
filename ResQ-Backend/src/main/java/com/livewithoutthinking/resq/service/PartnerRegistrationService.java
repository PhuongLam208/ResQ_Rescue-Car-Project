// Service Interface: PartnerRegistrationService.java
package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.PartnerRegistrationRequest;
import com.livewithoutthinking.resq.dto.PartnerServiceUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface PartnerRegistrationService {
    ResponseEntity<?> registerPartner(PartnerRegistrationRequest request);
    ResponseEntity<?> getServicesByType(String type);
    ResponseEntity<?> getRegisteredPartnerTypes(Integer userId);
    public ResponseEntity<?> getPartnerInfo(Integer userId);
    ResponseEntity<?> getSelectedServiceIds(Integer userId, String type);
    ResponseEntity<?> updateSelectedServices(PartnerServiceUpdateRequest request);
    boolean isPartnerRegistered(Integer userId);
    ResponseEntity<?> getPartnerDocuments(Integer userId);
    // PartnerRegistrationService.java
    Map<String, Object> getPartnerDocumentMap(Integer userId);
    ResponseEntity<?> cancelPartnerType(Integer userId, String type);
    ResponseEntity<?> updateOnlineStatus(Integer userId, boolean status);
    public ResponseEntity<?> updateDocument(Integer documentId, String documentNumber,
                                            Integer userId,
                                            MultipartFile frontImage, MultipartFile backImage);
    ResponseEntity<?> getUserFullname(Integer userId);
    ResponseEntity<?> getUserAvatar(Integer userId);

}