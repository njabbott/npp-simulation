package com.nick.npp.dto;

import com.nick.npp.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String paymentId,
        String endToEndId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String remittanceInfo,
        String payIdUsed,
        String rejectionReason,
        String debtorAccountName,
        String debtorBsb,
        String debtorAccountNumber,
        String debtorBankName,
        String creditorAccountName,
        String creditorBsb,
        String creditorAccountNumber,
        String creditorBankName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
