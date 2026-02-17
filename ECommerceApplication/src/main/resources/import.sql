-- Seeding data with conflict handling to avoid duplicates

-- Seed roles
INSERT INTO roles (role_id, role_name) VALUES (101, 'ADMIN') ON CONFLICT (role_id) DO NOTHING;
INSERT INTO roles (role_id, role_name) VALUES (102, 'USER') ON CONFLICT (role_id) DO NOTHING;

-- Seed categories
INSERT INTO categories (category_id, category_name) VALUES (1, 'Electronics') ON CONFLICT (category_id) DO NOTHING;
INSERT INTO categories (category_id, category_name) VALUES (2, 'Clothing') ON CONFLICT (category_id) DO NOTHING;
INSERT INTO categories (category_id, category_name) VALUES (3, 'Books') ON CONFLICT (category_id) DO NOTHING;

-- Seed products
INSERT INTO products (product_name, image, description, quantity, price, discount, special_price, category_id)
VALUES ('Laptop', 'laptop.jpg', 'High-performance laptop', 10, 1500.0, 10.0, 1350.0, 1)
ON CONFLICT (product_name, category_id) DO NOTHING;

INSERT INTO products (product_name, image, description, quantity, price, discount, special_price, category_id)
VALUES ('T-Shirt', 'shirt.jpg', 'Cotton T-Shirt', 50, 20.0, 5.0, 19.0, 2)
ON CONFLICT (product_name, category_id) DO NOTHING;

INSERT INTO products (product_name, image, description, quantity, price, discount, special_price, category_id)
VALUES ('Java Programming', 'book.jpg', 'Learn Java programming', 30, 50.0, 0.0, 50.0, 3)
ON CONFLICT (product_name, category_id) DO NOTHING;