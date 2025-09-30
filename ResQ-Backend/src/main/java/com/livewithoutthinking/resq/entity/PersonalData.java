package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "personaldatas")
public class PersonalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PDID")
    private int pdId;  

    @Column(name = "CitizenNumber", nullable = false, unique = true)
    private String citizenNumber;  // CitizenNumber là VARCHAR(20)

    @Column(name = "ExpirationDate")
    private java.sql.Date expirationDate;  // ExpirationDate là DATE

    @Column(name = "FrontImage")
    private String frontImage;  // FrontImage là VARCHAR(255)

    @Column(name = "BackImage")
    private String backImage;  // BackImage là VARCHAR(255)

    @Column(name = "VerificationStatus")
    private String verificationStatus;  // VerificationStatus là VARCHAR(50)

    @Column(name = "IssueDate")
    private java.sql.Date issueDate;  // IssueDate là DATE

    @Column(name = "IssuePlace")
    private String issuePlace;  // IssuePlace là VARCHAR(255)

    @Column(name = "VerifiedAt")
    private java.sql.Timestamp verifiedAt;  // VerifiedAt là DATETIME

    @Column(name = "Type")
    private String type;  // Type là VARCHAR(50eer

    @Column(name = "FaceImage")
    private String faceImage;  // FaceImage là VARCHAR(255)

    @OneToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID")
    private User user;
    // Constructor, Getters, Setters and other methods if needed
}
