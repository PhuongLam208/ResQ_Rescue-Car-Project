package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class HomeProfileDto {

    private String status;
    private String userName;
    private boolean hasPD;
    private String avatar;
    private Integer loyaltyPoint;
}
