package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "services")
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceID")
    private int serviceId; 

    @Column(name = "ServiceType", nullable = false)
    private String serviceType; // Phân loại dịch vụ (ResFix, ResTow, ResDrive)

    @Column(name = "ServiceName", nullable = false)
    private String serviceName; // Tên dịch vụ

    @Column(name = "FixedPrice")
    private double fixedPrice; // Giá cố định (nếu có)

    @Column(name = "PricePerKm")
    private double pricePerKm; // Giá theo km (nếu có)

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật
}
