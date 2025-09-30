package com.livewithoutthinking.resq.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter
@Getter
public class DocumentaryDto {
    private int documentId;
    private int partnerId;
    private String documentType;
    private String documentNumber;
    private String frontImageUrl;
    private String backImageUrl;
    private String documentStatus;
    private LocalDate expiryDate;
}
