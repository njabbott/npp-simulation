package com.nick.npp.controller;

import com.nick.npp.dto.PayIdResolutionResponse;
import com.nick.npp.model.PayIdType;
import com.nick.npp.service.PayIdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payid")
public class PayIdController {

    private final PayIdService payIdService;

    public PayIdController(PayIdService payIdService) {
        this.payIdService = payIdService;
    }

    @GetMapping("/resolve")
    public ResponseEntity<PayIdResolutionResponse> resolve(
            @RequestParam String type,
            @RequestParam String value) {
        PayIdType payIdType = PayIdType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(payIdService.resolve(payIdType, value));
    }
}
