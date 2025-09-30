// DTO: PartnerRegistrationRequest.java
package com.livewithoutthinking.resq.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PartnerRegistrationRequest {
    private Integer userId;
    private int resFix;
    private int resTow;
    private int resDrive;
    private List<Integer> selectedServiceIds;

    private String licenseExpiryDate; // cho FIX
    private String towLicenseExpiryDate;
    private String towInspectionExpiryDate;
    private String towSpecialPermitExpiryDate;
    private String driveLicenseExpiryDate;


    // Fix
    private String licenseNumber;
    private MultipartFile documentFront;
    private MultipartFile documentBack;

    // Tow
    private String towLicenseNumber;
    private MultipartFile towLicenseFront;
    private MultipartFile towLicenseBack;

    private String towInspectionNumber;
    private MultipartFile towInspectionFront;
    private MultipartFile towInspectionBack;

    private String towSpecialPermitNumber;
    private MultipartFile towSpecialPermitFront;
    private MultipartFile towSpecialPermitBack;

    private MultipartFile driveVehicleImage;
    private MultipartFile driveLicensePlateImage;

    // Drive
    private String driveLicenseNumber;
    private MultipartFile driveLicenseFront;
    private MultipartFile driveLicenseBack;
}