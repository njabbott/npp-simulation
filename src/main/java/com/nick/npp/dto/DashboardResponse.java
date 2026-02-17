package com.nick.npp.dto;

import java.util.List;

public record DashboardResponse(
        long totalPayments,
        long settledPayments,
        long confirmedPayments,
        long rejectedPayments,
        List<PaymentResponse> recentPayments,
        List<SettlementBalanceResponse> balances
) {}
