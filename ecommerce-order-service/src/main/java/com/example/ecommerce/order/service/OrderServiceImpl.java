package com.example.ecommerce.order.service;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.message.OrderItemMessage;
import com.example.ecommerce.message.OrderMessage;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.order.mapper.OrderItemMapper;
import com.example.ecommerce.order.mapper.OrderMapper;
import com.example.ecommerce.request.OrderItemRequest;
import com.example.ecommerce.request.OrderRequest;
import com.example.ecommerce.response.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, RabbitTemplate rabbitTemplate) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public void createOrder(OrderRequest orderRequest) {
        // 准备订单数据
        Order order = new Order();
        List<OrderItemRequest> orderItemRequestList = orderRequest.getOrderItemRequestList();
        if (orderItemRequestList == null || orderItemRequestList.isEmpty()) {
            throw new BusinessException("Order item list must not be empty.");
        }

        BigDecimal totalPrice = orderItemRequestList.stream()
                .map(OrderItemRequest::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);
        order.setCreatedAt(LocalDateTime.now());
        order.setUserId(orderRequest.getUserId());
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setStatus("PENDING");

        // 保存订单到数据库
        orderMapper.insertOrder(order);
        Long orderId = order.getId();

        // 保存订单项并构建消息
        List<OrderItemMessage> orderItemMessages = new ArrayList<>();
        for (OrderItemRequest item : orderItemRequestList) {
            OrderItem orderItem = new OrderItem();
            OrderItemMessage orderItemMessage = new OrderItemMessage();
            orderItem.setUserId(order.getUserId());
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemMessage.setProductId(item.getProductId());
            orderItemMessage.setQuantity(item.getQuantity());
            orderItemMessages.add(orderItemMessage);
            orderItemMapper.insertOrderItem(orderItem);
        }

        log.info("Creating order for customer: {}", orderRequest.getCustomerName());
        log.debug("Total order items: {}, Total price: {}", orderItemRequestList.size(), totalPrice);

        // 发送订单创建消息到RabbitMQ
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(orderId);
        orderMessage.setItems(orderItemMessages);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                orderMessage);
        log.info("Sent order creation message for order: {}", orderId);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        orderMapper.updateOrderStatus(orderId, status);
        log.info("Updated order {} status to: {}", orderId, status);
    }
} 