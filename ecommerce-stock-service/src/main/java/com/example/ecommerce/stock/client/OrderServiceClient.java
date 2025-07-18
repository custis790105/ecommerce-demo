package com.example.ecommerce.stock.client;

import com.example.ecommerce.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderServiceClient {
    
    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    Result<Void> updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);
} 