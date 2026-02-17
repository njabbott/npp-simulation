package com.nick.npp.repository;

import com.nick.npp.model.NppParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NppParticipantRepository extends JpaRepository<NppParticipant, Long> {
    Optional<NppParticipant> findByBic(String bic);
    Optional<NppParticipant> findByBsb(String bsb);
}
