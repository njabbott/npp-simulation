package com.nick.npp.repository;

import com.nick.npp.model.Iso20022Message;
import com.nick.npp.model.NppPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface Iso20022MessageRepository extends JpaRepository<Iso20022Message, Long> {
    List<Iso20022Message> findByPayment(NppPayment payment);
    List<Iso20022Message> findAllByOrderByCreatedAtDesc();
}
