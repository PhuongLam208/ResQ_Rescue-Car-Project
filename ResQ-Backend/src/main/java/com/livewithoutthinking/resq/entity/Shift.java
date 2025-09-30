package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "shift")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShiftID")
    private int shiftId; // ShiftID là INT và tự động tăng

    @ManyToOne
    @JoinColumn(name = "CreatorID", referencedColumnName = "StaffID")
    private Staff creator;

    @Column(name = "Title")
    private String title; // Tên sự kiện

    @Column(name = "StartTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime; // Thời gian bắt đầu

    @Column(name = "EndTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime; // Thời gian kết thúc

    @Column(name = "Status")
    private String status; // Trạng thái công việc (ví dụ: "Completed", "Pending")

    @Column(name = "EventColor")
    private String eventColor; // Màu sắc của sự kiện (để phân loại trên lịch)

    @Column(name = "Description")
    private String description; // Mô tả sự kiện

    @Column(name = "IsRecurring")
    private Boolean isRecurring; // Xác định nếu sự kiện là lặp lại

    @Column(name = "RecurrenceType")
    private String recurrenceType; // Loại chu kỳ lặp lại: "DAILY", "WEEKLY", "MONTHLY", "CUSTOM"

    @Column(name = "RecurrenceInterval")
    private Integer recurrenceInterval; // Số chu kỳ (ví dụ: lặp lại mỗi 2 tuần, mỗi tháng,...)

    @Column(name = "RecurrenceDays")
    private String recurrenceDays; // Các ngày lặp lại trong tuần, ví dụ: "MONDAY, WEDNESDAY, FRIDAY"

    @Column(name = "RecurrenceEndDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date recurrenceEndDate; // Ngày kết thúc chu kỳ (nếu có)

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
