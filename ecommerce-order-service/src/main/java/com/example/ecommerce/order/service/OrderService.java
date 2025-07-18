package com.example.ecommerce.order.service;

import com.example.ecommerce.request.OrderRequest;

public interface OrderService {
    void createOrder(OrderRequest orderRequest);
    void updateOrderStatus(Long orderId, String status);
} 