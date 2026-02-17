package com.nick.npp.dto;

import com.nick.npp.model.MandateStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MandateResponse(
        Long id,
        String mandateId,
        String description,
        BigDecimal maximumAmount,
        String frequency,
        MandateStatus status,
        LocalDate validFrom,
        LocalDate validTo,
        String creditorAccountName,
        String creditorBsb,
        String creditorAccountNumber,
        String debtorAccountName,
        String debtorBsb,
        String debtorAccountNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
