# 第六阶段：Sharding-JDBC 分库分表

本阶段目标是使用 Sharding-JDBC 实现按 `user_id` 进行分库、按 `order_id` 进行分表的读写分离架构。

---

## 🎯 项目结构与目标

- 以 `order` 为主表，按 `user_id` 分库（`ds0`, `ds1`）
- `order` 表按 `id` 分表（`order_0`, `order_1`）
- `order_item` 表跟随 `order` 表按 `order_id` 分表（`order_item_0`, `order_item_1`）
- 读写分离架构保留（与第五阶段兼容）
- 使用雪花算法自动生成全局唯一主键
- 开发环境与部署环境保持一致的配置方式，实现代码不变即可切换运行环境

---

## ⚙️ 配置说明（sharding-prod.yml / sharding-dev.yml）

### 1. 数据源配置

```yaml
dataSources:
  ds0:
    url: jdbc:mysql://mysql-ds0-master:3306/ecommerce_ds0...
  ds1:
    url: jdbc:mysql://mysql-ds1-master:3306/ecommerce_ds1...
```

两个主库组成的分库结构，配合读写分离中配置的主从数据源名（ds0, ds1）。

### 2. 分库策略

```yaml
shardingAlgorithms:
  db_inline:
    type: INLINE
    props:
      algorithm-expression: ds${user_id % 2}
```

按 `user_id` 路由到 `ds0` 或 `ds1`。

### 3. 分表策略

```yaml
shardingAlgorithms:
  order_table_inline:
    type: INLINE
    props:
      algorithm-expression: order_${id % 2}

  order_item_table_inline:
    type: INLINE
    props:
      algorithm-expression: order_item_${order_id % 2}
```

说明：

- `order.id` 作为主键路由规则（必须能取到该值）
- `order_item.order_id` 作为从表路由规则
- 注意：算法表达式中不能含空格，必须符合 `${}` 语法

---

## 🧠 遇到的问题与排查记录

### ✅ 成功解决的问题

| 问题 | 原因分析 | 解决方案 |
|------|-----------|------------|
| `order_item_${order_id%2}` 报错表达式与分片键不匹配 | 分片键在 SQL 中无法正确识别 | `insert` 时未设置 `useGeneratedKeys` 导致主键为空 |
| 本地无法调试 | 配置文件仅适用于容器环境，主机名无法解析 | 复制 `sharding-prod.yml` 为 `sharding-dev.yml`，将连接改为 `localhost` |
| IDE 启动访问数据库失败 | 容器内数据库名 `mysql-ds-master` 在宿主机中无法解析 | 本地调试用 `localhost` 替换数据库连接配置 |

---

## 💡 注意事项与经验总结

1. Sharding-JDBC 表达式写法不要带空格，如 `order_${id % 2}`。
2. 一定确保 insert 时主键字段有值，否则分表表达式取不到值。
3. 如果使用雪花算法生成主键，MyBatis 依然要配置 `useGeneratedKeys="true" keyProperty="id"`。
4. `order_id` 在 `order` 表中叫 `id`，在 `order_item` 中叫 `order_id`，表达式要匹配实体类字段名。
5. 本地开发可以通过 `sharding-dev.yml` + 修改数据库连接为 `localhost` 的方式实现调试，不需要换配置结构。

---

## 🛠 本地与容器联调建议

- Docker容器启动时暴露端口：
  ```yaml
  ports:
    - 3306:3306
    - 6379:6379
  ```
- `application-dev.yml` 中：
  ```yaml
  spring:
    datasource:
      url: jdbc:shardingsphere:classpath:sharding-dev.yml
    redis:
      host: localhost
  ```

确保容器 MySQL、Redis 服务的端口映射正确，开发环境配置本地连接即可。

---

## ✅ 阶段结论

- ✅ 实现了按 user_id 分库、order_id 分表的完整策略
- ✅ 开发与部署配置统一，调试与部署互不影响
- ✅ 解决了生成主键为空、配置模式报错等关键问题