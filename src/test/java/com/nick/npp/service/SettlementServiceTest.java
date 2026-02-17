package com.nick.npp.service;

import com.nick.npp.dto.SettlementBalanceResponse;
import com.nick.npp.exception.InsufficientEsaBalanceException;
import com.nick.npp.model.*;
import com.nick.npp.repository.BankAccountRepository;
import com.nick.npp.repository.NppParticipantRepository;
import com.nick.npp.repository.NppPaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private NppParticipantRepository participantRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private NppPaymentRepository paymentRepository;

    @Test
    void settlePaymentUpdatesEsaBalances() {
        NppParticipant pfb = participantRepository.findByBic("HBSLAU4T").orElseThrow();
        NppParticipant nab = participantRepository.findByBic("NATAAU33").orElseThrow();
        BigDecimal pfbBalanceBefore = pfb.getEsaBalance();
        BigDecimal nabBalanceBefore = nab.getEsaBalance();

        BankAccount debtor = bankAccountRepository.findByBsbAndAccountNumber("638-060", "12345678").orElseThrow();
        BankAccount creditor = bankAccountRepository.findByBsbAndAccountNumber("083-000", "22334455").orElseThrow();

        NppPayment payment = new NppPayment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setEndToEndId("E2ETEST001");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setCurrency("AUD");
        payment.setStatus(PaymentStatus.CLEARING);
        payment.setDebtorAccount(debtor);
        payment.setCreditorAccount(creditor);
        payment.setDebtorAgent(pfb);
        payment.setCreditorAgent(nab);
        payment = paymentRepository.save(payment);

        SettlementRecord record = settlementService.settle(payment);

        assertNotNull(record);
        assertEquals(new BigDecimal("1000.00"), record.getAmount());
        assertEquals(pfbBalanceBefore.subtract(new BigDecimal("1000.00")), record.getDebitBalanceAfter());
        assertEquals(nabBalanceBefore.add(new BigDecimal("1000.00")), record.getCreditBalanceAfter());
    }

    @Test
    void settleWithInsufficientBalanceThrows() {
        NppParticipant pfb = participantRepository.findByBic("HBSLAU4T").orElseThrow();
        NppParticipant nab = participantRepository.findByBic("NATAAU33").orElseThrow();

        BankAccount debtor = bankAccountRepository.findByBsbAndAccountNumber("638-060", "12345678").orElseThrow();
        BankAccount creditor = bankAccountRepository.findByBsbAndAccountNumber("083-000", "22334455").orElseThrow();

        NppPayment payment = new NppPayment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setEndToEndId("E2ETEST002");
        payment.setAmount(new BigDecimal("999999999.00"));
        payment.setCurrency("AUD");
        payment.setStatus(PaymentStatus.CLEARING);
        payment.setDebtorAccount(debtor);
        payment.setCreditorAccount(creditor);
        payment.setDebtorAgent(pfb);
        payment.setCreditorAgent(nab);
        paymentRepository.save(payment);

        assertThrows(InsufficientEsaBalanceException.class,
                () -> settlementService.settle(payment));
    }

    @Test
    void getAllBalancesReturnsFiveBanks() {
        List<SettlementBalanceResponse> balances = settlementService.getAllBalances();
        assertEquals(5, balances.size());
    }
}
