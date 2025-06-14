package com.example.ecommerce.service;

import com.alibaba.fastjson.JSON;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class ProductServiceImpl implements ProductService{
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    public ProductServiceImpl(ProductMapper productMapper,StringRedisTemplate stringRedisTemplate) {
        this.productMapper = productMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    @Override
    public Product queryProductById(Long id) {
        String key = "product:detail:" + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            log.info("Product {} found in cache.", id);
            return JSON.parseObject(json, Product.class);
        }
        log.info("Product {} not found in cache, querying database.", id);

        Product product = productMapper.findById(id);
        if (product != null) {
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(product), 30, TimeUnit.MINUTES);
        }
        return product;
    }
}
