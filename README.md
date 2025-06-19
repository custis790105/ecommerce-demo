# 第五阶段：MySQL 主从复制总结（stage-05-db-readwrite-split）

## 一、目标

本阶段目标是通过 MySQL 主从配置，实现数据库的读写分离，为后续分库分表打下基础。

---

## 二、实现方案

### 1. 架构简述

- 使用 Docker Compose 启动 `mysql-master` 和 `mysql-slave`
- `app1` 连接主库，处理写请求
- `app2` 连接从库，处理读请求
- 从库通过 `CHANGE MASTER TO` 命令手动配置复制

---

## 三、配置细节

### 1. 初始化同步的一致性问题

- 问题：主库通过 `SHOW MASTER STATUS` 查看 binlog 和 pos，但这代表当前状态，若从库直接用它设置主从，可能会错过部分数据。
- 正确做法：主从库在执行 `CHANGE MASTER TO` 之前，必须手动执行相同的 `init.sql` 脚本，确保起始数据一致。

### 2. binlog 起始位置不能从0开始？

- MySQL 启动后会生成默认 binlog 文件（如 `mysql-bin.000001`），再执行任何操作都会更新 pos。
- 即使 `down -v` 后重启，也可能因为初始内部操作（建库建表等）而使 pos 不为 0。
- 想从最开始记录变化，可以删除 volume 并在 `init.sql` 中控制顺序，提前 `RESET MASTER`。

---

## 四、只读配置问题

### 1. 为什么 `SET GLOBAL super_read_only = ON` 无效？

- 原因：`docker-entrypoint-initdb.d/*.sql` 脚本执行在 MySQL 启动初始化阶段，设置完只读后，脚本自身也没法执行写操作（如创建表）。
- 所以不能直接在 init.sql 中设置 `super_read_only = ON`。

### 2. 如何正确设置只读？

- 推荐手动进入容器执行：
  ```sql
  SET GLOBAL read_only = ON;
  SET GLOBAL super_read_only = ON;
  ```
- `super_read_only` 可以限制 `root` 账号写操作。

---

## 五、MyBatis 无法连接数据库的问题

### 问题排查过程

- 错误信息中提示 `UnknownHostException: db`
- 最终原因：应用容器的配置中写错了数据库主机名，写成了 `db`，但 compose 中并没有名为 `db` 的服务。

### 正确方式

- `app1` 使用 `mysql-master` 主机名：
  ```yaml
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql-master:3306/ecommerce
  ```

- `app2` 使用 `mysql-slave` 主机名：
  ```yaml
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql-slave:3306/ecommerce
  ```

---

## 六、其他注意事项

- 应用中如果使用 `Druid`，驱动类名 `driver-class-name` 通常可省略，Spring Boot 会自动推断。
- 主库应确保开启 binlog，并创建 `replica` 用户授权：
  ```sql
  CREATE USER 'replica'@'%' IDENTIFIED BY 'yourpassword';
  GRANT REPLICATION SLAVE ON *.* TO 'replica'@'%';
  ```

---

## 七、本阶段完成内容

- ✅ 主从数据库通过 Docker Compose 部署成功
- ✅ 数据初始一致性控制
- ✅ 主库开启 binlog，从库连接并同步
- ✅ 从库设置只读，防止写入
- ✅ 应用读写分离部署（app1 写，app2 读）
- ✅ 整体流程手动验证通过

---

