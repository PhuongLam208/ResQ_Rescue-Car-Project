package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class RecordStatusDto {
    private boolean hasFeedbacks;
    private boolean hasPartnerReport;
    private boolean hasCustomerReport;
}
