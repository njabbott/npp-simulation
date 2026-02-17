package com.nick.npp.service;

import com.nick.npp.dto.PayIdResolutionResponse;
import com.nick.npp.exception.PayIdNotFoundException;
import com.nick.npp.model.BankAccount;
import com.nick.npp.model.PayId;
import com.nick.npp.model.PayIdType;
import com.nick.npp.repository.PayIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayIdService {

    private static final Logger log = LoggerFactory.getLogger(PayIdService.class);

    private final PayIdRepository payIdRepository;

    public PayIdService(PayIdRepository payIdRepository) {
        this.payIdRepository = payIdRepository;
    }

    @Transactional(readOnly = true)
    public PayIdResolutionResponse resolve(PayIdType type, String value) {
        PayId payId = payIdRepository.findByTypeAndValueAndActiveTrue(type, value)
                .orElseThrow(() -> new PayIdNotFoundException(
                        "PayID not found: " + type + " / " + value));

        BankAccount account = payId.getBankAccount();
        log.info("Resolved PayID {} ({}) -> {} at {}",
                value, type, account.getAccountName(), account.getParticipant().getShortName());

        return new PayIdResolutionResponse(
                payId.getType(),
                payId.getValue(),
                payId.getDisplayName(),
                account.getBsb(),
                account.getAccountNumber(),
                account.getParticipant().getName(),
                account.getParticipant().getBic()
        );
    }
}
