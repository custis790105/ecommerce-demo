package com.example.ecommerce.order.mapper;

import com.example.ecommerce.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);
} 