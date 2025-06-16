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
import java.util.Random;
import java.util.UUID;
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
        else if (json != null) {
            log.warn("Product {} not found in DB, empty cache hit.",id);
            return null;
        }

        log.info("Product {} not found in cache, querying database.", id);

        String uniqueValue = UUID.randomUUID().toString();
        String lockKey = "lock:product:" + id;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey,uniqueValue,10,TimeUnit.SECONDS);
        if (success) {
            try {
                log.info("Lock acquired for product {}, querying database.", id);
                Product product = productMapper.findById(id);
                if (product != null) {
                    int ttl = 30 + new Random().nextInt(10);
                    stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(product), ttl, TimeUnit.MINUTES);
                } else {
                    int ttl = 5 + new Random().nextInt(5);
                    stringRedisTemplate.opsForValue().set(key, "", ttl, TimeUnit.MINUTES);
                    log.warn("Product {} not found in DB, cache empty placeholder.", id);
                }
                return product;
            }
            finally {
                if (uniqueValue.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                    stringRedisTemplate.delete(lockKey);
                }
            }
        }
        else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to acquire lock for product {}, retrying...", id);
            return queryProductById(id);
        }
    }
}
