package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.PDImageDto;
import com.livewithoutthinking.resq.dto.PersonalDataDto;
import com.livewithoutthinking.resq.entity.PersonalData;
import com.livewithoutthinking.resq.repository.PersonalDataRepository;
import com.livewithoutthinking.resq.util.AESEncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PersonalDataService {
    PersonalData getUnverifiedUserData(int customerId);
    boolean approvedCustomer(int customerId);
    boolean rejectedCustomer(int customerId, String reason);
    List<PersonalDataDto> getAllDecrypted();
    Optional<PersonalDataDto> getDecryptedById(int id);
    byte[] getDecryptedImage(String path) throws Exception;
    List<PersonalDataDto> getPersonalDataByUserId(int userId);
    PersonalDataDto toDto(PersonalData pd);
    String encryptSafe(String plain);
    String decryptSafe(String encrypted);
    String buildImageUrl(String encryptedPath);
    PersonalData addPersonalData(String citizenNumber, Date expirationDate, Date issueDate,
                                 String verificationStatus, String issuePlace, String type,
                                 MultipartFile frontImage, MultipartFile backImage, MultipartFile faceImage) throws Exception;

    PersonalData addPersonalData(PersonalDataDto dto, int userId, MultipartFile frontImage,
                                 MultipartFile backImage, MultipartFile faceImage) throws Exception;


    PersonalData updatePersonalData(PersonalDataDto dto, MultipartFile frontImage,
                                    MultipartFile backImage, MultipartFile faceImage) throws Exception;

    PDImageDto getLatestImageStatusByUserId(int userId);

    Optional<PersonalDataDto> getCustomerPd(int userId);
}
