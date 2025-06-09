# Stage 02 - Database Split（应用与数据库分离）

本阶段目标：将应用与数据库解耦，并通过 Docker 实现本地容器化部署，支持多环境配置和可控的数据库初始化。

---

## 目录结构调整

- `src/main/resources`
  - `application.yml`：主配置入口，声明默认配置文件加载行为
  - `application-dev.yml`：开发环境配置（本机运行）
  - `application-prod.yml`：生产配置（容器内运行）

---

## 多环境配置说明

```yaml
# application.yml（默认激活 dev 环境）
spring:
  profiles:
    active: dev
```

```yaml
# application-dev.yml（开发环境）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce
    username: root
    password: your-password
  jpa:
    show-sql: true
logging:
  level:
    com.example.ecommerce: DEBUG
```

```yaml
# application-prod.yml（生产/容器环境）
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

---

## Docker 部署相关

### Dockerfile 示例

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . /app
RUN ./mvnw clean package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/ecommerce-0.0.1-SNAPSHOT.jar"]
```

### docker-compose.yml 示例

```yaml
version: "3.8"
services:
  db:
    image: mysql:8
    container_name: mysql-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ecommerce
      MYSQL_USER: user
      MYSQL_PASSWORD: password
    ports:
      - "3307:3306"
    volumes:
      - db-data:/var/lib/mysql

  app:
    build: .
    container_name: ecommerce-app
    depends_on:
      - db
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/ecommerce?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: password

volumes:
  db-data:
```

---

## 数据库初始化说明

- `schema.sql`：数据库结构文件
- `data.sql`：初始化数据文件
- 两者均放置于根目录下的 `mysql-init` 目录中
- **容器初始化仅执行一次**，如需重新执行需：

```bash
docker compose down -v   # 删除容器及数据卷
docker compose build     # 重新构建镜像
docker compose up        # 启动容器
```

---

## 部署注意事项

- 如仅修改代码逻辑，可跳过 `build` 步骤，直接运行：
  ```bash
  docker compose up
  ```
- 如果 Docker 容器内数据库不希望暴露给宿主机，删除 `db` 服务中的 `ports` 映射即可
- 可使用 IDE 连接本地数据库测试，也可访问容器中数据库（通过端口如 3307）

---

## 第二阶段成果总结

- Spring Boot 项目支持 dev/prod 多环境切换
- 应用与数据库以 Docker 容器独立部署
- 数据库初始化通过 schema.sql 和 data.sql 控制
- 宿主机可以通过端口访问应用和数据库
- 支持热部署与快速重启，不影响数据持久化
