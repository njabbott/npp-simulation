package com.nick.npp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MandateRequest(
        @NotBlank String creditorBsb,
        @NotBlank String creditorAccountNumber,
        @NotBlank String debtorBsb,
        @NotBlank String debtorAccountNumber,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal maximumAmount,
        @NotBlank String frequency
) {}
