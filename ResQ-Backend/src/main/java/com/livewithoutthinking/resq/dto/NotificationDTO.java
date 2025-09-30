package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotificationDTO {
    private int noId;
    private String message;
    private String viewStatus;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isRead;


    // Thông tin từ NotificationTemplate
    private String notiType;
    private String title;
}
