package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Getter
@Setter
@Table(name = "conversation")
public class Conversation {

    // them conversation status
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConversationID")
    private int conversationId; 

    @ManyToOne
    @JoinColumn(name = "ContactType", referencedColumnName = "ContactID")
    private ContactType contactType; // Liên kết với bảng ContactType

    @ManyToOne
    @JoinColumn(name = "SenderID", referencedColumnName = "UserID")
    private User sender; // Liên kết với bảng Users cho người gửi

    @Column(name = "Subject")
    private String subject; // Chủ đề cuộc trò chuyện

    @Column(name = "IsClosed")
    private Boolean isClosed = false;

    @Column(name = "UserType")
    private String userType;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo cuộc trò chuyện

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật cuộc trò chuyện

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<Message> messages = new ArrayList<>();



    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
