package com.livewithoutthinking.resq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class RescueDetailDTO {
    private String customerName;
    private String customerPhone;
    private String partnerName;
    private String partnerPhone;
    private String rescueType;
    private Date startTime;
    private Date endTime;
    private double appFee;
    private double total;
    private String method;
    private String billStatus;
    private String requestStatus;
    private String cancelNote;

}
