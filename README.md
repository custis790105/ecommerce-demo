# 第四阶段：Nginx 负载均衡（stage-04-nginx-lb）

本阶段目标是模拟多实例部署，并通过 Nginx 实现请求的负载均衡。

## 实现内容

### 1. 多实例部署

- 使用 Docker Compose 启动两个 Spring Boot 应用实例：`app1` 和 `app2`
- 宿主机分别映射端口 8081 和 8082，容器内部端口仍为 8080
- 日志中可看到轮询效果，也在接口返回中增加了实例名（hostname）

### 2. Nginx 配置

- 使用稳定版镜像 `nginx:1.24.0`，避免使用 latest 带来的不确定性
- nginx.conf 配置了 upstream 组：

  ```nginx
  upstream ecommerce_backend {
      server app1:8080;
      server app2:8080;
  }
  ```

- location 路由设置：

  ```nginx
  location / {
      proxy_pass http://ecommerce_backend;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
  }

  location /druid {
      proxy_pass http://ecommerce_backend;
  }
  ```

### 3. nginx.conf 的加载方式

- 最终选择通过 Dockerfile 拷贝配置文件：

  ```dockerfile
  COPY nginx.conf /etc/nginx/nginx.conf
  ```

- 原因是开发阶段没法直接运行 Nginx，只能通过 Docker 发布，挂载方式依赖本地路径不太适合此场景
- 挂载适合开发测试，拷贝适合生产部署

## 疑问记录

### 为什么 nginx.conf 中 upstream 使用的是 app1:8080 而不是宿主机的 8081？

因为容器内部通信通过容器名进行 DNS 解析，app1 和 app2 是在同一个 Docker 网络中，可以直接访问容器内部端口 8080。

### nginx.conf 中为什么要单独配置 /druid 路径？

location 匹配是精确的，如果没有显式配置 `/druid`，访问 `/druid/index.html` 会返回 404。

### nginx.conf 已配置了 upstream，为何 docker-compose.yml 还需要写 depends_on？

docker-compose 的 `depends_on` 只是确保容器启动顺序，不等同于健康检查。upstream 是 Nginx 配置的一部分，跟 compose 启动顺序无关，但为了避免 Nginx 报错，建议在 nginx 服务中写上依赖项。

### 为什么请求日志没有打印出来？

一开始是因为没有注册拦截器，或者容器中没有 HOSTNAME 环境变量。通过在拦截器中读取环境变量 `System.getenv("HOSTNAME")` 实现。

### hostname 显示的是随机字符串？

容器默认会使用自动生成的 ID 作为 hostname，显示的是该 ID 的前缀。

### 日志在哪里查看？

可以使用命令查看容器日志：

```bash
docker logs -f ecommerce-app-1
```

其中 `-f` 表示持续输出（follow 模式）。

### Nginx 转发请求时如何区分实例？

每个请求经过 Nginx，会按照轮询策略转发到 app1 或 app2。可以通过接口返回中包含 `hostname` 来判断请求被哪个实例处理。

### 每次构建是否都需要 docker compose down？

是的，修改了构建参数或配置文件后，为了避免使用旧缓存，推荐执行：

```bash
docker compose down
docker compose build
docker compose up -d
```

### 为什么我访问 localhost/products/1 报错？

是因为 Redis 连接失败，最终定位是 Redis 地址写死为 localhost，容器内部无法访问。解决方法：

```yaml
spring:
  data:
    redis:
      host: ${SPRING_REDIS_HOST:localhost}
      port: ${SPRING_REDIS_PORT:6379}
```

并在 `docker-compose.yml` 中通过 environment 设置对应的变量，或者直接将 `host` 改为 `ecommerce-redis`。
