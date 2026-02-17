package com.nick.npp.service;

import com.nick.npp.dto.MandateExecuteRequest;
import com.nick.npp.dto.MandateRequest;
import com.nick.npp.dto.MandateResponse;
import com.nick.npp.dto.PaymentRequest;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.exception.AccountNotFoundException;
import com.nick.npp.exception.MandateNotFoundException;
import com.nick.npp.model.BankAccount;
import com.nick.npp.model.MandateStatus;
import com.nick.npp.model.PayToMandate;
import com.nick.npp.repository.BankAccountRepository;
import com.nick.npp.repository.PayToMandateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PayToService {

    private static final Logger log = LoggerFactory.getLogger(PayToService.class);

    private final PayToMandateRepository mandateRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PaymentService paymentService;

    public PayToService(PayToMandateRepository mandateRepository,
                        BankAccountRepository bankAccountRepository,
                        PaymentService paymentService) {
        this.mandateRepository = mandateRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public MandateResponse createMandate(MandateRequest request) {
        BankAccount creditor = bankAccountRepository
                .findByBsbAndAccountNumber(request.creditorBsb(), request.creditorAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Creditor account not found"));

        BankAccount debtor = bankAccountRepository
                .findByBsbAndAccountNumber(request.debtorBsb(), request.debtorAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Debtor account not found"));

        PayToMandate mandate = new PayToMandate();
        mandate.setMandateId(UUID.randomUUID().toString());
        mandate.setDescription(request.description());
        mandate.setMaximumAmount(request.maximumAmount());
        mandate.setFrequency(request.frequency());
        mandate.setStatus(MandateStatus.PENDING);
        mandate.setValidFrom(LocalDate.now());
        mandate.setValidTo(LocalDate.now().plusYears(1));
        mandate.setCreditorAccount(creditor);
        mandate.setDebtorAccount(debtor);

        mandate = mandateRepository.save(mandate);
        log.info("PayTo mandate {} created: {} -> {}", mandate.getMandateId(),
                creditor.getAccountName(), debtor.getAccountName());

        return toResponse(mandate);
    }

    @Transactional
    public MandateResponse approveMandate(Long id) {
        PayToMandate mandate = findMandateById(id);
        if (mandate.getStatus() != MandateStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING mandates, current status: " + mandate.getStatus());
        }
        mandate.setStatus(MandateStatus.ACTIVE);
        mandateRepository.save(mandate);
        log.info("PayTo mandate {} approved", mandate.getMandateId());
        return toResponse(mandate);
    }

    @Transactional
    public MandateResponse rejectMandate(Long id) {
        PayToMandate mandate = findMandateById(id);
        if (mandate.getStatus() != MandateStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING mandates, current status: " + mandate.getStatus());
        }
        mandate.setStatus(MandateStatus.REJECTED);
        mandateRepository.save(mandate);
        log.info("PayTo mandate {} rejected", mandate.getMandateId());
        return toResponse(mandate);
    }

    @Transactional
    public PaymentResponse executePayment(Long mandateId, MandateExecuteRequest request) {
        PayToMandate mandate = findMandateById(mandateId);

        if (mandate.getStatus() != MandateStatus.ACTIVE) {
            throw new IllegalStateException("Mandate must be ACTIVE to execute payments, current status: " + mandate.getStatus());
        }

        if (request.amount().compareTo(mandate.getMaximumAmount()) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Amount %s exceeds mandate maximum of %s",
                    request.amount().toPlainString(), mandate.getMaximumAmount().toPlainString()));
        }

        // Create payment from mandate
        PaymentRequest paymentRequest = new PaymentRequest(
                request.amount(),
                null, null,
                mandate.getCreditorAccount().getBsb(),
                mandate.getCreditorAccount().getAccountNumber(),
                mandate.getDebtorAccount().getBsb(),
                mandate.getDebtorAccount().getAccountNumber(),
                request.remittanceInfo() != null ? request.remittanceInfo() : "PayTo: " + mandate.getDescription()
        );

        log.info("Executing PayTo mandate {} for {} AUD", mandate.getMandateId(), request.amount());
        return paymentService.initiatePayment(paymentRequest);
    }

    @Transactional(readOnly = true)
    public List<MandateResponse> getAllMandates() {
        return mandateRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MandateResponse getMandate(Long id) {
        return toResponse(findMandateById(id));
    }

    private PayToMandate findMandateById(Long id) {
        return mandateRepository.findById(id)
                .orElseThrow(() -> new MandateNotFoundException("Mandate not found: " + id));
    }

    private MandateResponse toResponse(PayToMandate m) {
        return new MandateResponse(
                m.getId(),
                m.getMandateId(),
                m.getDescription(),
                m.getMaximumAmount(),
                m.getFrequency(),
                m.getStatus(),
                m.getValidFrom(),
                m.getValidTo(),
                m.getCreditorAccount().getAccountName(),
                m.getCreditorAccount().getBsb(),
                m.getCreditorAccount().getAccountNumber(),
                m.getDebtorAccount().getAccountName(),
                m.getDebtorAccount().getBsb(),
                m.getDebtorAccount().getAccountNumber(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
