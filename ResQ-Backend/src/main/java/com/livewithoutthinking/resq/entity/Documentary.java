package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@Table(name = "documentary")
public class Documentary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentID")
    private int documentId; 

    @ManyToOne
    @JoinColumn(name = "PartnerID", referencedColumnName = "PartnerID")
    private Partner partner; // Liên kết với bảng Partner qua PartnerID

    @ManyToOne
    @JoinColumn(name = "VehicleID", referencedColumnName = "VehicleID")
    private Vehicle vehicle; // Liên kết với bảng Vehicle qua VehicleID

    @Column(name = "DocumentType")
    private String documentType; // Loại giấy tờ (ví dụ: "Giấy đăng ký", "Bảo hiểm", v.v.)

    @Column(name = "DocumentNumber")
    private String documentNumber; // Số giấy tờ (ví dụ: Số giấy đăng ký xe)

    @Column(name = "FrontImage", nullable = true)
    private String frontImage;

    @Column(name = "BackImage", nullable = true)
    private String backImage;

    @Column(name = "DocumentStatus")
    private String documentStatus;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;  // dùng java.time.LocalDate cho kiểu DATE

    @Column(name = "ResType")
    private String resType;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    @ManyToOne
    @JoinColumn(name = "UserId", referencedColumnName = "UserID")
    private User user;

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
