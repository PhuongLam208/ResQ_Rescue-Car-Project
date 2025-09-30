package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.entity.OtpToken;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.service.OTPService;
import com.livewithoutthinking.resq.service.UserService;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/api/resq/verify")
public class TwilioVerifyController {

    @Value("${twilio.account.sid}")
    private String accountSid;
    @Value("${twilio.auth.token}")
    private String authToken;
    @Value("${twilio.verify.sid}")
    private String verifySid;

    @Autowired
    private OTPService otpService;
    @Autowired
    private UserService userService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber) {
        try {
            Twilio.init(accountSid, authToken);
            String code = generateOtp();

            List<OtpToken> otpTokens = otpService.findByPhoneNumber(phoneNumber);

            // Tìm OTP loại "FORGOT PASSWORD" trong danh sách
            OtpToken token = otpTokens.stream()
                    .filter(t -> "REGISTER".equalsIgnoreCase(t.getOtpType()))
                    .findFirst()
                    .orElse(null);

            if (token == null) {
                OtpToken newAccount = new OtpToken();
                newAccount.setPhoneNumber(phoneNumber);
                newAccount.setOtpType("REGISTER");
                setCode(newAccount, code);
                otpService.save(newAccount);

                sendOtpViaWhatsApp(phoneNumber, code);
                return ResponseEntity.status(200).body("OTP sent successfully");
            }

            OtpToken oldAccount = token;
            if (oldAccount.canRequestNewOtp()) {
                setCode(oldAccount, code);
                oldAccount.incrementRequestCount();
                otpService.save(oldAccount);

                sendOtpViaWhatsApp(phoneNumber, code);
                return ResponseEntity.status(200).body("OTP sent successfully");
            }
            return ResponseEntity.status(403)
                    .body("This phone number has been blocked due to exceeding OTP limits. " +
                            "Please try again after 24 hours.");

        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/check-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String phoneNumber = payload.get("phoneNumber");
        String code = payload.get("code");
        String otpType = payload.get("otpType");
        try {
            Twilio.init(accountSid, authToken);

            List<OtpToken> tokens = otpService.findByPhoneNumber(phoneNumber).stream()
                    .filter(t -> t.getOtpType().equalsIgnoreCase(otpType))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();

            if (tokens.isEmpty()) {
                return ResponseEntity.status(400).body("Invalid phone number or OTP type");
            }

            OtpToken otpToken = tokens.get(0);

            if (otpToken.isBlocked()) {
                return ResponseEntity.status(403).body("Phone number is blocked due to too many failed attempts.");
            }

            if (otpToken.isExpired()) {
                return ResponseEntity.status(400).body("OTP has expired");
            }

            if (!otpToken.getOtpCode().equals(code)) {
                otpToken.incrementFailCount();
                otpService.save(otpToken);
                return ResponseEntity.status(400).body("Incorrect OTP");
            }

            return ResponseEntity.ok("Phone number verified successfully");

        } catch (ApiException e) {
            return ResponseEntity.status(400).body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPasswordOtp(@RequestParam String phoneNumber) {
        try {
            Twilio.init(accountSid, authToken);
            String code = generateOtp();

            // Tìm user
            Optional<User> userOpt = userService.findByPhoneNumber(phoneNumber);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(400).body("Wrong Phone Number");
            }
            User user = userOpt.get();

            // Tìm danh sách OTP với SDT đó
            List<OtpToken> otpTokens = otpService.findByPhoneNumber(phoneNumber);

            // Tìm OTP loại "FORGOT PASSWORD" trong danh sách
            OtpToken otpToken = otpTokens.stream()
                    .filter(t -> "FORGOT PASSWORD".equalsIgnoreCase(t.getOtpType()))
                    .findFirst()
                    .orElse(null);

            if (otpToken == null) {
                // Nếu chưa tồn tại, tạo mới
                OtpToken newToken = new OtpToken();
                newToken.setPhoneNumber(phoneNumber);
                newToken.setOtpType("FORGOT PASSWORD");
                newToken.setUser(user);
                setCode(newToken, code);
                otpService.save(newToken);

                sendOtpViaWhatsApp(phoneNumber, code);
                return ResponseEntity.ok("OTP sent successfully");
            }

            // Nếu đã có, kiểm tra có thể gửi lại không
            if (otpToken.canRequestNewOtp()) {
                otpToken.incrementRequestCount();
                setCode(otpToken, code);
                otpService.save(otpToken);

                sendOtpViaWhatsApp(phoneNumber, code);
                return ResponseEntity.ok("OTP sent successfully");
            }

            return ResponseEntity.status(403).body(
                    "This phone number has been blocked due to exceeding OTP limits. " +
                            "Please try again after 24 hours."
            );

        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/update-phone/{userId}")
    public ResponseEntity<?> updatePhoneNumberOtp(
            @PathVariable int userId,
            @RequestParam String phoneNumber) {
        try {
            Twilio.init(accountSid, authToken);
            String code = generateOtp();

            // Tìm user
            Optional<User> userPhone = userService.findByPhoneNumber(phoneNumber);
            if (userPhone.isPresent()) {
                return ResponseEntity.status(400).body("This Phone number already exists");
            }

            Optional<User> userOtp = userService.findById(userId);
            if (userOtp.isEmpty()) {
                return ResponseEntity.status(400).body("User not found");
            }
            User user = userOtp.get();
            // Tìm danh sách OTP với SDT đó
            List<OtpToken> otpTokens = otpService.findByPhoneNumber(phoneNumber);

            // Tìm OTP loại "FORGOT PASSWORD" trong danh sách
            OtpToken otpToken = otpTokens.stream()
                    .filter(t -> "VERIFY".equalsIgnoreCase(t.getOtpType()))
                    .findFirst()
                    .orElse(null);

            if (otpToken == null) {
                // Nếu chưa tồn tại, tạo mới
                OtpToken newToken = new OtpToken();
                newToken.setPhoneNumber(phoneNumber);
                newToken.setOtpType("VERIFY");
                newToken.setUser(user);
                setCode(newToken, code);
                otpService.save(newToken);

                sendOtpViaWhatsApp(phoneNumber, code);
                return ResponseEntity.ok("OTP sent successfully");
            }

            // Nếu đã có, kiểm tra có thể gửi lại không
            if (otpToken.canRequestNewOtp()) {
                otpToken.incrementRequestCount();
                setCode(otpToken, code);
                otpService.save(otpToken);

                sendOtpViaWhatsApp(phoneNumber, code);
                return ResponseEntity.ok("OTP sent successfully");
            }

            return ResponseEntity.status(403).body(
                    "This phone number has been blocked due to exceeding OTP limits. " +
                            "Please try again after 24 hours."
            );

        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("Failed to send OTP: " + e.getMessage());
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // tạo số từ 100000 đến 999999
        return String.valueOf(otp);
    }

    private static String formatToTwilio(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("\\D+", ""); // loại bỏ mọi ký tự không phải số
        if (phoneNumber.startsWith("0")) {
            return "+84" + phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("84")) {
            return "+" + phoneNumber;
        }
        return null; // không hợp lệ
    }

    private OtpToken setCode(OtpToken token, String code) {
        token.setOtpCode(code);
        token.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        token.setExpiredAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusMinutes(3));
        return token;
    }

    private void sendOtpViaWhatsApp(String phoneNumber, String code) {
        Message.creator(
                new PhoneNumber("whatsapp:" + formatToTwilio(phoneNumber)),
                new PhoneNumber("whatsapp:+14155238886"),
                "Your OTP code is: " + code +
                        ". This OTP code will expire after 3 minutes. " +
                        "Please do not give this code to anyone else."
        ).create();
    }
}
