# stage-03-redis-cache 缓存进阶处理笔记（商品详情）

本阶段主要处理商品详情缓存，并解决以下三类缓存问题：渗透、雪崩、击穿。

---

## 一、缓存渗透

- 问题：请求的商品 ID 在数据库中也不存在，缓存也没有，导致不断访问数据库。
- 解决方案：
  - 如果数据库查询结果为空，缓存一个空字符串 `""`。
  - 设置较短 TTL（如 5~10 分钟），防止空缓存长期存在。
  - 判断逻辑中注意使用：
    ```java
    if (json != null && !StringUtils.hasText(json)) {
        return null;
    }
    ```
- 注意事项：
  - `StringUtils.hasText(json)` 可以排除 null、""、" " 等无效缓存。
  - 这比直接判断 `json != null` 更严谨。

---

## 二、缓存雪崩

- 问题：大量缓存 key 设置了相同的 TTL，可能同一时间全部过期，导致数据库压力激增。
- 解决方案：
  - 设置 TTL 时加上随机抖动，分散缓存过期时间：
    ```java
    int ttl = 30 + new Random().nextInt(10);
    ```

---

## 三、缓存击穿

- 问题：某个热点 key 在瞬间过期，多个线程同时访问，导致并发打数据库。
- 解决方案：
  - 使用 Redis 分布式锁：
    - 加锁：`setIfAbsent(key, uuid, 10, TimeUnit.SECONDS)`
    - 释放锁前判断 value 是否一致，防止误删别人加的锁。
  - 未获取到锁的线程可短暂 sleep 后递归重试。
  - 示例代码片段：
    ```java
    String lockKey = "lock:product:" + id;
    String uniqueValue = UUID.randomUUID().toString();
    Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uniqueValue, 10, TimeUnit.SECONDS);
    if (success) {
        try {
            // 查询数据库并写入缓存
        } finally {
            if (uniqueValue.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.delete(lockKey);
            }
        }
    } else {
        Thread.sleep(50);
        return queryProductById(id);
    }
    ```

---

## 其他细节记录

- Redis Key 命名采用冒号分隔（如：`product:detail:{id}`），模拟命名空间结构。
- 缓存内容为 JSON 字符串，使用 FastJSON 进行序列化/反序列化。
- 加锁的 value 推荐使用 UUID，避免误删其他线程加的锁。
- 分布式锁适用于单机和多节点部署，当前写法考虑了锁误删的问题。
- 目前未设置最大重试次数，后续如需控制递归调用次数可通过参数累加方式实现。

---
