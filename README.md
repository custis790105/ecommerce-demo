# Spring Boot + MySQL 单体应用开发纪要

## 架构与技术栈

- **后端框架**：Spring Boot 3.x
- **数据访问**：MyBatis + MySQL
- **日志系统**：SLF4J + LoggerFactory
- **JSON 通信**：RestController + Jackson（默认）
- **工程组织结构**：按领域划分（`model`, `request`, `response`, `service`, `mapper`, `controller` 等）

---

## 项目核心功能实现

### 🧩 Service 层逻辑

- 所有数据库操作封装在 Service 层，通过 `@Transactional` 保证原子性。
- 订单保存前计算总价，使用 `BigDecimal.reduce(BigDecimal.ZERO, BigDecimal::add)`。
- 批量检查库存并加行锁：使用 `SELECT ... FOR UPDATE`。
- 使用 `Map<Integer, Product>` 提高循环中数据访问效率。
- 批量更新库存：通过 `CASE WHEN ... THEN ... END + WHERE id IN (...)` 实现。

---

### 🧾 Mapper XML 编写规范

- 参数类型注意区分大小写，例如 `<foreach collection="list">`。
- `<foreach>` 用于构造动态 SQL：批量更新、IN 查询等场景。
- 使用 `@Param` 显式命名参数，避免 MyBatis 无法解析。

---

### 📦 Request / Response 封装

- 所有请求参数封装为 `OrderRequest` 和 `OrderItemRequest` 类，结构清晰。
- 所有返回值统一封装为 `Result<T>` 对象，包含 `code`, `message`, `data` 字段。
- 提供 `Result.success()` 与 `Result.failure(...)` 快捷构造方法。
- 异常统一封装为 `Result.failure(...)`，便于前端处理。

---

### ⚠️ 异常处理机制

- 使用 `@RestControllerAdvice + @ExceptionHandler` 实现全局异常捕获。
- 自定义业务异常 `BusinessException` 携带错误码和消息。
- 所有异常统一转为 `Result<T>` 对象返回客户端。

---

### 📄 日志打印

- 使用 `LoggerFactory.getLogger(...)` 实例化日志对象。
- 日志等级区分：
    - `info`：记录业务成功信息
    - `debug`：记录详细调试信息
- 配置日志等级通过 `application.yml` 或 `application.properties`：

```yaml
logging.level.com.example.ecommerce=debug
```

### ✅ 数据库连接池：Druid 集成说明

本项目使用 Druid 作为数据库连接池，替代默认的 HikariCP，便于后续进行 SQL 监控、性能调优等操作。

访问监控控制台:http://localhost:8080/druid

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-3-starter</artifactId>
    <version>1.2.20</version>
</dependency>
```



## 性能与扩展建议

- 查询库存与下单操作间通过行锁避免并发问题，满足单体架构场景。
- 批量处理库存更新，避免循环多次访问数据库。
- 合理使用 `distinct()`、`collect(Collectors.toMap(...))` 等流式操作，提高代码表达力和执行效率。
- 避免“魔法数字”，建议抽取为常量，例如库存不足错误码。

---

## 项目目前状态

| 模块                 | 完成情况             |
|----------------------|----------------------|
| 基础订单增删查改     | ✅ 完成              |
| 接口测试与调试       | ✅ 完成（Postman）   |
| 库存并发控制         | ✅ 行锁实现          |
| 异常处理机制         | ✅ 全局捕获          |
| 日志打印与调试       | ✅ 支持 INFO 和 DEBUG |
| 统一响应结构         | ✅ 使用 Result<T>    |
| Mapper 动态 SQL      | ✅ 支持批量操作      |

