package com.example.ecommerce.message;

import java.util.List;

public class OrderMessage {
    private Long orderId;
    private List<OrderItemMessage> items;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public List<OrderItemMessage> getItems() {
        return items;
    }

    public void setItems(List<OrderItemMessage> items) {
        this.items = items;
    }
} 