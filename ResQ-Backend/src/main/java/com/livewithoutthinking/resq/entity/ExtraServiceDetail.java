package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "extraservicedetail")
public class ExtraServiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ESDetailID")
    private int esDetailId; 

    @ManyToOne
    @JoinColumn(name = "ExtraServiceID", referencedColumnName = "ExtraServiceID", nullable = false)
    private ExtraService extraService; // Liên kết với bảng ExtraService

    @ManyToOne
    @JoinColumn(name = "ServiceID", referencedColumnName = "ServiceID", nullable = false)
    private Services service; // Liên kết với bảng Services

    @ManyToOne
    @JoinColumn(name = "BillID", referencedColumnName = "BillID", nullable = false)
    private Bill bill; // Liên kết với bảng Bill
}
