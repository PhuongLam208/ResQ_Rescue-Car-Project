package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OTPRepository extends JpaRepository<OtpToken, Integer> {

    Optional<OtpToken> findByUser_Sdt(String userSdt);
    Optional<OtpToken> findByOtpCode(String otpCode);
    List<OtpToken> findByPhoneNumber(String phoneNumber);
    OtpToken save(OtpToken otp);

}
