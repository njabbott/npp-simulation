package com.nick.npp.repository;

import com.nick.npp.model.SettlementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {
    List<SettlementRecord> findAllByOrderBySettledAtDesc();
}
