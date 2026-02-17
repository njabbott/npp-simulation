package com.nick.npp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "npp_payments")
public class NppPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentId;

    @Column(nullable = false)
    private String endToEndId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "AUD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String remittanceInfo;

    private String payIdUsed;

    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_account_id")
    private BankAccount debtorAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_account_id")
    private BankAccount creditorAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_agent_id")
    private NppParticipant debtorAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_agent_id")
    private NppParticipant creditorAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NppPayment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getEndToEndId() { return endToEndId; }
    public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getRemittanceInfo() { return remittanceInfo; }
    public void setRemittanceInfo(String remittanceInfo) { this.remittanceInfo = remittanceInfo; }
    public String getPayIdUsed() { return payIdUsed; }
    public void setPayIdUsed(String payIdUsed) { this.payIdUsed = payIdUsed; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public BankAccount getDebtorAccount() { return debtorAccount; }
    public void setDebtorAccount(BankAccount debtorAccount) { this.debtorAccount = debtorAccount; }
    public BankAccount getCreditorAccount() { return creditorAccount; }
    public void setCreditorAccount(BankAccount creditorAccount) { this.creditorAccount = creditorAccount; }
    public NppParticipant getDebtorAgent() { return debtorAgent; }
    public void setDebtorAgent(NppParticipant debtorAgent) { this.debtorAgent = debtorAgent; }
    public NppParticipant getCreditorAgent() { return creditorAgent; }
    public void setCreditorAgent(NppParticipant creditorAgent) { this.creditorAgent = creditorAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
