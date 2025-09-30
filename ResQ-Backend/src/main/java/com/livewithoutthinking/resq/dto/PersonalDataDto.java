package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
public class PersonalDataDto {
    private int pdId;

    private String citizenNumber;
    private String issuePlace;
    private String type;

    private String verificationStatus;
    private Date expirationDate;
    private Date issueDate;
    private Timestamp verifiedAt;

    // URL để truy cập ảnh sau khi giải mã đường dẫn
    private String frontImageUrl;
    private String backImageUrl;
    private String faceImageUrl;
}
