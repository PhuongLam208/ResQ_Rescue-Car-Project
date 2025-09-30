package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "extraservice")
public class ExtraService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ExtraServiceID")
    private int extraServiceId; 

    @Column(name = "Reason")
    private String reason; // Lý do yêu cầu dịch vụ phụ

    @ManyToOne
    @JoinColumn(name = "RequestRescueID", referencedColumnName = "RRID", nullable = false)
    private RequestRescue requestRescue; // Liên kết với bảng RequestRescue

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user; // Liên kết với bảng Users

    @Column(name = "Price", nullable = false)
    private double price; // Giá của dịch vụ phụ

    @ManyToOne
    @JoinColumn(name = "ServiceID", referencedColumnName = "ServiceID", nullable = true)
    private Services services; // Liên kết với bảng VehicleIssue (có thể NULL nếu không có sự cố)
}
