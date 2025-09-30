package com.livewithoutthinking.resq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportResolveDto {
    @NotBlank(message = "Response cannot be blank")
    private String responseToComplainant;

    @NotNull(message = "Status is required")
    @NotBlank(message = "Response cannot be blank")
    private String status;

    private Integer resolverId;
}
