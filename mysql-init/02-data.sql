
-- ========================
-- Insert test products
-- ========================

TRUNCATE table product;

INSERT INTO product (name, description, price, stock) VALUES
('Apple iPhone 15', '128GB, Black', 1599.00, 20),
('Samsung Galaxy S23', '256GB, White', 1399.00, 15),
('Google Pixel 8', '128GB, Blue', 1199.00, 10);
