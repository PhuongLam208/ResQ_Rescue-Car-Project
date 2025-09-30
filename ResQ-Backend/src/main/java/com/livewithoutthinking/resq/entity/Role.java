package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private int roleId;  // RoleID là INT

    @Column(name = "RoleName", nullable = false, unique = true)
    private String roleName;  // RoleName là VARCHAR(50)

    // Constructor, Getters, Setters and other methods if needed
}
