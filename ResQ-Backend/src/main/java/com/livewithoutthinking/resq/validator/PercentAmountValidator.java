package com.livewithoutthinking.resq.validator;

import com.livewithoutthinking.resq.dto.DiscountDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class PercentAmountValidator implements ConstraintValidator<PercentAmountConstrait, DiscountDto> {

    @Override
    public boolean isValid(DiscountDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        if ("Percent".equalsIgnoreCase(dto.getType()) && dto.getAmount() != null) {
            boolean valid = dto.getAmount().compareTo(BigDecimal.ZERO) > 0
                    && dto.getAmount().compareTo(BigDecimal.valueOf(100)) <= 0;

            if (!valid) {
                // Tắt default message
                context.disableDefaultConstraintViolation();
                // Gắn lỗi cụ thể vào field amount
                context.buildConstraintViolationWithTemplate("Discount amount must be between 0 and 100 for Percent type")
                        .addPropertyNode("amount")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
