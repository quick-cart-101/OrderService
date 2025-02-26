package com.quickcart.orderservice.repositories;

import com.quickcart.orderservice.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID> {
}
