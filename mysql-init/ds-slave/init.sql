/* ───────────────────────────────────────────────
   ① 创建三大逻辑库：product_db, ds0, ds1
   ─────────────────────────────────────────────── */
CREATE DATABASE IF NOT EXISTS product_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ds0
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ds1
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

/* ───────────────────────────────────────────────
   ② product_db 结构
   ─────────────────────────────────────────────── */
USE product_db;

CREATE TABLE IF NOT EXISTS product (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  description TEXT,
  price       DECIMAL(10,2) NOT NULL,
  stock       INT          NOT NULL DEFAULT 0,
  created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

/* 可选：初始化演示数据 */
TRUNCATE TABLE product;
INSERT INTO product (name, description, price, stock) VALUES
('Apple iPhone 15',   '128GB, Black', 1599.00, 20),
('Samsung Galaxy S23','256GB, White', 1399.00, 15),
('Google Pixel 8',    '128GB, Blue',  1199.00, 10);

/* ───────────────────────────────────────────────
   ③ ds0 分片库表结构（无 user_id 字段）
   ─────────────────────────────────────────────── */
USE ds0;

CREATE TABLE IF NOT EXISTS order_0 (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL, -- ← 分片键
  customer_name VARCHAR(100) NOT NULL,
  customer_email VARCHAR(100),
  total_price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS order_1 LIKE order_0;

CREATE TABLE IF NOT EXISTS order_item_0 (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL, -- ← 同样保留分片键
  order_id   BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity   INT    NOT NULL,
  price      DECIMAL(10,2) NOT NULL
);
CREATE TABLE IF NOT EXISTS order_item_1 LIKE order_item_0;

/* ───────────────────────────────────────────────
   ④ ds1 分片库表结构（同 ds0）
   ─────────────────────────────────────────────── */
USE ds1;
CREATE TABLE IF NOT EXISTS order_0 LIKE ds0.order_0;
CREATE TABLE IF NOT EXISTS order_1 LIKE ds0.order_0;
CREATE TABLE IF NOT EXISTS order_item_0 LIKE ds0.order_item_0;
CREATE TABLE IF NOT EXISTS order_item_1 LIKE ds0.order_item_0;

/* ───────────────────────────────────────────────
   ⑥ 应用访问账号
   ─────────────────────────────────────────────── */
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_pass';
GRANT ALL PRIVILEGES ON product_db.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON ds0.*        TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON ds1.*        TO 'app_user'@'%';

FLUSH PRIVILEGES;

/*
    -- 手动方式将从库连接到主库
    -- 主库执行如下代码
    docker exec -it ecommerce-mysql-ds-master mysql -uroot -proot -e "SHOW MASTER STATUS\G"

    -- 根据上述结果，手动调整 MASTER_LOG_FILE 和 MASTER_LOG_POS
    docker exec -it ecommerce-mysql-ds-slave mysql -uroot -proot

    STOP SLAVE;
    CHANGE MASTER TO
    MASTER_HOST='mysql-ds-master',
    MASTER_USER='replica',
    MASTER_PASSWORD='replica123',
    MASTER_LOG_FILE='mysql-bin.000003',
    MASTER_LOG_POS=197;
    START SLAVE;

    SET GLOBAL super_read_only = ON;

    -- 查看只读情况
    SHOW VARIABLES LIKE '%read_only%';
    -- 查看从库设置情况
    SHOW SLAVE STATUS\G
*/