package com.nick.npp.controller;

import com.nick.npp.dto.PayIdResolutionResponse;
import com.nick.npp.model.PayIdType;
import com.nick.npp.service.PayIdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PayID", description = "Resolve PayID aliases to bank account details")
@RestController
@RequestMapping("/api/payid")
public class PayIdController {

    private final PayIdService payIdService;

    public PayIdController(PayIdService payIdService) {
        this.payIdService = payIdService;
    }

    @Operation(summary = "List all PayIDs")
    @GetMapping
    public ResponseEntity<List<PayIdResolutionResponse>> getAll() {
        return ResponseEntity.ok(payIdService.getAllPayIds());
    }

    @Operation(summary = "Resolve a PayID", description = "Looks up the bank account linked to a given PayID type and value")
    @GetMapping("/resolve")
    public ResponseEntity<PayIdResolutionResponse> resolve(
            @RequestParam String type,
            @RequestParam String value) {
        PayIdType payIdType = PayIdType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(payIdService.resolve(payIdType, value));
    }
}
