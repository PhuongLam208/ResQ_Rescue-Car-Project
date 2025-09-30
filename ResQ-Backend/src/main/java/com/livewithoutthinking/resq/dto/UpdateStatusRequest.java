// src/main/java/com/livewithoutthinking/resq/dto/UpdateStatusRequest.java
package com.livewithoutthinking.resq.dto;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private int billId;
    private String status;
}
