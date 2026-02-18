package com.nick.npp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementTransactionResponse(
        Long id,
        BigDecimal amount,
        BigDecimal debitBalanceAfter,
        BigDecimal creditBalanceAfter,
        LocalDateTime settledAt,
        ParticipantSummary debitParticipant,
        ParticipantSummary creditParticipant
) {
    public record ParticipantSummary(String name, String shortName, String bic) {}
}
