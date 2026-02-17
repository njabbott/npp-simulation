package com.nick.npp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "npp_participants")
public class NppParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String shortName;

    @Column(nullable = false, unique = true)
    private String bic;

    @Column(nullable = false)
    private String bsb;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal esaBalance;

    public NppParticipant() {}

    public NppParticipant(String name, String shortName, String bic, String bsb, BigDecimal esaBalance) {
        this.name = name;
        this.shortName = shortName;
        this.bic = bic;
        this.bsb = bsb;
        this.esaBalance = esaBalance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getBic() { return bic; }
    public void setBic(String bic) { this.bic = bic; }
    public String getBsb() { return bsb; }
    public void setBsb(String bsb) { this.bsb = bsb; }
    public BigDecimal getEsaBalance() { return esaBalance; }
    public void setEsaBalance(BigDecimal esaBalance) { this.esaBalance = esaBalance; }
}
