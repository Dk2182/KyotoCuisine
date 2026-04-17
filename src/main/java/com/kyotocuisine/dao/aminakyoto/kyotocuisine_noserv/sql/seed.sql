USE kyoto_db;

INSERT INTO roles (role_name) VALUES ('CUSTOMER'), ('STAFF'), ('ADMIN');

INSERT INTO users (role_id, first_name, last_name, email, password_hash, phone) VALUES
(1, 'Amina', 'Customer', 'customer@kyoto.com', '123', '111111'),
(2, 'Danijel', 'Staff', 'staff@kyoto.com', '123', '222222'),
(3, 'Mateo', 'Admin', 'admin@kyoto.com', '123', '333333');

INSERT INTO customer_profiles (user_id) VALUES (1);
INSERT INTO staff_profiles (user_id, staff_position, hire_date) VALUES (2, 'Waiter', CURRENT_DATE);

INSERT INTO menu_categories (category_name, description) VALUES
('Sushi', 'Classic and specialty rolls'),
('Drinks', 'Soft drinks and tea'),
('Desserts', 'Sweet finishes');

INSERT INTO menu_items (menu_category_id, item_name, description, price, is_bestseller, is_available) VALUES
(1, 'Dragon Roll', 'Shrimp tempura, avocado, eel sauce', 12.50, TRUE, TRUE),
(1, 'Salmon Nigiri', 'Fresh salmon over seasoned rice', 8.90, FALSE, TRUE),
(3, 'Mochi Ice Cream', 'Soft mochi with vanilla filling', 5.20, FALSE, TRUE);

INSERT INTO table_categories (category_name, min_capacity, max_capacity) VALUES
('SMALL', 1, 2),
('MEDIUM', 3, 4),
('LARGE', 5, 6);

INSERT INTO restaurant_tables (table_category_id, table_label, capacity) VALUES
(2, 'T1', 4),
(2, 'T2', 4),
(3, 'T3', 6);

INSERT INTO reservation_statuses (status_name) VALUES ('PENDING'), ('CONFIRMED'), ('CANCELLED');
INSERT INTO order_statuses (status_name) VALUES ('NOT_PREPARING'), ('PREPARING'), ('READY'), ('COMPLETED');
INSERT INTO order_types (type_name) VALUES ('DINE_IN'), ('TAKEAWAY'), ('DELIVERY');
INSERT INTO payment_methods (method_name) VALUES ('CARD'), ('CASH');
INSERT INTO payment_statuses (status_name) VALUES ('PENDING'), ('PAID'), ('FAILED'), ('REFUNDED');
