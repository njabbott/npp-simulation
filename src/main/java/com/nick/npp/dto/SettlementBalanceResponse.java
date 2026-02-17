package com.nick.npp.dto;

import java.math.BigDecimal;

public record SettlementBalanceResponse(
        Long id,
        String participantName,
        String shortName,
        String bic,
        BigDecimal esaBalance
) {}
