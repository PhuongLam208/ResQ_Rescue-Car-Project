package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class UserDashboard {
    int totalSuccess;
    int totalCancel;
    double percentSuccess;
    double totalAmount;
}
