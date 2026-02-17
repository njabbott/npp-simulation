package com.nick.npp.controller;

import com.nick.npp.dto.SettlementBalanceResponse;
import com.nick.npp.dto.SettlementTransactionResponse;
import com.nick.npp.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Settlement", description = "ESA balances and settlement transaction log")
@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Operation(summary = "Get ESA balances", description = "Returns the Exchange Settlement Account balance for each NPP participant")
    @GetMapping("/balances")
    public ResponseEntity<List<SettlementBalanceResponse>> getBalances() {
        return ResponseEntity.ok(settlementService.getAllBalances());
    }

    @Operation(summary = "Get settlement transactions", description = "Returns the full log of inter-bank settlement entries")
    @GetMapping("/transactions")
    public ResponseEntity<List<SettlementTransactionResponse>> getTransactions() {
        return ResponseEntity.ok(settlementService.getTransactionLog());
    }
}
