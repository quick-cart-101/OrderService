package com.quickcart.orderservice.controllers;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.entities.Order;
import com.quickcart.orderservice.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto order) {
        return new ResponseEntity<>(orderService.createOrder(order), HttpStatus.CREATED);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Order>> getAllOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof String username) {
            List<Order> orders = orderService.getAllOrdersPlacedByUser(username);
            return ResponseEntity.ok(orders);
        }

        return null;
    }

    @PostMapping("/place")
    public  ResponseEntity<String> placeOrder(@RequestBody String orderId) {
        String paymentId = orderService.placeOrder(orderId);
        return ResponseEntity.ok(paymentId);
    }
}
