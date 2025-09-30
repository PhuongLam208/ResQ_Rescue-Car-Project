package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Entity
@Data
@Table(name = "refund_requests")
public class RefundRequest {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RefundID")
    private int refundId; 

    @ManyToOne
    @JoinColumn(name = "RRID", referencedColumnName = "RRID", nullable = false)
    private RequestRescue requestRescue; // Liên kết với bảng RequestRescue (khóa ngoại RRID)

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user; // Liên kết với bảng Users (khóa ngoại UserID)

    @ManyToOne
    @JoinColumn(name = "SenderID", referencedColumnName = "StaffID", nullable = false, updatable = false)
    private Staff senderStaff; // Liên kết với bảng Staff (khóa ngoại StaffID)

    @ManyToOne
    @JoinColumn(name = "RecipientID", referencedColumnName = "StaffID")
    private Staff recipientStaff; // Liên kết với bảng Staff (khóa ngoại StaffID)

    @Column(name = "Amount")
    private BigDecimal amount; // Số tiền hoàn

    @Column(name = "Reason")
    private String reason; // Lý do hoàn tiền

    @Column(name = "Status")
    private String status; // Trạng thái

    @OneToOne
    @JoinColumn(name = "ConversationID", referencedColumnName = "ConversationID")
    private Conversation conversation;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật


    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
