-- Seed data for Kyoto Cuisine

-- Roles
INSERT IGNORE INTO roles (role_id, role_name) VALUES (1, 'CUSTOMER'), (2, 'STAFF'), (3, 'ADMIN');

-- Order Statuses (with display_order for enforcing transitions)
INSERT IGNORE INTO order_statuses (order_status_id, status_name, display_order) VALUES
    (1, 'NOT_PREPARING', 1),
    (2, 'PREPARING', 2),
    (3, 'READY', 3),
    (4, 'COMPLETED', 4);

-- Reservation Statuses
INSERT IGNORE INTO reservation_statuses (reservation_status_id, status_name) VALUES
    (1, 'PENDING'),
    (2, 'CONFIRMED'),
    (3, 'CANCELLED'),
    (4, 'COMPLETED');

-- Table Categories
INSERT IGNORE INTO table_categories (table_category_id, category_name, min_capacity, max_capacity) VALUES
    (1, 'SMALL', 1, 2),
    (2, 'MEDIUM', 3, 4),
    (3, 'LARGE', 5, 8);

-- Restaurant Tables
INSERT IGNORE INTO restaurant_tables (table_id, table_category_id, table_label, capacity, is_active) VALUES
    (1, 1, 'T1', 2, TRUE),
    (2, 1, 'T2', 2, TRUE),
    (3, 2, 'T3', 4, TRUE),
    (4, 2, 'T4', 4, TRUE),
    (5, 2, 'T5', 4, TRUE),
    (6, 3, 'T6', 6, TRUE),
    (7, 3, 'T7', 8, TRUE);

-- Menu Categories
INSERT IGNORE INTO menu_categories (menu_category_id, category_name, description) VALUES
    (1, 'Sushi', 'Fresh sushi and sashimi selections'),
    (2, 'Drinks', 'Traditional Japanese beverages'),
    (3, 'Desserts', 'Sweet Japanese treats');

-- Menu Items (matching existing frontend data)
INSERT IGNORE INTO menu_items (menu_item_id, menu_category_id, item_name, description, price, image_url, is_bestseller, is_available) VALUES
    (1, 1, 'Salmon Nigiri', 'Fresh Norwegian salmon on seasoned rice, finished with a delicate touch.', 8.99, '/assets/salmon_nigiri.jpg', TRUE, TRUE),
    (2, 1, 'Dragon Roll', 'Shrimp tempura, avocado, topped with eel and a special house drizzle.', 16.99, '/assets/dragon_roll.jpg', TRUE, TRUE),
    (3, 1, 'Tuna Sashimi', 'Premium bluefin tuna, thinly sliced with soy sauce and wasabi.', 18.99, '/assets/tuna_sashimi.jpg', FALSE, TRUE),
    (4, 1, 'California Roll', 'Classic roll with crab, avocado, and cucumber, perfectly balanced.', 12.99, '/assets/california_roll.jpg', FALSE, TRUE),
    (5, 2, 'Sake', 'Traditional Japanese rice wine, served warm or chilled.', 9.99, '/assets/sake.jpg', FALSE, TRUE),
    (6, 2, 'Green Tea', 'Premium matcha green tea from Kyoto region.', 4.99, '/assets/greentea.jpg', TRUE, TRUE),
    (7, 3, 'Mochi Ice Cream', 'Soft rice cake filled with green tea ice cream.', 7.99, '/assets/icecream.jpg', FALSE, TRUE),
    (8, 3, 'Dorayaki', 'Sweet red bean pancake sandwich, lightly sweetened.', 6.99, '/assets/dorayaki.jpg', FALSE, TRUE);

-- Default Admin account (password: admin123)
INSERT IGNORE INTO users (user_id, role_id, first_name, last_name, email, password_hash, is_active) VALUES
    (1, 3, 'Admin', 'Kyoto', 'admin@kyotocuisine.com', '$2a$10$AUN956ZlBExGC7B80i.D7eQFyo4jaZc2IMdmhvHSBLZH48f1QpYQm', TRUE);

-- Default Staff account (password: staff123)
INSERT IGNORE INTO users (user_id, role_id, first_name, last_name, email, password_hash, is_active) VALUES
    (2, 2, 'Staff', 'Member', 'staff@kyotocuisine.com', '$2a$10$623rOvPFgofvYb0.mOz7yOapc3dQL5FYSnO4eAvMiVuCF/gYpl0Vy', TRUE);

INSERT IGNORE INTO staff_profiles (staff_id, user_id, staff_position, hire_date) VALUES
    (1, 2, 'Waiter', '2025-09-01');

-- Ensure the seeded passwords match documented credentials, even if rows already exist
UPDATE users SET password_hash = '$2a$10$AUN956ZlBExGC7B80i.D7eQFyo4jaZc2IMdmhvHSBLZH48f1QpYQm' WHERE email = 'admin@kyotocuisine.com';
UPDATE users SET password_hash = '$2a$10$623rOvPFgofvYb0.mOz7yOapc3dQL5FYSnO4eAvMiVuCF/gYpl0Vy' WHERE email = 'staff@kyotocuisine.com';
