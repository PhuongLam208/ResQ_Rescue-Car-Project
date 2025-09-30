package com.livewithoutthinking.resq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NearbyPartnerDTO {
    private Integer id;
    private String name;
    private double latitude;
    private double longitude;
    private double distanceKm;
}

