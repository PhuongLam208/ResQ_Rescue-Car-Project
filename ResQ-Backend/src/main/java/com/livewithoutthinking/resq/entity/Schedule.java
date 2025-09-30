package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleID")
    private int scheduleId;

    @ManyToOne
    @JoinColumn(name = "ShiftID", referencedColumnName = "ShiftID")
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "ManagerID", referencedColumnName = "StaffID")
    private Staff manager;

    @ManyToOne
    @JoinColumn(name = "StaffID", referencedColumnName = "StaffID")
    private Staff staff;

    @Column(name = "WorkingDate")
    private LocalDate workingDate;
}
