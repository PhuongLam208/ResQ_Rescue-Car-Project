package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class StaffDto {
    int staffId;
    int userId;
    int monthLateCount;
    String fullName;
    String userName;
    String email;
    String sdt;
    String avatar;
    String password;
    String address;
    Date createdAt;
    String status;
    int totalRescues;
    double responseTime;
}
