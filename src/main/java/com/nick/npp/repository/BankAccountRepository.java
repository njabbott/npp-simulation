package com.nick.npp.repository;

import com.nick.npp.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByBsbAndAccountNumber(String bsb, String accountNumber);
}
