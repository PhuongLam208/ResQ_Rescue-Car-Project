package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RegisterDto {
    private String fullName;
    private String email;
    private String password;
    private Date dob;
    private String gender;
    private String sdt;
}
