package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentDto {
    private int paymentId;
    private String paypalEmail;
    private String name;
    private String accountName;
    private String accountNo;
    private Date expiredDate;
    private String method;
    private boolean isDefault;
    private Date createdAt;
    private Date updatedAt;
    private String username; // lấy từ payment.user.username
}


