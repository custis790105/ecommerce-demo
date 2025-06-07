package com.example.ecommerce.service;

import com.example.ecommerce.request.OrderRequest;

public interface OrderService {
    void insertOrder(OrderRequest orderRequest);
}
