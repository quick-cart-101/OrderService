package com.quickcart.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemDto {
    private UUID productId;
    private Integer quantity;
}
