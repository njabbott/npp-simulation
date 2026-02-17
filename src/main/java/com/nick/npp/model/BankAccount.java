package com.nick.npp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String bsb;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private NppParticipant participant;

    public BankAccount() {}

    public BankAccount(String accountNumber, String bsb, String accountName, BigDecimal balance, NppParticipant participant) {
        this.accountNumber = accountNumber;
        this.bsb = bsb;
        this.accountName = accountName;
        this.balance = balance;
        this.participant = participant;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getBsb() { return bsb; }
    public void setBsb(String bsb) { this.bsb = bsb; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public NppParticipant getParticipant() { return participant; }
    public void setParticipant(NppParticipant participant) { this.participant = participant; }
}
