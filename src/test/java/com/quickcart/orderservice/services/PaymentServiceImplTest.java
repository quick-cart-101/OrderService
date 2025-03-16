package com.quickcart.orderservice.services;

import com.quickcart.orderservice.dto.PaymentDto;
import com.quickcart.orderservice.dto.RefundPaymentDto;
import com.quickcart.orderservice.services.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Value("${PAYMENT_SERVICE_BASE_URL}")
    private String PAYMENT_SERVICE_BASE_URL;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PAYMENT_SERVICE_BASE_URL = "http://localhost:8080";
    }

    @Test
    void testCreatePayment() {
        double amount = 100.0;
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setAmount(amount);

        when(restTemplate.getForEntity(anyString(), eq(String.class), any(PaymentDto.class)))
                .thenReturn(new ResponseEntity<>("Payment Created", HttpStatus.OK));

        String result = paymentService.createPayment(amount);

        assertEquals("Payment Created", result);
    }

    @Test
    void testRefundPayment() {
        String paymentId = "12345";
        double totalAmount = 50.0;
        RefundPaymentDto refundPaymentDto = new RefundPaymentDto();
        refundPaymentDto.setPaymentId(paymentId);
        refundPaymentDto.setAmount(totalAmount);

        when(restTemplate.getForEntity(anyString(), eq(String.class), any(RefundPaymentDto.class)))
                .thenReturn(new ResponseEntity<>("Payment Refunded", HttpStatus.OK));

        String result = paymentService.refundPayment(paymentId, totalAmount);

        assertEquals("Payment Refunded", result);
    }
}