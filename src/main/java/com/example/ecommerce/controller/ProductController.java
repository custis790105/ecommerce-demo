package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.response.Result;
import com.example.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<Map<String, Object>> getAllProducts() {
        Map<String, Object> result = new HashMap<>();
        result.put("instance", System.getenv("HOSTNAME"));
        result.put("data", productService.getAllProducts());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Product> queryProductById(@PathVariable long id) {
        Product product = productService.queryProductById(id);
        if (product != null) {
            return Result.success(product);
        } else {
            return Result.failure("Product not found.");
        }

    }
}
