package com.nick.npp.service;

import com.nick.npp.dto.PayIdResolutionResponse;
import com.nick.npp.exception.PayIdNotFoundException;
import com.nick.npp.model.PayIdType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PayIdServiceTest {

    @Autowired
    private PayIdService payIdService;

    @Test
    void resolvePhonePayId() {
        PayIdResolutionResponse result = payIdService.resolve(PayIdType.PHONE, "+61412345678");
        assertNotNull(result);
        assertEquals("John S", result.displayName());
        assertEquals("062-000", result.bsb());
        assertEquals("12345678", result.accountNumber());
        assertEquals("CTBAAU2S", result.bankBic());
    }

    @Test
    void resolveEmailPayId() {
        PayIdResolutionResponse result = payIdService.resolve(PayIdType.EMAIL, "sarah.j@email.com");
        assertNotNull(result);
        assertEquals("Sarah Johnson", result.displayName());
        assertEquals("Commonwealth Bank of Australia", result.bankName());
    }

    @Test
    void resolveAbnPayId() {
        PayIdResolutionResponse result = payIdService.resolve(PayIdType.ABN, "51824753556");
        assertNotNull(result);
        assertEquals("ACME Pty Ltd", result.displayName());
    }

    @Test
    void resolveNonExistentPayIdThrows() {
        assertThrows(PayIdNotFoundException.class,
                () -> payIdService.resolve(PayIdType.PHONE, "+61400000000"));
    }
}
