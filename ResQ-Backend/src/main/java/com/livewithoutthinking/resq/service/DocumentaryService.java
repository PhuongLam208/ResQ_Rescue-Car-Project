package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.DocumentaryDto;
import com.livewithoutthinking.resq.entity.Documentary;
import com.livewithoutthinking.resq.entity.Partner;
import com.livewithoutthinking.resq.repository.DocumentaryRepository;
import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DocumentaryService {
    List<Documentary> findByPartnerId_PartnerId(Integer partnerId);
    List<Documentary> getUnverifiedPartnerDoc(Integer partnerId);
    boolean rejectPartner(List<String> documentTypes, int partnerId, String reason);
    Documentary addDocumentary(DocumentaryDto dto, MultipartFile frontImage, MultipartFile backImage) throws Exception;
    List<DocumentaryDto> getAllDecrypted();
    Optional<DocumentaryDto> getDecryptedById(int id);
    List<DocumentaryDto> getByPartnerId(int partnerId);
    byte[] getDecryptedImage(String decryptedPath) throws IOException;
    DocumentaryDto toDto(Documentary doc);
    String decryptSafe(String val);
    String decryptPathSafe(String fullPath);

    List<DocumentaryDto> getByUserId(int userId) throws Exception;
    Documentary addCusDoc(DocumentaryDto documentaryDto, int userId,
                          MultipartFile frontImage, MultipartFile backImage) throws Exception;
    Documentary updateCusDoc(DocumentaryDto documentaryDto, MultipartFile frontImage,
                             MultipartFile backImage) throws Exception;
    void deleteDocument(int documentId);
}
