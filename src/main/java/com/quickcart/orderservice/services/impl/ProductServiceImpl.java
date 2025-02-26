package com.quickcart.orderservice.services.impl;

import com.quickcart.orderservice.models.Product;
import com.quickcart.orderservice.services.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductServiceImpl implements ProductService {

    private final RestTemplate restTemplate;

    @Value("${product_service_base_url}")
    private String baseUrl;
    private final String BULK_PRODUCT_URL = "/products/bulk";

    public ProductServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Product> getProductsByIds(List<UUID> productIds) {
        ResponseEntity<Product[]> responseEntity = restTemplate
                .postForEntity(baseUrl + BULK_PRODUCT_URL, productIds, Product[].class);
        if(responseEntity.hasBody()) {
            return Arrays.stream(Objects.requireNonNull(responseEntity.getBody())).toList();
        }
        return List.of();
    }
}
