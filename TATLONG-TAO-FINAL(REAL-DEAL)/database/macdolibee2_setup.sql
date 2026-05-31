CREATE DATABASE IF NOT EXISTS macdolibee2
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE macdolibee2;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'employee',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS foods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    price INT NOT NULL,
    price_w_vat DECIMAL(10, 2) NULL,
    image_path VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_foods_category (category)
) ENGINE=InnoDB;

SET @foods_price_w_vat_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'foods'
      AND COLUMN_NAME = 'price_w_vat'
);
SET @foods_price_w_vat_sql = IF(
    @foods_price_w_vat_exists = 0,
    'ALTER TABLE foods ADD COLUMN price_w_vat DECIMAL(10, 2) NULL AFTER price',
    'SELECT 1'
);
PREPARE foods_price_w_vat_stmt FROM @foods_price_w_vat_sql;
EXECUTE foods_price_w_vat_stmt;
DEALLOCATE PREPARE foods_price_w_vat_stmt;

SET @foods_image_path_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'foods'
      AND COLUMN_NAME = 'image_path'
);
SET @foods_image_path_sql = IF(
    @foods_image_path_exists = 0,
    'ALTER TABLE foods ADD COLUMN image_path VARCHAR(255) NULL AFTER price',
    'SELECT 1'
);
PREPARE foods_image_path_stmt FROM @foods_image_path_sql;
EXECUTE foods_image_path_stmt;
DEALLOCATE PREPARE foods_image_path_stmt;

CREATE TABLE IF NOT EXISTS ingredients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    cost DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS electricity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS food_ingredients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    food_name VARCHAR(120) NOT NULL,
    ingredient_name VARCHAR(120) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL DEFAULT 1,
    UNIQUE KEY uq_food_ingredient (food_name, ingredient_name),
    CONSTRAINT fk_food_ingredients_food
        FOREIGN KEY (food_name)
        REFERENCES foods(name)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_food_ingredients_ingredient
        FOREIGN KEY (ingredient_name)
        REFERENCES ingredients(name)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    unit_count INT NOT NULL DEFAULT 0,
    sub_total INT NOT NULL DEFAULT 0,
    product_list TEXT NOT NULL,
    barcode VARCHAR(80) NOT NULL,
    order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ON-GOING',
    INDEX idx_orders_order_time (order_time),
    INDEX idx_orders_completed_at (completed_at),
    INDEX idx_orders_status (status),
    INDEX idx_orders_barcode (barcode)
) ENGINE=InnoDB;

SET @orders_status_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'status'
);
SET @orders_status_sql = IF(
    @orders_status_exists = 0,
    'ALTER TABLE orders ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''ON-GOING'' AFTER order_time',
    'SELECT 1'
);
PREPARE orders_status_stmt FROM @orders_status_sql;
EXECUTE orders_status_stmt;
DEALLOCATE PREPARE orders_status_stmt;

SET @orders_status_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_orders_status'
);
SET @orders_status_index_sql = IF(
    @orders_status_index_exists = 0,
    'CREATE INDEX idx_orders_status ON orders (status)',
    'SELECT 1'
);
PREPARE orders_status_index_stmt FROM @orders_status_index_sql;
EXECUTE orders_status_index_stmt;
DEALLOCATE PREPARE orders_status_index_stmt;

UPDATE orders
SET status = 'ON-GOING'
WHERE status IS NULL OR status = '';

SET @orders_completed_at_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'completed_at'
);
SET @orders_completed_at_sql = IF(
    @orders_completed_at_exists = 0,
    'ALTER TABLE orders ADD COLUMN completed_at TIMESTAMP NULL AFTER order_time',
    'SELECT 1'
);
PREPARE orders_completed_at_stmt FROM @orders_completed_at_sql;
EXECUTE orders_completed_at_stmt;
DEALLOCATE PREPARE orders_completed_at_stmt;

SET @orders_completed_at_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_orders_completed_at'
);
SET @orders_completed_at_index_sql = IF(
    @orders_completed_at_index_exists = 0,
    'CREATE INDEX idx_orders_completed_at ON orders (completed_at)',
    'SELECT 1'
);
PREPARE orders_completed_at_index_stmt FROM @orders_completed_at_index_sql;
EXECUTE orders_completed_at_index_stmt;
DEALLOCATE PREPARE orders_completed_at_index_stmt;

UPDATE orders
SET completed_at = order_time
WHERE status = 'COMPLETED'
  AND completed_at IS NULL;

SET @orders_subtotal_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'sub_total'
);
SET @orders_subtotal_sql = IF(
    @orders_subtotal_exists = 0,
    'ALTER TABLE orders ADD COLUMN sub_total INT NOT NULL DEFAULT 0 AFTER unit_count',
    'SELECT 1'
);
PREPARE orders_subtotal_stmt FROM @orders_subtotal_sql;
EXECUTE orders_subtotal_stmt;
DEALLOCATE PREPARE orders_subtotal_stmt;

INSERT IGNORE INTO users (username, password, role) VALUES
    ('admin', 'admin123', 'admin'),
    ('quiamco', 'admin123', 'admin'),
    ('EMPLOYEE', '1234', 'employee');

INSERT IGNORE INTO foods (name, category, price) VALUES
    ('MacdoLibee Classic Burger', 'BURGER', 89),
    ('Double Bee Burger', 'BURGER', 129),
    ('Cheesy Mac Burger', 'BURGER', 109),
    ('Chickenjoy Mac Meal', 'MEAL', 159),
    ('Burger Steak Rice Meal', 'MEAL', 139),
    ('Spaghetti Burger Combo', 'MEAL', 149),
    ('Super Chicken Meal', 'SUPER MEAL', 189),
    ('Super Burger Meal', 'SUPER MEAL', 199),
    ('Kids Happy Burger', 'HAPPY MEAL', 99),
    ('Kids Chicken Meal', 'HAPPY MEAL', 109),
    ('New Crispy Chicken Sandwich', 'NEW', 129),
    ('New Honey Fries', 'NEW', 79),
    ('Regular Fries', 'FRIES', 59),
    ('Large Fries', 'FRIES', 89),
    ('Sundae Cup', 'DESSERT', 49),
    ('Peach Mango Pie', 'DESSERT', 55),
    ('Coke Float', 'DRINKS', 65),
    ('Pineapple Juice', 'DRINKS', 55);

UPDATE foods
SET image_path = CONCAT('src/images/', id, '.png')
WHERE image_path IS NULL OR image_path = '';

UPDATE foods
SET price_w_vat = ROUND(price * 1.12, 2)
WHERE price IS NOT NULL;

INSERT IGNORE INTO ingredients (name, cost) VALUES
    ('Burger Bun', 15.00),
    ('Beef Patty', 45.00),
    ('Chicken Fillet', 52.00),
    ('Rice', 12.00),
    ('Cheese Slice', 10.00),
    ('Fries Potato', 24.00),
    ('Soft Drink Syrup', 18.00),
    ('Ice Cream Mix', 20.00),
    ('Spaghetti Sauce', 22.00),
    ('Pie Filling', 16.00);

INSERT IGNORE INTO electricity (name, price) VALUES
    ('Kitchen Lights', 350.00),
    ('Dining Lights', 280.00),
    ('Freezer Usage', 950.00),
    ('Fryer Usage', 1200.00),
    ('Air Conditioning', 1600.00);

INSERT IGNORE INTO food_ingredients (food_name, ingredient_name, quantity) VALUES
    ('MacdoLibee Classic Burger', 'Burger Bun', 1),
    ('MacdoLibee Classic Burger', 'Beef Patty', 1),
    ('MacdoLibee Classic Burger', 'Cheese Slice', 1),
    ('Double Bee Burger', 'Burger Bun', 1),
    ('Double Bee Burger', 'Beef Patty', 2),
    ('Chickenjoy Mac Meal', 'Chicken Fillet', 1),
    ('Chickenjoy Mac Meal', 'Rice', 1),
    ('Regular Fries', 'Fries Potato', 1),
    ('Sundae Cup', 'Ice Cream Mix', 1),
    ('Peach Mango Pie', 'Pie Filling', 1);
