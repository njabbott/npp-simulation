package com.nick.npp.repository;

import com.nick.npp.model.PayId;
import com.nick.npp.model.PayIdType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PayIdRepository extends JpaRepository<PayId, Long> {
    Optional<PayId> findByTypeAndValueAndActiveTrue(PayIdType type, String value);
}
