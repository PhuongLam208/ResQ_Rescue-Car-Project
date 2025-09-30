package com.livewithoutthinking.resq.dto;


import lombok.Data;

import java.util.Date;

@Data
public class PartnerDto {
    private Integer partnerId;
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String sdt;
    private String location;
    private String partnerAddress;
    private String status;
    private String avatar;

    private int resFix;
    private int resTow;
    private int resDrive;
    private boolean verificationStatus;
    private float avgTime;
    private Date createdAt;
    private Date updatedAt;
}

