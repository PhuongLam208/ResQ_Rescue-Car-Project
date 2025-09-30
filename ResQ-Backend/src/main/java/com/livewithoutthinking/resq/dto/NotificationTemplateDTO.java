package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotificationTemplateDTO {
    private Long notificationTemplateID;
    private String title;
    private String notitype;
    private Date createdAt;
    private Date updatedAt;
}
