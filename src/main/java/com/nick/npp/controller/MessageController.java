package com.nick.npp.controller;

import com.nick.npp.dto.Iso20022MessageResponse;
import com.nick.npp.model.Iso20022Message;
import com.nick.npp.service.Iso20022MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ISO 20022 Messages", description = "View ISO 20022 pacs/camt messages exchanged during payment processing")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final Iso20022MessageService messageService;

    public MessageController(Iso20022MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "List all ISO 20022 messages")
    @GetMapping
    public ResponseEntity<List<Iso20022MessageResponse>> getAllMessages() {
        List<Iso20022MessageResponse> messages = messageService.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Get a message by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Iso20022MessageResponse> getMessage(@PathVariable Long id) {
        Iso20022Message msg = messageService.findById(id);
        if (msg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(msg));
    }

    @Operation(summary = "Get raw XML for a message", description = "Returns the full ISO 20022 XML payload for a given message")
    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getMessageXml(@PathVariable Long id) {
        Iso20022Message msg = messageService.findById(id);
        if (msg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(msg.getXmlContent());
    }

    private Iso20022MessageResponse toResponse(Iso20022Message m) {
        return new Iso20022MessageResponse(
                m.getId(),
                m.getMessageType(),
                m.getMessageId(),
                m.getDirection(),
                m.getSenderBic(),
                m.getReceiverBic(),
                m.getXmlContent(),
                m.getPayment() != null ? m.getPayment().getPaymentId() : null,
                m.getCreatedAt()
        );
    }
}
