package com.nick.npp.repository;

import com.nick.npp.model.PayToMandate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayToMandateRepository extends JpaRepository<PayToMandate, Long> {
    Optional<PayToMandate> findByMandateId(String mandateId);
    List<PayToMandate> findAllByOrderByCreatedAtDesc();
}
