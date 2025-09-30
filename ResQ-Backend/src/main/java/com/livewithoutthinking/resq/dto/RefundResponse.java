package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundResponse {
    private int refundId;
    private String staffName;
    private String recipientName;
    private String userName;
    private BigDecimal amount;
    private String reason;
    private String status;
    private int conversationId;

}
