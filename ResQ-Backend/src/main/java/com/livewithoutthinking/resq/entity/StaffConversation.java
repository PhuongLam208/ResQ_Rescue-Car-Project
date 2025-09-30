package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(StaffConversationId.class)
@Table(name = "staff_conversation")
public class StaffConversation {

    @Id
    @ManyToOne
    @JoinColumn(name = "StaffID", referencedColumnName = "StaffID")
    private Staff staff;

    @Id
    @ManyToOne
    @JoinColumn(name = "ConversationID", referencedColumnName = "ConversationID")
    private Conversation conversation;

    // Optional: thêm thông tin khác như assignedAt hoặc isActive
    @Column(name = "AssignedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date assignedAt;
}
