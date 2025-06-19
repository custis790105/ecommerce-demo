-- ========================
-- Drop Tables if exist
-- ========================
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS product;

-- ========================
-- Create product table
-- ========================
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- Create order table
-- ========================
CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100),
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- Create order_item table
-- ========================
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- ========================
-- Insert test products
-- ========================

TRUNCATE table product;

INSERT INTO product (name, description, price, stock) VALUES
('Apple iPhone 15', '128GB, Black', 1599.00, 20),
('Samsung Galaxy S23', '256GB, White', 1399.00, 15),
('Google Pixel 8', '128GB, Blue', 1199.00, 10);



/*
    -- 手动方式将从库连接到主库
    -- 主库执行如下代码
    docker exec -it ecommerce-mysql-master mysql -uroot -proot -e "SHOW MASTER STATUS\G"

    -- 根据上述结果，手动调整 MASTER_LOG_FILE 和 MASTER_LOG_POS
    docker exec -it ecommerce-mysql-slave mysql -uroot -proot

    STOP SLAVE;
    CHANGE MASTER TO
    MASTER_HOST='mysql-master',
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