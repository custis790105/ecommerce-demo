package com.example.ecommerce.service;

import com.example.ecommerce.mapper.OrderItemMapper;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.request.OrderItemRequest;
import com.example.ecommerce.request.OrderRequest;
import com.example.ecommerce.response.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderServiceImpl implements OrderService{
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);


    @Autowired
    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void insertOrder(OrderRequest orderRequest) {
        // Prepare order details
        Order order = new Order();
        List<OrderItemRequest> orderItemRequestList = orderRequest.getOrderItemRequestList();
        if(orderItemRequestList == null || orderItemRequestList.isEmpty()){
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

        // Save order to database
        orderMapper.insertOrder(order);
        Long orderId = order.getId();

        // Save order items and update product stock
        List<Long> productIds = orderItemRequestList.stream()
                .map(OrderItemRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<Product> productList = productMapper.getProductByIdsForUpdate(productIds);
        Map<Long, Product> productMap = productList.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest item : orderItemRequestList) {
            Product product = productMap.get(item.getProductId());
            Integer availableStock = product.getStock();
            Integer requestedQty = item.getQuantity();
            if (availableStock < requestedQty) {
                throw new BusinessException(String.format(
                        "Insufficient stock. Product ID=%d, Name=%s, Available=%d, Requested=%d",
                        product.getId(), product.getName(), product.getStock(), item.getQuantity()));
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(order.getUserId());
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemMapper.insertOrderItem(orderItem);
            orderItems.add(orderItem);
        }

        productMapper.batchUpdateStock(orderItems);

        log.info("Inserting order for customer: {}", orderRequest.getCustomerName());
        log.debug("Total order items: {}, Total price: {}", orderItemRequestList.size(), totalPrice);
    }
}
