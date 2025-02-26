package com.quickcart.orderservice.repositories;

import com.quickcart.orderservice.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepo extends JpaRepository<OrderItem, UUID> {
}
