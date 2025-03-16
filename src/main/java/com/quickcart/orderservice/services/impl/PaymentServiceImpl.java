package com.quickcart.orderservice.services.impl;

import com.quickcart.orderservice.dto.PaymentDto;
import com.quickcart.orderservice.dto.RefundPaymentDto;
import com.quickcart.orderservice.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate;

    @Value("${PAYMENT_SERVICE_BASE_URL}")
    private String PAYMENT_SERVICE_BASE_URL;

    public PaymentServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String createPayment(double amount) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setAmount(amount);
        ResponseEntity<String> forEntity = restTemplate.getForEntity(PAYMENT_SERVICE_BASE_URL + "/payment/create", String.class, paymentDto);
        if (forEntity.getStatusCode().is2xxSuccessful()) {
            return forEntity.getBody();
        }
        return null;
    }

    @Override
    public String refundPayment(String paymentId, Double totalAmount) {
        RefundPaymentDto refundPaymentDto = new RefundPaymentDto();
        refundPaymentDto.setPaymentId(paymentId);
        refundPaymentDto.setAmount(totalAmount);
        ResponseEntity<String> forEntity = restTemplate.getForEntity(PAYMENT_SERVICE_BASE_URL + "/payment/refund", String.class, refundPaymentDto);
        if (forEntity.getStatusCode().is2xxSuccessful()) {
            return forEntity.getBody();
        }
        return null;
    }
}
