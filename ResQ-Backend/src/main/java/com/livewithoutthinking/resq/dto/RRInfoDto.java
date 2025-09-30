package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class RRInfoDto {
    int rrId;
    String description;
    String paymentMethod;
    double total;
}
