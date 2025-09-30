package com.livewithoutthinking.resq.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter

public class RequestRescueDto {
    private Integer rrid;
    private String customerName;
    private Integer userId;
    private String partnerName;
    private Integer partnerId;

    public RequestRescueDto(Integer rrid, String customerName, Integer userId,
                            String partnerName, Integer partnerId) {
        this.rrid = rrid;
        this.customerName = customerName;
        this.userId = userId;
        this.partnerName = partnerName;
        this.partnerId = partnerId;
    }
}
