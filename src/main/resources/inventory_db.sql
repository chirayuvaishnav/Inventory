-- ========================================================
-- Inventory Management System (IMS) Database Schema & Seeds
-- ========================================================

CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- 1. Users Table (Authentication & RBAC)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'cashier') NOT NULL
);

-- 2. Products Table (Inventory Catalog)
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    min_required INT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
);

-- 3. Bills Table (Checkout Sales Orders)
CREATE TABLE IF NOT EXISTS bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DOUBLE NOT NULL
);

-- 4. Bill Items Table (Order Line Items)
CREATE TABLE IF NOT EXISTS bill_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Seed Default Users
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'admin'),
('cashier', 'cashier123', 'cashier'),
('cashier1', 'cash123', 'cashier')
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- Seed Sample Products
INSERT INTO products (name, price, quantity, min_required, is_active) VALUES
('Maggi 2-Minute Noodles', 24.00, 35, 10, 1),
('Potato Chips 150g', 30.00, 18, 5, 1),
('Coca Cola 500ml', 40.00, 60, 15, 1),
('Organic Honey 500g', 250.00, 22, 5, 1),
('Dairy Milk Silk', 85.00, 40, 10, 1),
('Tata Tea Gold 500g', 320.00, 15, 5, 1),
('Sugar 1kg', 48.00, 2, 5, 1)
ON DUPLICATE KEY UPDATE price = VALUES(price);