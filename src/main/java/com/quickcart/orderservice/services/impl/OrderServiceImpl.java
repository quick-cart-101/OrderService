package com.quickcart.orderservice.services.impl;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.dto.OrderItemDto;
import com.quickcart.orderservice.entities.Order;
import com.quickcart.orderservice.entities.OrderItem;
import com.quickcart.orderservice.entities.OrderStatus;
import com.quickcart.orderservice.exceptions.OrderNotFoundException;
import com.quickcart.orderservice.models.Product;
import com.quickcart.orderservice.repositories.OrderRepo;
import com.quickcart.orderservice.services.OrderService;
import com.quickcart.orderservice.services.PaymentService;
import com.quickcart.orderservice.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final ProductService productService;
    private final PaymentService paymentService;

    @Value("${gstRate}")
    private BigDecimal gstRate;
    @Value("${additionalTax}")
    private BigDecimal additionalTax;

    public OrderServiceImpl(OrderRepo orderRepo, ProductService productService, PaymentService paymentService) {
        this.orderRepo = orderRepo;
        this.productService = productService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public Order createOrder(OrderDto orderDto) {
        log.info("Creating order for user: {}", orderDto.getUserId());

        Order order = new Order();
        order.setUserId(orderDto.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        Map<UUID, Product> productMap = fetchProductMap(orderDto);

        List<OrderItem> items = prepareItems(orderDto, productMap, order);
        order.setOrderItems(items);

        BigDecimal totalAmount = items.stream()
                .map(item -> BigDecimal.valueOf(item.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(calculateAmount(totalAmount).doubleValue());
        return orderRepo.save(order);
    }

    @Override
    public List<Order> getAllOrdersPlacedByUser(String username) {
        UUID userId = parseUuid(username);
        log.info("Fetching all orders for user ID: {}", userId);
        return orderRepo.findAllByUserId(userId);
    }

    @Override
    public String placeOrder(String orderId) {
        UUID id = parseUuid(orderId);
        Order order = findOrderById(id);

        String paymentId = paymentService.createPayment(order.getTotalAmount());
        order.setPaymentId(paymentId);
        order.setStatus(OrderStatus.PROCESSING);

        orderRepo.save(order);
        log.info("Order placed successfully with payment ID: {}", paymentId);
        return paymentId;
    }

    @Override
    public void cancelOrder(UUID orderId) {
        Order order = findOrderById(orderId);

        switch (order.getStatus()) {
            case PENDING -> log.info("Cancelling pending order: {}", orderId);
            case PROCESSING, SHIPPED, DELIVERED -> {
                log.info("Cancelling {} order: {}", order.getStatus(), orderId);
                if (order.getPaymentId() != null) {
                    String refundId = paymentService.refundPayment(order.getPaymentId(), order.getTotalAmount());
                    order.setPaymentId(refundId);
                }
            }
            default -> log.warn("Order status not cancellable: {}", order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepo.save(order);
        log.info("Order {} cancelled successfully", orderId);
    }

    @Override
    public Order updateOrder(UUID orderId, OrderDto orderDto) {

        Order existingOrder = findOrderById(orderId);

        existingOrder.setOrderItems(prepareItems(orderDto, fetchProductMap(orderDto), existingOrder));
        existingOrder.setTotalAmount(calculateAmount(
                existingOrder.getOrderItems().stream()
                        .map(item -> BigDecimal.valueOf(item.getPrice()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        ).doubleValue());

        return orderRepo.save(existingOrder);
    }

    private Map<UUID, Product> fetchProductMap(OrderDto orderDto) {
        List<UUID> productIds = orderDto.getItems().stream()
                .map(OrderItemDto::getProductId)
                .toList();

        return productService.getProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }

    private List<OrderItem> prepareItems(OrderDto orderDto, Map<UUID, Product> productMap, Order order) {
        return orderDto.getItems().stream()
                .map(dto -> {
                    Product product = productMap.get(dto.getProductId());
                    if (product == null) {
                        throw new IllegalArgumentException("Invalid product ID: " + dto.getProductId());
                    }

                    BigDecimal price = BigDecimal.valueOf(product.getPrice())
                            .multiply(BigDecimal.valueOf(dto.getQuantity()));

                    return OrderItem.builder()
                            .order(order)
                            .productId(dto.getProductId())
                            .quantity(dto.getQuantity())
                            .price(price.doubleValue())
                            .build();
                })
                .toList();
    }

    private BigDecimal calculateAmount(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal gstAmount = baseAmount.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = baseAmount.multiply(additionalTax).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return baseAmount.add(gstAmount).add(taxAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID format: " + value);
        }
    }

    private Order findOrderById(UUID orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + orderId));
    }
}
