package com.livewithoutthinking.resq.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
@Data
@Setter
@Getter
public class MonthlyRevenueDto {
    private int month;
    private int year;
    private double total;
    private double appFee;
    private String status;
    private LocalDateTime latestCreatedAt;

    public MonthlyRevenueDto(int month, int year, double total, double appFee, String status, LocalDateTime latestCreatedAt) {
        this.month = month;
        this.year = year;
        this.total = total;
        this.appFee = appFee;
        this.status = status;
        this.latestCreatedAt = latestCreatedAt;
    }

    // Getters & setters
}

