package com.nick.npp.controller;

import com.nick.npp.dto.MandateExecuteRequest;
import com.nick.npp.dto.MandateRequest;
import com.nick.npp.dto.MandateResponse;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.service.PayToService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payto/mandates")
public class PayToController {

    private final PayToService payToService;

    public PayToController(PayToService payToService) {
        this.payToService = payToService;
    }

    @PostMapping
    public ResponseEntity<MandateResponse> createMandate(@Valid @RequestBody MandateRequest request) {
        return ResponseEntity.ok(payToService.createMandate(request));
    }

    @GetMapping
    public ResponseEntity<List<MandateResponse>> getAllMandates() {
        return ResponseEntity.ok(payToService.getAllMandates());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<MandateResponse> approveMandate(@PathVariable Long id) {
        return ResponseEntity.ok(payToService.approveMandate(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<MandateResponse> rejectMandate(@PathVariable Long id) {
        return ResponseEntity.ok(payToService.rejectMandate(id));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<PaymentResponse> executePayment(
            @PathVariable Long id,
            @Valid @RequestBody MandateExecuteRequest request) {
        return ResponseEntity.ok(payToService.executePayment(id, request));
    }
}
