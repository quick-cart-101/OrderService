package com.quickcart.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderDto {
    private UUID userId;
    private List<OrderItemDto> items;
}
