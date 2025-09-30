package com.livewithoutthinking.resq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private Integer userId;
    private String token;
    private String fullName;
    private String role;
    private boolean onShift;

}
