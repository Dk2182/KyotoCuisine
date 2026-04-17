CREATE DATABASE IF NOT EXISTS kyoto_db;
USE kyoto_db;

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
);

CREATE TABLE customer_profiles (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    loyalty_points INT NOT NULL DEFAULT 0,
    preferred_contact_method VARCHAR(20),
    notes VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE staff_profiles (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    staff_position VARCHAR(50) NOT NULL,
    hire_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE menu_categories (
    menu_category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE menu_items (
    menu_item_id INT AUTO_INCREMENT PRIMARY KEY,
    menu_category_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    is_bestseller BOOLEAN NOT NULL DEFAULT FALSE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (menu_category_id) REFERENCES menu_categories(menu_category_id) ON DELETE RESTRICT
);

CREATE TABLE table_categories (
    table_category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(30) NOT NULL UNIQUE,
    min_capacity INT NOT NULL,
    max_capacity INT NOT NULL
);

CREATE TABLE restaurant_tables (
    table_id INT AUTO_INCREMENT PRIMARY KEY,
    table_category_id INT NOT NULL,
    table_label VARCHAR(20) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (table_category_id) REFERENCES table_categories(table_category_id) ON DELETE RESTRICT
);

CREATE TABLE reservation_statuses (
    reservation_status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    table_id INT NOT NULL,
    reservation_status_id INT NOT NULL,
    reservation_start DATETIME NOT NULL,
    reservation_end DATETIME NOT NULL,
    guest_count INT NOT NULL,
    special_request VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer_profiles(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id) ON DELETE RESTRICT,
    FOREIGN KEY (reservation_status_id) REFERENCES reservation_statuses(reservation_status_id) ON DELETE RESTRICT,
    CONSTRAINT uq_table_time_slot UNIQUE (table_id, reservation_start, reservation_end)
);

CREATE TABLE order_statuses (
    order_status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE order_types (
    order_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    order_status_id INT NOT NULL,
    order_type_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    placed_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    pickup_time DATETIME,
    notes VARCHAR(255),
    FOREIGN KEY (customer_id) REFERENCES customer_profiles(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (order_status_id) REFERENCES order_statuses(order_status_id) ON DELETE RESTRICT,
    FOREIGN KEY (order_type_id) REFERENCES order_types(order_type_id) ON DELETE RESTRICT
);

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    special_instruction VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(menu_item_id) ON DELETE RESTRICT
);

CREATE TABLE payment_methods (
    payment_method_id INT AUTO_INCREMENT PRIMARY KEY,
    method_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE payment_statuses (
    payment_status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method_id INT NOT NULL,
    payment_status_id INT NOT NULL,
    transaction_reference VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL,
    paid_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(payment_method_id) ON DELETE RESTRICT,
    FOREIGN KEY (payment_status_id) REFERENCES payment_statuses(payment_status_id) ON DELETE RESTRICT
);

CREATE TABLE audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action_type VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id INT NOT NULL,
    action_details TEXT,
    logged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_reservations_table_id ON reservations(table_id);
