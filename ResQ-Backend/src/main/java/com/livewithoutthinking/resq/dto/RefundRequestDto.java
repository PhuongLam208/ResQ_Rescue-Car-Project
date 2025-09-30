package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequestDto {
    private int senderId;
    private int rrid;
    private int userId;
    private BigDecimal amount;
    private String reason;


}
