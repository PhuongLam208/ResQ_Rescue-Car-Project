package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private int reportId; 

    @ManyToOne
    @JoinColumn(name = "RRID", referencedColumnName = "RRID", nullable = false)
    private RequestRescue requestRescue; // Liên kết với bảng RequestRescue (khóa ngoại RRID)

    @Column(name = "Name")
    private String name; // Tên báo cáo

    @Column(name = "Description")
    private String description; // Mô tả báo cáo

    @Column(name = "Status")
    private String status;

    @Column(name = "Response_To_Complainant", nullable = true)
    private String responseToComplainant;

    @ManyToOne
    @JoinColumn(name = "StaffID", referencedColumnName = "StaffID", nullable = false)
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "ResolvedBy", referencedColumnName = "StaffID")
    private Staff resolver;

    // Complainant
    @ManyToOne
    @JoinColumn(name = "Complainant_Customer_Id", referencedColumnName = "UserID")
    private User complainantCustomer;

    @ManyToOne
    @JoinColumn(name = "Complainant_Partner_Id", referencedColumnName = "PartnerID")
    private Partner complainantPartner;

    // Defendant
    @ManyToOne
    @JoinColumn(name = "Defendant_Customer_Id", referencedColumnName = "UserID")
    private User defendantCustomer;

    @ManyToOne
    @JoinColumn(name = "Defendant_Partner_Id", referencedColumnName = "PartnerID")
    private Partner defendantPartner;

    @Column(name = "Request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "PdfFileName")
    private String pdfFileName;

    @Column(name = "Within_24H", nullable = false)
    private boolean within24H = false;

    @ManyToOne
    @JoinColumn(name = "partner_id") // tên cột foreign key trong bảng Report
    private Partner partner;

    @ManyToOne
    @JoinColumn(name = "user_id") // cột foreign key trong bảng Report
    private User user;

    @Column(name = "Created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian tạo

    @Column(name = "Updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // Thời gian cập nhật

    // Constructor, Getters, Setters và các phương thức khác nếu cần
}
