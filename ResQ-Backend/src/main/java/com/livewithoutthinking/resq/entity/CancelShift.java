package com.livewithoutthinking.resq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "cancel_shift")
public class CancelShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "shift_id", referencedColumnName = "ShiftID")
    private Shift shift;

    @Column(name = "canceled_date", nullable = false)
    private LocalDate canceledDate;
}
