package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StaffID")
    private int staffId; 

    @OneToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID")
    private User user; // Liên kết với bảng Users, thông qua UserID

    @Column(name = "Hired_date")
    private Date hiredDate; // Ngày thuê

    @Column(name = "AvgTime")
    private Float avgTime; // Thời gian trung bình

    @Column(name = "ConversationCount")
    private Integer conversationCount;
    // Tổng số conversation từ đó đến giờ để tiện tính AvgTime

    @Column(name = "OnShiftChatCount")
    private Integer onShiftChatCount;
    // Số cuộc trò chuyện chưa kết thúc trong ca để tiện chia conversation mới

    @Column(name = "OnShift", nullable = false)
    private Boolean onShift = false;

    @Column(name = "IsOnline")
    private Boolean isOnline = false;

    @Column(name = "MonthLateCount")
    private Integer monthLateCount = 0;
    // Đăng nhập vào ca trễ trong tháng ( quá 5ph là trễ )

    @Column(name = "LastLogin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    // Constructor, getters, setters và các phương thức khác nếu cần

    public boolean isOnShift() {
        return onShift;
    }
}
