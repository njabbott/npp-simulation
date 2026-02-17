package com.nick.npp.service;

import com.nick.npp.dto.SettlementBalanceResponse;
import com.nick.npp.exception.InsufficientEsaBalanceException;
import com.nick.npp.model.NppParticipant;
import com.nick.npp.model.NppPayment;
import com.nick.npp.model.SettlementRecord;
import com.nick.npp.repository.NppParticipantRepository;
import com.nick.npp.repository.SettlementRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final NppParticipantRepository participantRepository;
    private final SettlementRecordRepository settlementRecordRepository;

    public SettlementService(NppParticipantRepository participantRepository,
                             SettlementRecordRepository settlementRecordRepository) {
        this.participantRepository = participantRepository;
        this.settlementRecordRepository = settlementRecordRepository;
    }

    @Transactional
    public SettlementRecord settle(NppPayment payment) {
        NppParticipant debitParticipant = payment.getDebtorAgent();
        NppParticipant creditParticipant = payment.getCreditorAgent();
        BigDecimal amount = payment.getAmount();

        // Validate ESA balance
        if (debitParticipant.getEsaBalance().compareTo(amount) < 0) {
            throw new InsufficientEsaBalanceException(
                    String.format("Insufficient ESA balance for %s: available %s, required %s",
                            debitParticipant.getShortName(),
                            debitParticipant.getEsaBalance().toPlainString(),
                            amount.toPlainString()));
        }

        // Debit sender's ESA
        debitParticipant.setEsaBalance(debitParticipant.getEsaBalance().subtract(amount));
        participantRepository.save(debitParticipant);

        // Credit receiver's ESA
        creditParticipant.setEsaBalance(creditParticipant.getEsaBalance().add(amount));
        participantRepository.save(creditParticipant);

        // Create settlement record
        SettlementRecord record = new SettlementRecord();
        record.setPayment(payment);
        record.setAmount(amount);
        record.setDebitParticipant(debitParticipant);
        record.setCreditParticipant(creditParticipant);
        record.setDebitBalanceAfter(debitParticipant.getEsaBalance());
        record.setCreditBalanceAfter(creditParticipant.getEsaBalance());
        record.setSettledAt(LocalDateTime.now());

        log.info("FSS Settlement: {} AUD from {} ({}) -> {} ({})",
                amount.toPlainString(),
                debitParticipant.getShortName(), debitParticipant.getEsaBalance(),
                creditParticipant.getShortName(), creditParticipant.getEsaBalance());

        return settlementRecordRepository.save(record);
    }

    @Transactional
    public void reverseSettlement(NppPayment payment) {
        NppParticipant originalDebtor = payment.getDebtorAgent();
        NppParticipant originalCreditor = payment.getCreditorAgent();
        BigDecimal amount = payment.getAmount();

        // Reverse: credit original debtor, debit original creditor
        originalDebtor.setEsaBalance(originalDebtor.getEsaBalance().add(amount));
        participantRepository.save(originalDebtor);

        originalCreditor.setEsaBalance(originalCreditor.getEsaBalance().subtract(amount));
        participantRepository.save(originalCreditor);

        // Create reversal settlement record
        SettlementRecord record = new SettlementRecord();
        record.setPayment(payment);
        record.setAmount(amount.negate());
        record.setDebitParticipant(originalCreditor);
        record.setCreditParticipant(originalDebtor);
        record.setDebitBalanceAfter(originalCreditor.getEsaBalance());
        record.setCreditBalanceAfter(originalDebtor.getEsaBalance());
        record.setSettledAt(LocalDateTime.now());

        settlementRecordRepository.save(record);
        log.info("FSS Reversal: {} AUD returned {} -> {}", amount.toPlainString(),
                originalCreditor.getShortName(), originalDebtor.getShortName());
    }

    @Transactional(readOnly = true)
    public List<SettlementBalanceResponse> getAllBalances() {
        return participantRepository.findAll().stream()
                .map(p -> new SettlementBalanceResponse(p.getId(), p.getName(), p.getShortName(), p.getBic(), p.getEsaBalance()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementRecord> getTransactionLog() {
        return settlementRecordRepository.findAllByOrderBySettledAtDesc();
    }
}
