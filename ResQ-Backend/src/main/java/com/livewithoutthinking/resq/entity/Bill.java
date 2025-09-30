package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name = "bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BillID")
    private int billId;

    @OneToOne
    @JoinColumn(name = "rrid")
    private RequestRescue requestRescue;

    @Column(name = "ServicePrice")
    private double servicePrice; // Giá dịch vụ
    
    @Column(name = "DistancePrice")
    private double distancePrice; // Giá dịch vụ theo khoảng cách

    @Column(name = "ExtraPrice")
    private double extraPrice; // Giá dịch vụ phụ (nếu có)

    @Column(name = "TotalPrice")
    private double totalPrice; // Tổng số tiền cần thanh toán
    
    @ManyToOne
    @JoinColumn(name = "PaymentID", referencedColumnName = "PaymentID")
    private Payment payment; // Liên kết với bảng Payment

    @Column(name = "AppFee")
    private double appFee; // Giá dịch vụ theo loại

    @Column(name = "DiscountAmount")
    private double discountAmount; // Số tiền giảm
    
    @Column(name = "Total")
    private double total; // Giá đã được giảm

    @Column(name = "Method")
    private String method;

    @Column(name = "Status")
    private String status;

    @Column(name = "Currency")
    private String currency; // "VND" hoặc "USD"

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt; // Thời gian cập nhật
}
