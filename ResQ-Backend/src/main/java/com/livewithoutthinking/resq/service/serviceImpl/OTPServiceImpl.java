package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.entity.OtpToken;
import com.livewithoutthinking.resq.repository.OTPRepository;
import com.livewithoutthinking.resq.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OTPServiceImpl implements OTPService {

    @Autowired
    private OTPRepository otpRepository;

    @Override
    public Optional<OtpToken> findByUser_Sdt(String userSdt) {
        return otpRepository.findByUser_Sdt(userSdt);
    }

    @Override
    public Optional<OtpToken> findByOtpCode(String otpCode) {
        return otpRepository.findByOtpCode(otpCode);
    }

    @Override
    public List<OtpToken> findByPhoneNumber(String phoneNumber) {
        return otpRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public void save(OtpToken otp) {
        otpRepository.save(otp);
    }


}
