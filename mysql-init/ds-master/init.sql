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
  user_id BIGINT NOT NULL,  -- ← 分片键
  customer_name VARCHAR(100) NOT NULL,
  customer_email VARCHAR(100),
  total_price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
);
CREATE TABLE IF NOT EXISTS order_1 LIKE order_0;

CREATE TABLE IF NOT EXISTS order_item_0 (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL, -- ← 同样保留分片键
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL
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
   ⑤ 复制账号（主从同步专用）
   ─────────────────────────────────────────────── */
CREATE USER IF NOT EXISTS 'replica'@'%' IDENTIFIED BY 'replica123';
GRANT REPLICATION SLAVE ON *.* TO 'replica'@'%';

/* ───────────────────────────────────────────────
   ⑥ 应用访问账号
   ─────────────────────────────────────────────── */
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_pass';
GRANT ALL PRIVILEGES ON product_db.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON ds0.*        TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON ds1.*        TO 'app_user'@'%';

FLUSH PRIVILEGES;
