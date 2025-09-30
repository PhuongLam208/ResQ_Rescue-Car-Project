package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.List;

@Data
public class VerifiedUserDto {
    private List<String> documentTypes;
    private String reason;
}
