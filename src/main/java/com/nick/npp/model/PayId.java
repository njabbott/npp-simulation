package com.nick.npp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pay_ids")
public class PayId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayIdType type;

    @Column(name = "pay_id_value", nullable = false)
    private String value;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    public PayId() {}

    public PayId(PayIdType type, String value, String displayName, boolean active, BankAccount bankAccount) {
        this.type = type;
        this.value = value;
        this.displayName = displayName;
        this.active = active;
        this.bankAccount = bankAccount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayIdType getType() { return type; }
    public void setType(PayIdType type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }
}
