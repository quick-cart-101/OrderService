package com.quickcart.orderservice.services;

import com.quickcart.orderservice.models.Product;
import com.quickcart.orderservice.services.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    @Value("${product_service_base_url}")
    private String baseUrl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        baseUrl = "http://localhost:8080";
    }

    @Test
    void testGetProductsByIds() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        List<UUID> productIds = List.of(productId1, productId2);

        Product product1 = new Product(productId1, "Product 1", 10.0);
        Product product2 = new Product(productId2, "Product 2", 20.0);
        Product[] products = {product1, product2};

        when(restTemplate.postForEntity(anyString(), eq(productIds), eq(Product[].class)))
                .thenReturn(new ResponseEntity<>(products, HttpStatus.OK));

        List<Product> result = productService.getProductsByIds(productIds);

        assertEquals(2, result.size());
        assertEquals(product1, result.get(0));
        assertEquals(product2, result.get(1));
    }
}