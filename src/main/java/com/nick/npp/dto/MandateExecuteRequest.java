package com.nick.npp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MandateExecuteRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String remittanceInfo
) {}
