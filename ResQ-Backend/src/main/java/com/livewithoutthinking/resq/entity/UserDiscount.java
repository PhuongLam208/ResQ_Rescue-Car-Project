package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "userdiscount")
public class UserDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UDID")
    private int udid; 

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user; // Liên kết với bảng Users (khóa ngoại UserID)

    @ManyToOne
    @JoinColumn(name = "DisID", referencedColumnName = "DiscountID", nullable = false)
    private Discount discount; // Liên kết với bảng Discount (khóa ngoại DiscountID)

    @Column(name = "IsUsed", nullable = true)
    private boolean isUsed = Boolean.FALSE;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
