package com.example.ecommerce.mapper;

import com.example.ecommerce.message.OrderItemMessage;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findAll();

    Product findById(@Param("id") Long id);

    List<Product> getProductByIdsForUpdate(List<Long> ids);

    void updateStock(@Param("quantity") Integer quantity, @Param("productId") Long productID);

    int batchUpdateStock(@Param("items") List<OrderItemMessage> items);
}
