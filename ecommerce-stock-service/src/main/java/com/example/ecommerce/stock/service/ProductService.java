package com.example.ecommerce.stock.service;

import com.example.ecommerce.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> queryAllProducts();
    
    Product queryProductById(Long id);
} 