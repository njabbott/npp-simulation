package com.nick.npp.repository;

import com.nick.npp.model.NppPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NppPaymentRepository extends JpaRepository<NppPayment, Long> {
    Optional<NppPayment> findByPaymentId(String paymentId);
    List<NppPayment> findAllByOrderByCreatedAtDesc();
}
