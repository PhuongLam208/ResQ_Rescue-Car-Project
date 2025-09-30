package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserDiscountDto {
    private int udId;
    private int userId;
    private int discountId;
    private BigDecimal amount;
    private String code;
    private String name;
    private boolean isPercent = false;
}
