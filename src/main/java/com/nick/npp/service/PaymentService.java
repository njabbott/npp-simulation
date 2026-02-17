package com.nick.npp.service;

import com.nick.npp.dto.PayIdResolutionResponse;
import com.nick.npp.dto.PaymentRequest;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.exception.AccountNotFoundException;
import com.nick.npp.exception.InsufficientEsaBalanceException;
import com.nick.npp.exception.PaymentNotFoundException;
import com.nick.npp.model.*;
import com.nick.npp.repository.BankAccountRepository;
import com.nick.npp.repository.NppPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final String[] REJECTION_REASONS = {
            "Account closed",
            "Invalid BSB",
            "Account frozen - regulatory hold",
            "Beneficiary name mismatch",
            "Transaction limit exceeded"
    };

    @Value("${npp.simulation.clearing-delay-ms:500}")
    private long clearingDelayMs;

    @Value("${npp.simulation.settlement-delay-ms:800}")
    private long settlementDelayMs;

    @Value("${npp.simulation.confirmation-delay-ms:200}")
    private long confirmationDelayMs;

    private final NppPaymentRepository paymentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PayIdService payIdService;
    private final SettlementService settlementService;
    private final Iso20022MessageService messageService;

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public PaymentService(NppPaymentRepository paymentRepository,
                          BankAccountRepository bankAccountRepository,
                          PayIdService payIdService,
                          SettlementService settlementService,
                          Iso20022MessageService messageService) {
        this.paymentRepository = paymentRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.payIdService = payIdService;
        this.settlementService = settlementService;
        this.messageService = messageService;
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // Resolve debtor account
        BankAccount debtorAccount = bankAccountRepository
                .findByBsbAndAccountNumber(request.debtorBsb(), request.debtorAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Debtor account not found: " + request.debtorBsb() + "/" + request.debtorAccountNumber()));

        // Resolve creditor account (via PayID or BSB/account)
        BankAccount creditorAccount;
        String payIdUsed = null;

        if (request.payIdType() != null && request.payIdValue() != null
                && !request.payIdType().isEmpty() && !request.payIdValue().isEmpty()) {
            PayIdType type = PayIdType.valueOf(request.payIdType());
            PayIdResolutionResponse resolved = payIdService.resolve(type, request.payIdValue());
            creditorAccount = bankAccountRepository
                    .findByBsbAndAccountNumber(resolved.bsb(), resolved.accountNumber())
                    .orElseThrow(() -> new AccountNotFoundException("Resolved PayID account not found"));
            payIdUsed = type + ":" + request.payIdValue();
        } else if (request.creditorBsb() != null && request.creditorAccountNumber() != null) {
            creditorAccount = bankAccountRepository
                    .findByBsbAndAccountNumber(request.creditorBsb(), request.creditorAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(
                            "Creditor account not found: " + request.creditorBsb() + "/" + request.creditorAccountNumber()));
        } else {
            throw new IllegalArgumentException("Must provide either PayID or creditor BSB/account number");
        }

        // Validate not same account
        if (debtorAccount.getId().equals(creditorAccount.getId())) {
            throw new IllegalArgumentException("Cannot send payment to the same account");
        }

        // Create payment
        NppPayment payment = new NppPayment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setEndToEndId("E2E" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        payment.setAmount(request.amount());
        payment.setCurrency("AUD");
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setRemittanceInfo(request.remittanceInfo());
        payment.setPayIdUsed(payIdUsed);
        payment.setDebtorAccount(debtorAccount);
        payment.setCreditorAccount(creditorAccount);
        payment.setDebtorAgent(debtorAccount.getParticipant());
        payment.setCreditorAgent(creditorAccount.getParticipant());

        payment = paymentRepository.save(payment);
        log.info("Payment {} initiated: {} AUD from {} -> {}",
                payment.getPaymentId(), payment.getAmount(),
                debtorAccount.getAccountName(), creditorAccount.getAccountName());

        // Build pacs.008
        messageService.buildPacs008(payment);

        // Kick off async processing
        processPaymentAsync(payment.getId());

        return toResponse(payment);
    }

    @Async
    public void processPaymentAsync(Long paymentId) {
        try {
            // Stage 1: Clearing
            Thread.sleep(clearingDelayMs);
            NppPayment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) return;

            payment.setStatus(PaymentStatus.CLEARING);
            payment = paymentRepository.save(payment);
            emitEvent(payment.getPaymentId(), "CLEARING", "Payment entered NPP clearing");
            log.info("Payment {} -> CLEARING", payment.getPaymentId());

            // ~10% chance of rejection at clearing
            Random random = new Random();
            if (random.nextInt(10) == 0) {
                String reason = REJECTION_REASONS[random.nextInt(REJECTION_REASONS.length)];
                payment.setStatus(PaymentStatus.REJECTED);
                payment.setRejectionReason(reason);
                paymentRepository.save(payment);
                messageService.buildPacs002(payment, false, reason);
                emitEvent(payment.getPaymentId(), "REJECTED", reason);
                log.info("Payment {} -> REJECTED: {}", payment.getPaymentId(), reason);
                return;
            }

            // Stage 2: Settlement via FSS
            Thread.sleep(settlementDelayMs);
            payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) return;

            try {
                settlementService.settle(payment);
                payment.setStatus(PaymentStatus.SETTLED);
                payment = paymentRepository.save(payment);
                emitEvent(payment.getPaymentId(), "SETTLED", "FSS settlement complete");
                log.info("Payment {} -> SETTLED", payment.getPaymentId());
            } catch (InsufficientEsaBalanceException e) {
                payment.setStatus(PaymentStatus.REJECTED);
                payment.setRejectionReason("Insufficient ESA balance: " + e.getMessage());
                paymentRepository.save(payment);
                messageService.buildPacs002(payment, false, e.getMessage());
                emitEvent(payment.getPaymentId(), "REJECTED", e.getMessage());
                log.info("Payment {} -> REJECTED (ESA): {}", payment.getPaymentId(), e.getMessage());
                return;
            }

            // Stage 3: Confirmation
            Thread.sleep(confirmationDelayMs);
            payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) return;

            messageService.buildPacs002(payment, true, null);
            payment.setStatus(PaymentStatus.CONFIRMED);
            paymentRepository.save(payment);
            emitEvent(payment.getPaymentId(), "CONFIRMED", "Payment confirmed by creditor agent");
            log.info("Payment {} -> CONFIRMED", payment.getPaymentId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment processing interrupted for {}", paymentId);
        }
    }

    @Transactional
    public PaymentResponse returnPayment(Long paymentId) {
        NppPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.CONFIRMED && payment.getStatus() != PaymentStatus.SETTLED) {
            throw new IllegalStateException("Can only return CONFIRMED or SETTLED payments, current status: " + payment.getStatus());
        }

        settlementService.reverseSettlement(payment);
        messageService.buildPacs004(payment, "Requested by originator");

        payment.setStatus(PaymentStatus.RETURNED);
        paymentRepository.save(payment);

        log.info("Payment {} -> RETURNED", payment.getPaymentId());
        return toResponse(payment);
    }

    public SseEmitter subscribe(String paymentId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.computeIfAbsent(paymentId, k -> Collections.synchronizedList(new ArrayList<>())).add(emitter);

        emitter.onCompletion(() -> removeEmitter(paymentId, emitter));
        emitter.onTimeout(() -> removeEmitter(paymentId, emitter));
        emitter.onError(e -> removeEmitter(paymentId, emitter));

        // Send current status immediately
        paymentRepository.findByPaymentId(paymentId).ifPresent(payment -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(Map.of("status", payment.getStatus().name(),
                                "message", "Current status")));
            } catch (IOException e) {
                log.warn("Failed to send initial SSE status for {}", paymentId);
            }
        });

        return emitter;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        NppPayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByPaymentId(String paymentId) {
        NppPayment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return paymentRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == status)
                .count();
    }

    private void emitEvent(String paymentId, String status, String message) {
        List<SseEmitter> paymentEmitters = emitters.get(paymentId);
        if (paymentEmitters == null) return;

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : paymentEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(Map.of("status", status, "message", message)));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        paymentEmitters.removeAll(deadEmitters);
    }

    private void removeEmitter(String paymentId, SseEmitter emitter) {
        List<SseEmitter> paymentEmitters = emitters.get(paymentId);
        if (paymentEmitters != null) {
            paymentEmitters.remove(emitter);
        }
    }

    public PaymentResponse toResponse(NppPayment p) {
        return new PaymentResponse(
                p.getId(),
                p.getPaymentId(),
                p.getEndToEndId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getRemittanceInfo(),
                p.getPayIdUsed(),
                p.getRejectionReason(),
                p.getDebtorAccount().getAccountName(),
                p.getDebtorAccount().getBsb(),
                p.getDebtorAccount().getAccountNumber(),
                p.getDebtorAgent().getName(),
                p.getCreditorAccount().getAccountName(),
                p.getCreditorAccount().getBsb(),
                p.getCreditorAccount().getAccountNumber(),
                p.getCreditorAgent().getName(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
