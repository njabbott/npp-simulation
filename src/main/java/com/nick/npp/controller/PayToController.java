package com.nick.npp.controller;

import com.nick.npp.dto.MandateExecuteRequest;
import com.nick.npp.dto.MandateRequest;
import com.nick.npp.dto.MandateResponse;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.service.PayToService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PayTo Mandates", description = "Create and manage PayTo payment mandates")
@RestController
@RequestMapping("/api/payto/mandates")
public class PayToController {

    private final PayToService payToService;

    public PayToController(PayToService payToService) {
        this.payToService = payToService;
    }

    @Operation(summary = "Create a mandate")
    @PostMapping
    public ResponseEntity<MandateResponse> createMandate(@Valid @RequestBody MandateRequest request) {
        return ResponseEntity.ok(payToService.createMandate(request));
    }

    @Operation(summary = "List all mandates")
    @GetMapping
    public ResponseEntity<List<MandateResponse>> getAllMandates() {
        return ResponseEntity.ok(payToService.getAllMandates());
    }

    @Operation(summary = "Approve a mandate")
    @PutMapping("/{id}/approve")
    public ResponseEntity<MandateResponse> approveMandate(@PathVariable Long id) {
        return ResponseEntity.ok(payToService.approveMandate(id));
    }

    @Operation(summary = "Reject a mandate")
    @PutMapping("/{id}/reject")
    public ResponseEntity<MandateResponse> rejectMandate(@PathVariable Long id) {
        return ResponseEntity.ok(payToService.rejectMandate(id));
    }

    @Operation(summary = "Execute a mandate payment", description = "Triggers a payment against an approved mandate")
    @PostMapping("/{id}/execute")
    public ResponseEntity<PaymentResponse> executePayment(
            @PathVariable Long id,
            @Valid @RequestBody MandateExecuteRequest request) {
        return ResponseEntity.ok(payToService.executePayment(id, request));
    }
}
