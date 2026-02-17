package com.nick.npp.controller;

import com.nick.npp.dto.PaymentRequest;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<PaymentResponse> returnPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.returnPayment(id));
    }

    @GetMapping(value = "/{paymentId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEvents(@PathVariable String paymentId) {
        return paymentService.subscribe(paymentId);
    }
}
