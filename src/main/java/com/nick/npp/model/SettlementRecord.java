package com.nick.npp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_records")
public class SettlementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal debitBalanceAfter;

    @Column(precision = 19, scale = 2)
    private BigDecimal creditBalanceAfter;

    @Column(nullable = false)
    private LocalDateTime settledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private NppPayment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_participant_id", nullable = false)
    private NppParticipant debitParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_participant_id", nullable = false)
    private NppParticipant creditParticipant;

    public SettlementRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getDebitBalanceAfter() { return debitBalanceAfter; }
    public void setDebitBalanceAfter(BigDecimal debitBalanceAfter) { this.debitBalanceAfter = debitBalanceAfter; }
    public BigDecimal getCreditBalanceAfter() { return creditBalanceAfter; }
    public void setCreditBalanceAfter(BigDecimal creditBalanceAfter) { this.creditBalanceAfter = creditBalanceAfter; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    public NppPayment getPayment() { return payment; }
    public void setPayment(NppPayment payment) { this.payment = payment; }
    public NppParticipant getDebitParticipant() { return debitParticipant; }
    public void setDebitParticipant(NppParticipant debitParticipant) { this.debitParticipant = debitParticipant; }
    public NppParticipant getCreditParticipant() { return creditParticipant; }
    public void setCreditParticipant(NppParticipant creditParticipant) { this.creditParticipant = creditParticipant; }
}
