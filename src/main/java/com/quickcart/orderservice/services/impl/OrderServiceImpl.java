package com.quickcart.orderservice.services.impl;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.dto.OrderItemDto;
import com.quickcart.orderservice.entities.Order;
import com.quickcart.orderservice.entities.OrderItem;
import com.quickcart.orderservice.entities.OrderStatus;
import com.quickcart.orderservice.models.Product;
import com.quickcart.orderservice.repositories.OrderItemRepo;
import com.quickcart.orderservice.repositories.OrderRepo;
import com.quickcart.orderservice.services.OrderService;
import com.quickcart.orderservice.services.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final ProductService productService;
    private final OrderItemRepo itemRepo;

    //TODO create separate initializer to set this at run time
    private final Double gstRate = 0D;
    private final Double additionalTax = 0D;

    public OrderServiceImpl(OrderRepo orderRepo, ProductService productService, OrderItemRepo itemRepo) {
        this.orderRepo = orderRepo;
        this.productService = productService;
        this.itemRepo = itemRepo;
    }

    @Override
    public Order createOrder(OrderDto orderDto) {
        List<UUID> productIds = orderDto.getItems().stream()
                .map(OrderItemDto::getProductId)
                .toList();

        Map<UUID, Product> productMap = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderItem> items = prepareItems(orderDto, productMap);
        Double totalAmount = items.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();

        List<OrderItem> savedItems = itemRepo.saveAll(items);

        Order order = prepareOrder(orderDto, savedItems, totalAmount);
        return orderRepo.save(order);
    }

    private Order prepareOrder(OrderDto orderDto, List<OrderItem> savedItems, Double totalAmount) {
        Order order = new Order();
        order.setOrderItems(savedItems);
        order.setUserId(orderDto.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(calculateAmount(totalAmount));
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private List<OrderItem> prepareItems(OrderDto orderDto, Map<UUID, Product> productMap) {
        return orderDto.getItems().stream()
                .map(dto -> {
                    Product product = productMap.get(dto.getProductId());
                    Double price = product.getPrice() * dto.getQuantity();

                    return OrderItem.builder()
                            .productId(dto.getProductId())
                            .quantity(dto.getQuantity())
                            .price(price)
                            .build();
                })
                .toList();
    }

    private Double calculateAmount(Double baseAmount) {
        if (baseAmount == null || baseAmount <= 0) {
            throw new IllegalArgumentException("Amount must be valid");
        }

        double gstAmount = (gstRate / 100) * baseAmount;
        double taxAmount = (additionalTax / 100) * baseAmount;

        return baseAmount + gstAmount + taxAmount;
    }
}
