package com.example.ecommerce.mapper;

import com.example.ecommerce.model.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    void updateOrderStatus(Long orderId, String status);
}
