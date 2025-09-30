package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VehicleID")
    private int vehicleId; 

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID")
    private User user; // Liên kết với bảng Users qua UserID

    @Column(name = "PlateNo")
    private String plateNo; // Biển số xe

    @Column(name = "Brand")
    private String brand; // Hãng xe

    @Column(name = "Model")
    private String model; // Mẫu xe

    @Column(name = "Year")
    private int year; // Năm sản xuất

    @Column(name = "FrontImage")
    private String frontImage; // Hình ảnh phía trước xe

    @Column(name = "BackImage")
    private String backImage; // Hình ảnh phía sau xe

    @Column(name = "VehicleStatus")
    private String vehicleStatus; // Trạng thái xe (ví dụ: "Đang hoạt động", "Hư hỏng", v.v.)

    @Column(name = "DocumentStatus")
    private String documentStatus; // Trạng thái giấy tờ (ví dụ: "Đã xác minh", "Chưa xác minh", v.v.)

    @Column(name = "ImgTem")
    private String imgTem; // Hình ảnh tem xe

    @Column(name = "ImgTool")
    private String imgTool; // Hình ảnh công cụ kiểm tra xe

    @Column(name = "ImgDevice")
    private String imgDevice; // Hình ảnh thiết bị xe

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật gần nhất

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
