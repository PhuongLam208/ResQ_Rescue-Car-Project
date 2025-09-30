package com.livewithoutthinking.resq.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnerSearchDto {
    private Integer userId;
    private String fullName;
    private String username;

    public PartnerSearchDto(String fullName, String username, Integer userId) {
        this.fullName = fullName;
        this.username = username;
        this.userId = userId;
    }
}
