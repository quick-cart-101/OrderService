package com.quickcart.orderservice.services;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.dto.OrderItemDto;
import com.quickcart.orderservice.entities.Order;
import com.quickcart.orderservice.entities.OrderItem;
import com.quickcart.orderservice.entities.OrderStatus;
import com.quickcart.orderservice.exceptions.OrderNotFoundException;
import com.quickcart.orderservice.models.Product;
import com.quickcart.orderservice.repositories.OrderRepo;
import com.quickcart.orderservice.services.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderDto orderDto;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        // Set test-specific GST and additional tax values
        orderService.gstRate = BigDecimal.valueOf(5);
        orderService.additionalTax = BigDecimal.valueOf(2);

        UUID productId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setPrice(100.0);

        orderDto = new OrderDto();
        orderDto.setUserId(UUID.randomUUID());
        orderDto.setItems(List.of(
                new OrderItemDto(productId, 2)
        ));

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(orderDto.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(List.of(
                new OrderItem(order, productId, 2, 200.0)
        ));
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully() {
        // Arrange
        when(productService.getProductsByIds(any())).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order createdOrder = orderService.createOrder(orderDto);

        // Assert
        assertNotNull(createdOrder);
        assertEquals(orderDto.getUserId(), createdOrder.getUserId());
        assertEquals(214.0, createdOrder.getTotalAmount()); // 200 + 5% GST + 2% Tax
        assertEquals(1, createdOrder.getOrderItems().size());
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldUpdateOrderWithPaymentId() {
        // Arrange
        UUID orderId = order.getId();
        order.setTotalAmount(100.0);
        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentService.createPayment(order.getTotalAmount())).thenReturn("PAY123");

        // Act
        String paymentId = orderService.placeOrder(orderId.toString());

        // Assert
        assertEquals("PAY123", paymentId);
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        verify(orderRepo, times(1)).save(order);
    }

    @Test
    void placeOrder_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.placeOrder(orderId.toString()));
    }

    @Test
    void getAllOrdersPlacedByUser_ShouldReturnOrders() {
        // Arrange
        UUID userId = orderDto.getUserId();
        when(orderRepo.findAllByUserId(userId)).thenReturn(List.of(order));

        // Act
        List<Order> orders = orderService.getAllOrdersPlacedByUser(userId.toString());

        // Assert
        assertEquals(1, orders.size());
        assertEquals(userId, orders.get(0).getUserId());
        verify(orderRepo, times(1)).findAllByUserId(userId);
    }

    @Test
    void cancelOrder_ShouldCancelOrderSuccessfully() {
        // Arrange
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentId("PAY123");

        when(orderRepo.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentService.refundPayment("PAY123", order.getTotalAmount())).thenReturn("REF123");

        // Act
        orderService.cancelOrder(order.getId());

        // Assert
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals("REF123", order.getPaymentId());
        verify(orderRepo, times(1)).save(order);
    }

    @Test
    void cancelOrder_ShouldNotRefund_WhenOrderIsPending() {
        // Arrange
        order.setStatus(OrderStatus.PENDING);

        when(orderRepo.findById(order.getId())).thenReturn(Optional.of(order));

        // Act
        orderService.cancelOrder(order.getId());

        // Assert
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        verify(paymentService, never()).refundPayment(any(), any());
        verify(orderRepo, times(1)).save(order);
    }

    @Test
    void updateOrder_ShouldUpdateOrderSuccessfully() {
        // Arrange
        when(orderRepo.findById(order.getId())).thenReturn(Optional.of(order));
        when(productService.getProductsByIds(any())).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order updatedOrder = orderService.updateOrder(order.getId(), orderDto);

        // Assert
        assertNotNull(updatedOrder);
        assertEquals(orderDto.getUserId(), updatedOrder.getUserId());
        assertEquals(214.0, updatedOrder.getTotalAmount()); // 200 + 5% GST + 2% Tax
        verify(orderRepo, times(1)).save(order);
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrder(orderId, orderDto));
    }
}
