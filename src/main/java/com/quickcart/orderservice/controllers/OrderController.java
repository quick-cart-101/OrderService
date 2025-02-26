package com.quickcart.orderservice.controllers;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.entities.Order;
import com.quickcart.orderservice.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto order) {
        return new ResponseEntity<>(orderService.createOrder(order), HttpStatus.CREATED);
    }
}
