package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "contacttype")
public class ContactType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContactID")
    private int contactId; // ContactID là INT và tự động tăng

    @Column(name = "Name", nullable = false, unique = true)
    private String name; // Tên loại liên hệ

    @Column(name = "Description")
    private String description; // Mô tả loại liên hệ

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
