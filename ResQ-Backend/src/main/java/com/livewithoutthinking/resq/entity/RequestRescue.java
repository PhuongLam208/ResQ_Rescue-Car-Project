package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "requestrescue")
public class RequestRescue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RRID")
    private int rrid; 

    @ManyToOne
    @JoinColumn(name = "PartnerID", referencedColumnName = "PartnerID", nullable = true)
    private Partner partner; // Liên kết với bảng Partner (khóa ngoại PartnerID)

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user; // Liên kết với bảng Users (khóa ngoại UserID)

    @Column(name = "StartTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime; // Thời gian bắt đầu

    @Column(name = "EndTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime; // Thời gian kết thúc

    @Column(name = "RescueType")
    private String rescueType; // Loại cứu hộ
    @OneToOne(mappedBy = "requestRescue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Bill bill;
    @Column(name = "Status")
    private String status; // Trạng thái

    @Column(name = "ULocation")
    private String uLocation; // Vị trí người yêu cầu

    @Column(name = "Destination")
    private String destination; // Vị trí đối tác

    @Column(name = "CancelNote")
    private String cancelNote; // Ghi chú hủy yêu cầu cứu hộ

    @ManyToOne
    @JoinColumn(name = "DiscountID", referencedColumnName = "DiscountID")
    private Discount discount; // Liên kết với bảng Discount (khóa ngoại DiscountID)

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    @Column(name="Description")
    private String description;

    @Column(name = "UserLatitude")
    private Double userLatitude;

    @Column(name = "UserLongitude")
    private Double userLongitude;

    @Column(name = "DestLatitude", nullable = true)
    private Double destLatitude;

    @Column(name = "DestLongitude", nullable = true)
    private Double destLongitude;
    @Column(name = "start_address", length = 500)
    private String startAddress;
    @Column(name = "end_address", length = 500, nullable = true)
    private String endAddress;
    public RequestRescue() {

    }

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
