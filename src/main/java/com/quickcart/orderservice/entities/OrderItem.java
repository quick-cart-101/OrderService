package com.quickcart.orderservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private UUID productId;
    private Integer quantity;
    private Double price;

    public OrderItem(UUID productId, Integer quantity, Double price) {
    }
}
