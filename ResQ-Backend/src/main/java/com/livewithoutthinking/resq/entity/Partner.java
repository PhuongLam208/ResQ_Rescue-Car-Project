package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "partner")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PartnerID")
    private Integer partnerId;

    @OneToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user; // Liên kết với bảng Users qua UserID

    @Column(name = "ResFix")
    private int resFix = 0; // Loại đối tác sửa tại chỗ
    
    @Column(name = "ResTow")
    private int resTow = 0; // Loại đối tác kéo xe
    
    @Column(name = "ResDrive")
    private int resDrive = 0; // Loại đối tác lái thay

    @Column(name = "Location")
    private String location; // Địa chỉ đối tác

    @Column(name = "VerificationStatus")
    private boolean verificationStatus = false; // Trạng thái xác minh (0 hoặc 1)

    @Column(name = "AvgTime")
    private float avgTime = 0.0f; // Thời gian trung bình

    @Column(name = "OnWorking")
    private boolean onWorking = false; // Trạng thái xác minh (0 hoặc 1)

    @Column(name = "WalletAmount", precision = 10, scale = 2)
    private BigDecimal walletAmount = BigDecimal.ZERO;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo đối tác

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật đối tác

    @OneToMany(mappedBy = "partner")
    private List<Report> reports;

    @ManyToOne
    @JoinColumn(name = "VehicleID", referencedColumnName = "VehicleID")
    private Vehicle vehicle;

    @Column(name = "latitude", nullable = true)
    private double latitude = 0;
    @Column(name = "longitude",  nullable = true)
    private double longitude = 0;

    @Column(name = "block_until")
    private LocalDateTime blockUntil;

    @Column(name = "status")
    private String status;

    // Constructor, Getters, Setters và các phương thức khác nếu cần

    public void deductWalletAmount() {
        BigDecimal fixedAmount = BigDecimal.valueOf(50000);
        this.walletAmount = this.walletAmount.subtract(fixedAmount);
        this.updatedAt = new Date();
    }

    public void deductWalletAmount(double amount) {
        BigDecimal subAmount = BigDecimal.valueOf(amount);
        this.walletAmount = this.walletAmount.subtract(subAmount);
        this.updatedAt = new Date();
    }

    public void addWalletAmount(double amount) {
        BigDecimal addAmount = BigDecimal.valueOf(amount);
        this.walletAmount = this.walletAmount.add(addAmount);
        this.updatedAt = new Date();
    }
}
