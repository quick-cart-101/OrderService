package com.quickcart.orderservice.services;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.entities.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderDto orderDto);

    List<Order> getAllOrdersPlacedByUser(String username);

    String placeOrder(String orderId);
}
