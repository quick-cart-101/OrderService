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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final ProductService productService;

    //TODO create separate initializer to set this at run time
    private final Double gstRate = 0D;
    private final Double additionalTax = 0D;

    public OrderServiceImpl(OrderRepo orderRepo, ProductService productService, OrderItemRepo itemRepo) {
        this.orderRepo = orderRepo;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Order createOrder(OrderDto orderDto) {
        Order order = new Order();
        order.setUserId(orderDto.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        List<UUID> productIds = orderDto.getItems().stream()
                .map(OrderItemDto::getProductId)
                .toList();

        Map<UUID, Product> productMap = productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderItem> items = prepareItems(orderDto, productMap, order);
        order.setOrderItems(items);

        Double totalAmount = items.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();
        order.setTotalAmount(calculateAmount(totalAmount));

        return orderRepo.save(order);
    }

    @Override
    public List<Order> getAllOrdersPlacedByUser(String username) {
        return orderRepo.findAllByUserId(UUID.fromString(username));
    }

    private List<OrderItem> prepareItems(OrderDto orderDto, Map<UUID, Product> productMap, Order order) {
        return orderDto.getItems().stream()
                .map(dto -> {
                    Product product = productMap.get(dto.getProductId());
                    Double price = product.getPrice() * dto.getQuantity();

                    return OrderItem.builder()
                            .order(order)
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
