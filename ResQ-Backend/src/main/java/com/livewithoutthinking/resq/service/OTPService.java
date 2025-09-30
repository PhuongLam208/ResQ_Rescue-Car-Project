package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.entity.OtpToken;

import java.util.List;
import java.util.Optional;

public interface OTPService {
    Optional<OtpToken> findByUser_Sdt(String userSdt);
    Optional<OtpToken> findByOtpCode(String otpCode);
    List<OtpToken> findByPhoneNumber(String phoneNumber);
    void save(OtpToken otp);
}
