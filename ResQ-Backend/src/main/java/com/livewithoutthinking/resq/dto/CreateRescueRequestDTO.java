package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class CreateRescueRequestDTO {
    private Integer userId;
    private String rescueType;
    private Double startLat;
    private Double startLng;
    private String startAddress;
    private Double endLat;
    private Double endLng;
    private String endAddress;
    private Double distanceKm;
    private String promoCode;
    private String paymentMethod;
}
