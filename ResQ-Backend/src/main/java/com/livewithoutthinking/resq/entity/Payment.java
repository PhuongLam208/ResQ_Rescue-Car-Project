package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private int paymentId;

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user; // Liên kết với bảng Users qua UserID

    @Column(name="Name")
    private String name;

    @Column(name = "PaypalEmail", nullable = false)
    private String paypalEmail;

    @Column(name="Created_at")
    private Date createdAt;

    @Column(name="Updated_at")
    private Date updatedAt;

    @Column(name = "Method", nullable = false)
    private String method;
}
