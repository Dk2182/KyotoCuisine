-- Kyoto Cuisine Database Schema
-- All 15 tables as defined in Milestone 2 document

CREATE TABLE IF NOT EXISTS roles (
    role_id INT NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (role_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT,
    role_id INT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS customer_profiles (
    customer_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    loyalty_points INT NOT NULL DEFAULT 0,
    preferred_contact_method VARCHAR(20),
    notes VARCHAR(255),
    PRIMARY KEY (customer_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS staff_profiles (
    staff_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    staff_position VARCHAR(50) NOT NULL,
    hire_date DATE NOT NULL,
    PRIMARY KEY (staff_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS table_categories (
    table_category_id INT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(30) NOT NULL UNIQUE,
    min_capacity INT NOT NULL CHECK (min_capacity > 0),
    max_capacity INT NOT NULL,
    PRIMARY KEY (table_category_id)
);

CREATE TABLE IF NOT EXISTS restaurant_tables (
    table_id INT NOT NULL AUTO_INCREMENT,
    table_category_id INT NOT NULL,
    table_label VARCHAR(20) NOT NULL UNIQUE,
    capacity INT NOT NULL CHECK (capacity > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (table_id),
    FOREIGN KEY (table_category_id) REFERENCES table_categories(table_category_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS reservation_statuses (
    reservation_status_id INT NOT NULL AUTO_INCREMENT,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (reservation_status_id)
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    table_id INT NOT NULL,
    reservation_status_id INT NOT NULL,
    reservation_start DATETIME NOT NULL,
    reservation_end DATETIME NOT NULL,
    guest_count INT NOT NULL CHECK (guest_count > 0),
    special_request VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (reservation_id),
    FOREIGN KEY (customer_id) REFERENCES customer_profiles(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id) ON DELETE RESTRICT,
    FOREIGN KEY (reservation_status_id) REFERENCES reservation_statuses(reservation_status_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS menu_categories (
    menu_category_id INT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    PRIMARY KEY (menu_category_id)
);

CREATE TABLE IF NOT EXISTS menu_items (
    menu_item_id INT NOT NULL AUTO_INCREMENT,
    menu_category_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    image_url VARCHAR(255),
    is_bestseller BOOLEAN NOT NULL DEFAULT FALSE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (menu_item_id),
    FOREIGN KEY (menu_category_id) REFERENCES menu_categories(menu_category_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS order_statuses (
    order_status_id INT NOT NULL AUTO_INCREMENT,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    display_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (order_status_id)
);

CREATE TABLE IF NOT EXISTS order_types (
    order_type_id INT NOT NULL AUTO_INCREMENT,
    type_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (order_type_id)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT NOT NULL AUTO_INCREMENT,
    customer_id INT,
    order_status_id INT NOT NULL,
    order_type_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    placed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pickup_time DATETIME,
    notes VARCHAR(255),
    PRIMARY KEY (order_id),
    FOREIGN KEY (customer_id) REFERENCES customer_profiles(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (order_status_id) REFERENCES order_statuses(order_status_id) ON DELETE RESTRICT,
    FOREIGN KEY (order_type_id) REFERENCES order_types(order_type_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    line_total DECIMAL(10,2) NOT NULL CHECK (line_total >= 0),
    special_instruction VARCHAR(255),
    PRIMARY KEY (order_item_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(menu_item_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS payment_methods (
    payment_method_id INT NOT NULL AUTO_INCREMENT,
    method_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (payment_method_id)
);

CREATE TABLE IF NOT EXISTS payment_statuses (
    payment_status_id INT NOT NULL AUTO_INCREMENT,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (payment_status_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    payment_method_id INT NOT NULL,
    payment_status_id INT NOT NULL,
    transaction_reference VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    paid_at DATETIME,
    PRIMARY KEY (payment_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(payment_method_id) ON DELETE RESTRICT,
    FOREIGN KEY (payment_status_id) REFERENCES payment_statuses(payment_status_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT NOT NULL AUTO_INCREMENT,
    user_id INT,
    action_type VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id INT NOT NULL,
    action_details TEXT,
    logged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);
