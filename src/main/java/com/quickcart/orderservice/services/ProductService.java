package com.quickcart.orderservice.services;

import com.quickcart.orderservice.models.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> getProductsByIds(List<UUID> productIds);
}
