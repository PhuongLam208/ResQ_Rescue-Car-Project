/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.livewithoutthinking.resq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 *
 * @author ANVO
 */
@Entity
@Data
@Table(name = "partnerservices")
public class PartnerService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PartnerServiceID")
    private Integer parnerserviceId;
    
    @ManyToOne
    @JoinColumn(name = "PartnerID", referencedColumnName = "PartnerID", nullable = false)
    private Partner partner; // Liên kết với bảng Partners qua PartnerID
    
    @ManyToOne
    @JoinColumn(name = "ServiceID", referencedColumnName = "ServiceID", nullable = false)
    private Services services; // Liên kết với bảng Services qua ServiceID
}
