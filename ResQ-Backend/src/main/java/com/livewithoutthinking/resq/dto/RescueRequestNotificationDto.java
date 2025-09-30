package com.livewithoutthinking.resq.dto;


import lombok.Data;

@Data
public class RescueRequestNotificationDto {
   private Integer rrid;
   private double startLatitude;
   private double startLongitude;
   private double endLatitude;
   private double toLongitude;
   private String endLongitude;
   private String userFullName;
   private String From;
   private String To;
   private String serviceType;
   private double estimatedPrice;
   private double discountAmount;
   private double finalPrice;
   private String paymentMethod;
}