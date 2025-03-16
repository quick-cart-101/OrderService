package com.quickcart.orderservice.services;

public interface PaymentService {

    String createPayment(double amount);

    String refundPayment(String paymentId, Double totalAmount);
}
