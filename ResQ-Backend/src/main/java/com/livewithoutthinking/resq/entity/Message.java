package com.livewithoutthinking.resq.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.util.Date;
@Entity
@Data
@Table(name = "message")
public class Message {

    @Id
    @Column(name = "MessageID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int messageId; // MessageID là INT AUTO_INCREMENT

    @ManyToOne
    @JoinColumn(name = "ConversationID", referencedColumnName = "ConversationID")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Conversation conversation;
    // Liên kết với bảng Conversation

    @ManyToOne
    @JoinColumn(name = "SenderID", referencedColumnName = "UserID")
    private User sender;


    @Column(name = "Content")
    private String content; // Nội dung tin nhắn

    @Enumerated(EnumType.STRING)
    private Status status = Status.SENT;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo tin nhắn

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật tin nhắn

    // Constructor, Getters, Setters và các phương thức khác nếu cần

    public enum Status {
        SENT,
        READ
    }


    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }
}