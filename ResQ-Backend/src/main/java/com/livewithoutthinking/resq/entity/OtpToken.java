package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Entity
@Table(name = "otp_token")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int otpId;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = true)
    private User user;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "OtpCode", nullable = false)
    private String otpCode;

    @Column(name = "OtpType", nullable = false)
    private String otpType; // REGISTER, FORGOT PASSWORD

    @Column(name = "fail_count", nullable = false)
    private int failCount = 0;

    @Column(name = "request_count", nullable = false)
    private int requestCount = 0;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public boolean isExpired() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).isAfter(expiredAt);
    }

    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).isBefore(blockedUntil);
    }

    public void incrementFailCount() {
        this.failCount++;
        if (this.failCount >= 5) {
            this.blockedUntil = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusHours(24);
        }
    }

    public void incrementRequestCount() {
        this.requestCount++;
        if (this.requestCount >= 10) {
            this.blockedUntil = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusHours(24);
        }
    }

    public boolean canRequestNewOtp() {
        return !isBlocked();
    }

    public boolean canVerify() {
        return !isBlocked() && !isExpired();
    }

}
