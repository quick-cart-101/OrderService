package com.quickcart.orderservice.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderDoesNotExists) {
        super(orderDoesNotExists);
    }
}
