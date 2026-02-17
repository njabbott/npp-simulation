package com.nick.npp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payto_mandates")
public class PayToMandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String mandateId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maximumAmount;

    @Column(nullable = false)
    private String frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MandateStatus status;

    @Column(nullable = false)
    private LocalDate validFrom;

    private LocalDate validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_account_id", nullable = false)
    private BankAccount creditorAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_account_id", nullable = false)
    private BankAccount debtorAccount;

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

    public PayToMandate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMandateId() { return mandateId; }
    public void setMandateId(String mandateId) { this.mandateId = mandateId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getMaximumAmount() { return maximumAmount; }
    public void setMaximumAmount(BigDecimal maximumAmount) { this.maximumAmount = maximumAmount; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public MandateStatus getStatus() { return status; }
    public void setStatus(MandateStatus status) { this.status = status; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public BankAccount getCreditorAccount() { return creditorAccount; }
    public void setCreditorAccount(BankAccount creditorAccount) { this.creditorAccount = creditorAccount; }
    public BankAccount getDebtorAccount() { return debtorAccount; }
    public void setDebtorAccount(BankAccount debtorAccount) { this.debtorAccount = debtorAccount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
