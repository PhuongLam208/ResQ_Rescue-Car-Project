package com.livewithoutthinking.resq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RescueRequestAcceptedDto {
    private int rrid;
    private int partnerId;
    private double partnerLat;
    private double partnerLon;
    private double distanceKm;

    private String vehicleFrontImage;
    private String vehicleModel;
    private String vehicleBrand;
    private String vehicleStatus;
}
