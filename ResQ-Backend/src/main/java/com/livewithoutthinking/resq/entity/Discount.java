package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "discount")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DiscountID")
    private int discountId; 

    @Column(name = "Amount", nullable = false)
    private BigDecimal amount; // Số tiền giảm giá

    @Column(name = "Type", nullable = false)
    private String type; // Loại giảm giá ( Money / Percent )

    @Column(name = "TypeDis", nullable = false)
    private String typeDis;

    @Column(name = "Quantity")
    private Integer quantity; // Số lượng giảm giá còn lại

    @Column(name = "Name", nullable = false)
    private String name; // Tên giảm giá (ví dụ: "Giảm giá mùa hè")

    @Column(name = "Status", nullable = false)
    private String status; // Trạng thái (ví dụ: "Success", "Fail")

    @Column(name = "ApplyDate")
    private Date applyDate;

    @Column(name = "Code", nullable = false, unique = true)
    private String code; // Mã giảm giá (ví dụ: "SUMMER2025")

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
