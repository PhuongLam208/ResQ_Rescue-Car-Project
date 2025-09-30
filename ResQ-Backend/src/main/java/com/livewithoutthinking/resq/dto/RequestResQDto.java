package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RequestResQDto {
    int rrid;
    int customerId;
    String partnerName;
    String partnerPhone;
    String userName;
    String userPhone;
    String uLocation;
    String destination;
    String reqStatus;
    String rescueType;
    String cancelNote;
    String paymentMethod;
    String paymentStatus;
    String currency;
    String description;
    String note;
    Date createdAt;
    Date startTime;
    Date endTime;
    double fixedPrice;
    double distancePrice;
    double totalPrice;
    double total;
    double appFee;
}
