package com.example.ecommerce.mapper;

import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findAll();

    List<Product> getProductByIdsForUpdate(List<Integer> ids);

    void updateStock(@Param("quantity") Integer quantity, @Param("productId") Integer productID);

    void batchUpdateStock(@Param("items") List<OrderItem> items);
}
