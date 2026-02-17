package com.nick.npp.controller;

import com.nick.npp.dto.PaymentRequest;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Payments", description = "Initiate and manage NPP real-time payments")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Initiate a payment", description = "Submits a new NPP payment and begins the clearing/settlement process")
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @Operation(summary = "List all payments")
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @Operation(summary = "Get a payment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @Operation(summary = "Return a payment", description = "Initiates a return for a previously settled payment")
    @PostMapping("/{id}/return")
    public ResponseEntity<PaymentResponse> returnPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.returnPayment(id));
    }

    @Operation(summary = "Subscribe to payment events", description = "Server-Sent Events stream for real-time payment status updates")
    @GetMapping(value = "/{paymentId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEvents(@PathVariable String paymentId) {
        return paymentService.subscribe(paymentId);
    }
}
