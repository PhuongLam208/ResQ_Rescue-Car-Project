package com.livewithoutthinking.resq.dto;


import lombok.Data;

@Data

public class BillResponse {
    private Integer rrid;
    private Integer billId;
    private double total;
    private String currency;
    private String method;
    private String startAddress;
    private String endAddress;
    private BillBreakdown breakdown;

}

