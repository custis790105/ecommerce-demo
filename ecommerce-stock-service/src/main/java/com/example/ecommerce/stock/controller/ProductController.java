package com.example.ecommerce.stock.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.response.Result;
import com.example.ecommerce.stock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<List<Product>> getAllProducts() {
        List<Product> products = productService.queryAllProducts();
        return Result.success(products);
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productService.queryProductById(id);
        if (product != null) {
            return Result.success(product);
        } else {
            return Result.error("Product not found");
        }
    }
} 