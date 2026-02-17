package com.nick.npp.controller;

import com.nick.npp.dto.DashboardResponse;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.dto.SettlementBalanceResponse;
import com.nick.npp.model.PaymentStatus;
import com.nick.npp.service.PaymentService;
import com.nick.npp.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "Aggregated payment and settlement statistics")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PaymentService paymentService;
    private final SettlementService settlementService;

    public DashboardController(PaymentService paymentService, SettlementService settlementService) {
        this.paymentService = paymentService;
        this.settlementService = settlementService;
    }

    @Operation(summary = "Get dashboard summary", description = "Returns payment counts by status, recent payments, and ESA balances")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        List<PaymentResponse> allPayments = paymentService.getAllPayments();
        List<PaymentResponse> recentPayments = allPayments.stream().limit(10).toList();
        List<SettlementBalanceResponse> balances = settlementService.getAllBalances();

        long total = allPayments.size();
        long settled = allPayments.stream().filter(p -> p.status() == PaymentStatus.SETTLED).count();
        long confirmed = allPayments.stream().filter(p -> p.status() == PaymentStatus.CONFIRMED).count();
        long rejected = allPayments.stream().filter(p -> p.status() == PaymentStatus.REJECTED).count();

        return ResponseEntity.ok(new DashboardResponse(
                total, settled, confirmed, rejected, recentPayments, balances));
    }
}
