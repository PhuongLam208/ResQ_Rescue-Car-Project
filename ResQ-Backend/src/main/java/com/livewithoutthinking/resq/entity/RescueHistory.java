package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "rescuehistory")
public class RescueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RRHistoryID")
    private int rrHistoryId; 

    @ManyToOne
    @JoinColumn(name = "RRID", referencedColumnName = "RRID", nullable = false)
    private RequestRescue requestRescue; // Liên kết với bảng RequestRescue

    @ManyToOne
    @JoinColumn(name = "BillID", referencedColumnName = "BillID", nullable = false)
    private Bill bill; // Liên kết với bảng Bill

    @ManyToOne
    @JoinColumn(name = "ServiceID", referencedColumnName = "ServiceID", nullable = false)
    private Services service; // Liên kết với bảng Services

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật
}
