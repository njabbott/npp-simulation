package com.nick.npp.controller;

import com.nick.npp.model.NppParticipant;
import com.nick.npp.repository.NppParticipantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Participants", description = "NPP connected financial institution participants")
@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final NppParticipantRepository participantRepository;

    public ParticipantController(NppParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @Operation(summary = "List all NPP participants")
    @GetMapping
    public ResponseEntity<List<NppParticipant>> getAllParticipants() {
        return ResponseEntity.ok(participantRepository.findAll());
    }
}
