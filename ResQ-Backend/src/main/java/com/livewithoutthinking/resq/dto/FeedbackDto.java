package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class FeedbackDto {
    double rateCustomer;
    double ratePartner;
    double rateRequest;
    String feedbackCustomer;
    String feedbackPartner;
    String feedbackRequest;

    String userName;
    String userPhone;
    String partnerName;
    String partnerPhone;
    String reqStatus;
    String rescueType;

    int rrId;
    int feedbackId;
}
