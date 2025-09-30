package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "requestService")
public class RequestService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="RequestSrvId")
    private int requestSrvId;

    @ManyToOne
    @JoinColumn(name = "RrId", referencedColumnName = "RrId", nullable = false)
    private RequestRescue request;

    @ManyToOne
    @JoinColumn(name = "ServiceId", referencedColumnName = "ServiceId", nullable = false)
    private Services service; // Liên kết với bảng Users qua UserID
}
