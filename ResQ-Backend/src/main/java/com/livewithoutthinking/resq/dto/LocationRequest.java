package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class LocationRequest {
    private double lat;
    private double lng;
    private double radiusKm;
}
