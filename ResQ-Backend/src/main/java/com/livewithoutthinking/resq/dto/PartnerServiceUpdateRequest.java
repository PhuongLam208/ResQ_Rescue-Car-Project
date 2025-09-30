// DTO
package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.List;

@Data
public class PartnerServiceUpdateRequest {
    private Integer userId;
    private String type;
    private List<Integer> serviceIds;
}