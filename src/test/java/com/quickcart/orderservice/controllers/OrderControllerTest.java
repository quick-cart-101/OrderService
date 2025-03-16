package com.quickcart.orderservice.controllers;

import com.quickcart.orderservice.dto.OrderDto;
import com.quickcart.orderservice.entities.Order;
import com.quickcart.orderservice.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder() {
        OrderDto orderDto = new OrderDto();
        Order order = new Order();
        when(orderService.createOrder(any(OrderDto.class))).thenReturn(order);

        ResponseEntity<Order> response = orderController.createOrder(orderDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void testGetAllOrders() {
        List<Order> orders = List.of(new Order());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("username");
        when(orderService.getAllOrdersPlacedByUser(anyString())).thenReturn(orders);
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<List<Order>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orders, response.getBody());
    }

    @Test
    void testPlaceOrder() {
        String orderId = "orderId";
        String paymentId = "paymentId";
        when(orderService.placeOrder(anyString())).thenReturn(paymentId);

        ResponseEntity<String> response = orderController.placeOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paymentId, response.getBody());
    }

    @Test
    void testUpdateOrder() {
        UUID id = UUID.randomUUID();
        OrderDto orderDto = new OrderDto();
        Order order = new Order();
        when(orderService.updateOrder(any(UUID.class), any(OrderDto.class))).thenReturn(order);

        ResponseEntity<Order> response = orderController.updateOrder(id, orderDto);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void testCancelOrder() {
        UUID orderId = UUID.randomUUID();
        doNothing().when(orderService).cancelOrder(any(UUID.class));

        ResponseEntity<String> response = orderController.cancelOrder(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order cancelled", response.getBody());
    }
}