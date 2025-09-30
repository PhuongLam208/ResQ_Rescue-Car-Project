package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeedbackID")
    private int feedbackId; 

    @ManyToOne
    @JoinColumn(name = "RRID", referencedColumnName = "RRID", nullable = false)
    private RequestRescue requestRescue; // Liên kết với bảng RequestRescue (khóa ngoại RRID)

    @Column(name = "Name")
    private String name; // Tên phản hồi

    @Column(name = "Description")
    private String description; // Mô tả phản hồi

    @Column(name = "Rate")
    private int rate; // Đánh giá (từ 1 đến 5)

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
