package com.example.ecommerce.stock.listener;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.message.OrderItemMessage;
import com.example.ecommerce.message.OrderMessage;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.stock.client.OrderServiceClient;
import com.example.ecommerce.stock.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StockMessageListener {
    private static final Logger log = LoggerFactory.getLogger(StockMessageListener.class);

    private final ProductMapper productMapper;
    private final OrderServiceClient orderServiceClient;

    @Autowired
    public StockMessageListener(ProductMapper productMapper, OrderServiceClient orderServiceClient) {
        this.productMapper = productMapper;
        this.orderServiceClient = orderServiceClient;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderMessage(OrderMessage orderMessage) {
        log.info("Stock service received order message, orderId: {}", orderMessage.getOrderId());
        
        try {
            // 获取订单中所有商品的ID
            List<Long> productIds = orderMessage.getItems().stream()
                    .map(OrderItemMessage::getProductId)
                    .collect(Collectors.toList());

            // 查询商品库存（加锁）
            List<Product> products = productMapper.getProductByIdsForUpdate(productIds);
            Map<Long, Integer> stockMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, Product::getStock));

            // 检查所有商品是否有足够库存
            boolean hasStock = orderMessage.getItems().stream()
                    .allMatch(item -> stockMap.getOrDefault(item.getProductId(), 0) >= item.getQuantity());

            if (!hasStock) {
                log.warn("Insufficient stock for orderId: {}", orderMessage.getOrderId());
                // 通过OpenFeign调用订单服务更新状态
                orderServiceClient.updateOrderStatus(orderMessage.getOrderId(), "FAILED");
                return;
            }

            // 批量扣减库存
            int updatedRows = productMapper.batchUpdateStock(orderMessage.getItems());
            if (updatedRows < orderMessage.getItems().size()) {
                log.warn("Concurrent stock deduction failed for orderId: {}", orderMessage.getOrderId());
                // 通过OpenFeign调用订单服务更新状态
                orderServiceClient.updateOrderStatus(orderMessage.getOrderId(), "FAILED");
                return;
            }

            log.info("Stock deduction succeeded for orderId: {}", orderMessage.getOrderId());
            // 通过OpenFeign调用订单服务更新状态
            orderServiceClient.updateOrderStatus(orderMessage.getOrderId(), "SUCCESS");
            
        } catch (Exception e) {
            log.error("Error processing order message for orderId: {}", orderMessage.getOrderId(), e);
            try {
                // 通过OpenFeign调用订单服务更新状态
                orderServiceClient.updateOrderStatus(orderMessage.getOrderId(), "FAILED");
            } catch (Exception ex) {
                log.error("Failed to update order status for orderId: {}", orderMessage.getOrderId(), ex);
            }
        }
    }
} 