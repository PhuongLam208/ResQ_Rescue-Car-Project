package com.livewithoutthinking.resq.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "Username", nullable = false, unique = true)
    private String username;

    @Column(name = "FullName", nullable = false, unique = true)
    private String fullName;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Email", unique = true)
    private String email;

    @Column(name = "SDT", nullable = false, unique = true)
    private String sdt; // Phone number

    @Column(name = "Status")
    private String status = "WAITING";

    @Column(name = "DOB")
    private Date dob; // Date of Birth

    @Column(name = "Gender")
    private String gender;

    @Column(name = "Address")
    private String address;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "PhoneVerified")
    private boolean phoneVerified;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "Language")
    private String language;

    @Column(name = "AppColor")
    private String appColor;

    @Column(name = "LoyaltyPoint")
    private int loyaltyPoint = 0;

    @Column(name = "is_online")
    private Boolean isOnline = true;

    @Column(name = "block_until")
    private LocalDateTime blockUntil;

    @ManyToOne
    @JoinColumn(name = "RoleID", referencedColumnName = "RoleID")
    @JsonManagedReference
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Report> reports;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Partner partner;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PersonalData personalData;

    public User() {
    }
    public User(Integer userId) {
        this.userId = userId;
    }

    public boolean hasPD() {
        return personalData != null;
    }


    // Constructor, Getters, Setters and other methods
}

