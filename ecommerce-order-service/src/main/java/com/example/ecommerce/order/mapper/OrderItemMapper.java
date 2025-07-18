package com.example.ecommerce.order.mapper;

import com.example.ecommerce.model.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper {
    void insertOrderItem(OrderItem orderItem);
} 