package com.livewithoutthinking.resq.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class UserDto {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String sdt;
    private String status;
    private Date dob;
    private String gender;
    private String address;
    private String avatar;
    private String pdStatus;
    private boolean phoneVerified;
    private String currentPassword;
    private String password;
    private Date createdAt;
    private Date updatedAt;
    private String language;
    private String appColor;
    private int loyaltyPoint;
    private int totalRescues;
    private String roleName;
    private int role;
}

