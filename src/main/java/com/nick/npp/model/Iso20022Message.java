package com.nick.npp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "iso20022_messages")
public class Iso20022Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Iso20022MessageType messageType;

    @Column(nullable = false)
    private String messageId;

    @Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    private String xmlContent;

    @Column(nullable = false)
    private String direction;

    private String senderBic;

    private String receiverBic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private NppPayment payment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Iso20022Message() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Iso20022MessageType getMessageType() { return messageType; }
    public void setMessageType(Iso20022MessageType messageType) { this.messageType = messageType; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getXmlContent() { return xmlContent; }
    public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getSenderBic() { return senderBic; }
    public void setSenderBic(String senderBic) { this.senderBic = senderBic; }
    public String getReceiverBic() { return receiverBic; }
    public void setReceiverBic(String receiverBic) { this.receiverBic = receiverBic; }
    public NppPayment getPayment() { return payment; }
    public void setPayment(NppPayment payment) { this.payment = payment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
