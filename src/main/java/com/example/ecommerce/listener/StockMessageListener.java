package com.example.ecommerce.listener;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.message.OrderItemMessage;
import com.example.ecommerce.message.OrderMessage;
import com.example.ecommerce.model.Product;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StockMessageListener {

    private static final Logger log = LoggerFactory.getLogger(StockMessageListener.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderMessage(OrderMessage orderMessage) {
        log.info("Received order message, orderId: {}", orderMessage.getOrderId());
        List<Long> productIds = orderMessage.getItems().stream()
                .map(OrderItemMessage::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productMapper.getProductByIdsForUpdate(productIds);
        Map<Long, Integer> stockMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Product::getStock));

        // Check if all products have enough stock
        boolean hasStock = orderMessage.getItems().stream()
                .allMatch(item -> stockMap.getOrDefault(item.getProductId(), 0) >= item.getQuantity());
        if (!hasStock) {
            log.warn("Insufficient stock, orderId: {}", orderMessage.getOrderId());
            orderMapper.updateOrderStatus(orderMessage.getOrderId(), "FAILED");
            return;
        }

        int updatedRows = productMapper.batchUpdateStock(orderMessage.getItems());
        if (updatedRows < orderMessage.getItems().size()) {
            log.warn("Concurrent stock deduction failed, orderId: {}", orderMessage.getOrderId());
            orderMapper.updateOrderStatus(orderMessage.getOrderId(), "FAILED");
            return;
        }
        log.info("Stock deduction succeeded, orderId: {}", orderMessage.getOrderId());
        orderMapper.updateOrderStatus(orderMessage.getOrderId(), "SUCCESS");
    }
}