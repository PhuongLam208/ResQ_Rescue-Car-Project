package com.livewithoutthinking.resq.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PercentAmountValidator.class) // tham chiếu tới class validator
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PercentAmountConstrait {
    String message() default "If discount type is 'Percent', amount must not exceed 100";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
