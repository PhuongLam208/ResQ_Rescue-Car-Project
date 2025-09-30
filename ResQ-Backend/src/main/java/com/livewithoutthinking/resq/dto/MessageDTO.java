package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MessageDTO {
    private Integer messageId;
    private Integer conversationId;
    private Integer senderId;
    private Integer recipientId;
    private String senderName;
    private String senderRole;
    private String content;
    private Date createdAt;
}
