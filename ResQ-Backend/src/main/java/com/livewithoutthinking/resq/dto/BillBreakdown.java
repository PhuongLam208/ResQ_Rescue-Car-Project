package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class BillBreakdown {
    private double servicePrice;
    private double distancePrice;
    private double appFee;
    private double discount;

    // constructor, getters, setters
}

