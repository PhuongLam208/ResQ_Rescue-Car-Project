package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class ServiceDto {
    int srvId;
    String srvName;
    String srvType;
    Double srvPrice;
    boolean fixedPrice;
}
