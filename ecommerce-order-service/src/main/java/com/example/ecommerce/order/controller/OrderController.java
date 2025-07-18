package com.example.ecommerce.order.controller;

import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.request.OrderRequest;
import com.example.ecommerce.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<Void> createOrder(@RequestBody OrderRequest orderRequest) {
        orderService.createOrder(orderRequest);
        return Result.success("Order created successfully", null);
    }

    /**
     * 更新订单状态 - 供库存服务调用
     */
    @PutMapping("/{orderId}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return Result.success("Order status updated successfully", null);
    }
} 