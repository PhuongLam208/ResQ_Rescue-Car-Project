package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class UpdateBillDto {
    private Integer billId;
    private Integer discountId;          // null nếu không có
    private String paymentMethod;
    private Double discountAmount;       // null nếu không có giảm giá
    private Double totalPrice;           // Đã tính từ phía client (sau khi trừ giảm giá)
    private Integer userId;              // dùng để kiểm tra quyền nếu cần
}
