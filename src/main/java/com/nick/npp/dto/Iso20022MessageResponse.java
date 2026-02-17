package com.nick.npp.dto;

import com.nick.npp.model.Iso20022MessageType;
import java.time.LocalDateTime;

public record Iso20022MessageResponse(
        Long id,
        Iso20022MessageType messageType,
        String messageId,
        String direction,
        String senderBic,
        String receiverBic,
        String xmlContent,
        String paymentId,
        LocalDateTime createdAt
) {}
