package com.livewithoutthinking.resq.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class VehicleDto {
    private Integer vehicleId;
    private Integer userId;
    private String brand;
    private String model;
    private String plateNo;
    private int year;
    private String frontImage;
    private String backImage;
    private String vehicleStatus;
    private String imgTem;
    private String imgTool;
    private String imgDevice;
}

