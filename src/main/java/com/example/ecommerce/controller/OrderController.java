package com.example.ecommerce.controller;

import com.example.ecommerce.request.OrderRequest;
import com.example.ecommerce.response.Result;
import com.example.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<Void> insertOrder(@RequestBody OrderRequest orderRequest) {
        orderService.insertOrder(orderRequest);
        return Result.success("Order created successfully",null);
    }

}
