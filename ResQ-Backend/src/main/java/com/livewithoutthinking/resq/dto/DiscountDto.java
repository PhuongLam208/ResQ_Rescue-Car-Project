package com.livewithoutthinking.resq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.livewithoutthinking.resq.validator.PercentAmountConstrait;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@PercentAmountConstrait
public class DiscountDto {
    private Integer id;
    @NotBlank(message = "Discount name must not be empty")
    @Size(max = 100, message = "Discount name must be les than 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Discount name must not contain specail characters")
    private String name;
    @NotBlank(message = "Discount type_dis must not be empty")
    @JsonProperty("type_dis") // map từ JSON key "type_dis" sang field "typeDis"
    private String typeDis;


    @NotBlank(message = "Discount code must not be empty")
    @Size(max = 50, message = "Discount code must be less than 50 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Discount code must contain only uppercase letters and numbers")
    private String code;

    @NotNull(message = "Discount amount must not be null")
    @DecimalMin(value = "0.01", message = "Discount amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Discount type must not be empty")
    @Pattern(regexp = "^(Money|Percent)$", message = "Discount type must be either 'Money' or 'Percent'")
    private String type; // Money or Percent

    @FutureOrPresent(message = "Apply date must be today or in the future")
    @NotNull(message = "Apply date must not be null")
    private Date applyDate;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String status = "Active"; // Default value
    private boolean isPercent = false;
}
