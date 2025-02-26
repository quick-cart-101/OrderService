package com.quickcart.orderservice.services;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.entities.Order;

public interface OrderService {
    Order createOrder(OrderDto orderDto);
}
