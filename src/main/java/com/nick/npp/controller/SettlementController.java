package com.nick.npp.controller;

import com.nick.npp.dto.SettlementBalanceResponse;
import com.nick.npp.model.SettlementRecord;
import com.nick.npp.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/balances")
    public ResponseEntity<List<SettlementBalanceResponse>> getBalances() {
        return ResponseEntity.ok(settlementService.getAllBalances());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<SettlementRecord>> getTransactions() {
        return ResponseEntity.ok(settlementService.getTransactionLog());
    }
}
