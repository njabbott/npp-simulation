package com.nick.npp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String payIdType,
        String payIdValue,
        String creditorBsb,
        String creditorAccountNumber,
        @NotBlank String debtorBsb,
        @NotBlank String debtorAccountNumber,
        String remittanceInfo
) {}
