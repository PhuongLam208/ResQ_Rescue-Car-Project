package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NoID")
    private int noId; // NoID là INT và tự động tăng

    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID")
    private User user; // Liên kết với bảng Users, thông qua UserID

    @Column(name = "Title")
    private String title;

    @Column(name = "Message")
    private String message; // Tin nhắn thông báo

    @Column(name = "ViewStatus")
    private String viewStatus;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo thông báo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật thông báo

    @Column(name = "isRead")
    private Boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "NotificationTemplateID", referencedColumnName = "NotificationTemplateID")
    private NotificationTemplate notificationTemplate;


    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
