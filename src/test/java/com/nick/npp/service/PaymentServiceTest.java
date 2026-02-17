package com.nick.npp.service;

import com.nick.npp.dto.PaymentRequest;
import com.nick.npp.dto.PaymentResponse;
import com.nick.npp.exception.AccountNotFoundException;
import com.nick.npp.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void initiatePaymentViaPayId() {
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("50.00"),
                "PHONE", "+61498765432",
                null, null,
                "638-060", "12345678",
                "Test payment via PayID"
        );
        PaymentResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertNotNull(response.paymentId());
        assertEquals(new BigDecimal("50.00"), response.amount());
        assertEquals("AUD", response.currency());
        assertEquals("John Smith", response.debtorAccountName());
        assertEquals("Mike Wilson", response.creditorAccountName());
        assertTrue(response.payIdUsed().contains("PHONE"));
    }

    @Test
    void initiatePaymentViaBsbAccount() {
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("100.00"),
                null, null,
                "083-000", "22334455",
                "062-000", "87654321",
                "BSB payment test"
        );
        PaymentResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("Sarah Johnson", response.debtorAccountName());
        assertEquals("Mike Wilson", response.creditorAccountName());
    }

    @Test
    void initiatePaymentInvalidAccountThrows() {
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("10.00"),
                null, null,
                "083-000", "22334455",
                "999-999", "00000000",
                null
        );
        assertThrows(AccountNotFoundException.class,
                () -> paymentService.initiatePayment(request));
    }

    @Test
    void getAllPaymentsReturnsResults() {
        // Ensure at least one payment exists
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("25.00"),
                "EMAIL", "james.b@email.com",
                null, null,
                "012-000", "33445566",
                null
        );
        paymentService.initiatePayment(request);

        List<PaymentResponse> payments = paymentService.getAllPayments();
        assertFalse(payments.isEmpty());
    }
}
