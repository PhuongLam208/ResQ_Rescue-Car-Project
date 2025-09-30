package com.livewithoutthinking.resq.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Setter
@Getter
public class TransactionDto {
    private Integer billId;
    private double servicePrice;
    private double distancePrice;
    private double extraPrice;
    private double discountAmount;
    private double totalPrice; // Tổng số tiền trước giảm
    private double total; // Tổng cuối cùng sau giảm
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String status;
    private String rescueType;
    private Date startTime;
    private Date endTime;

    public TransactionDto(Integer billId, double servicePrice, double distancePrice, double extraPrice,
                          double discountAmount, double totalPrice, double total,
                          String paymentMethod, LocalDateTime createdAt, String status,
                          String rescueType, Date startTime, Date endTime) {
        this.billId = billId;
        this.servicePrice = servicePrice;
        this.distancePrice = distancePrice;
        this.extraPrice = extraPrice;
        this.discountAmount = discountAmount;
        this.totalPrice = totalPrice;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.status = status;             // thêm dòng này
        this.rescueType = rescueType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
