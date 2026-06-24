-- Enterprise MySQL Schema for E-Resource Marketplace (v2.0)

CREATE DATABASE IF NOT EXISTS e_resource_enterprise;
USE e_resource_solution;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users & RBAC
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email_id VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'shop_owner', 'worker', 'customer') DEFAULT 'customer',
    refresh_token TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Shops (Marketplace)
CREATE TABLE shops (
    shop_id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    contact_no VARCHAR(50),
    working_hours VARCHAR(100),
    logo_url VARCHAR(255),
    verified_status BOOLEAN DEFAULT FALSE,
    avg_rating FLOAT DEFAULT 0,
    FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Tools Catalog
CREATE TABLE tools (
    tool_id INT AUTO_INCREMENT PRIMARY KEY,
    shop_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    price_hr DECIMAL(10, 2),
    price_day DECIMAL(10, 2),
    price_week DECIMAL(10, 2),
    security_deposit DECIMAL(10, 2),
    available_qty INT DEFAULT 1,
    condition_status ENUM('new', 'excellent', 'good', 'fair') DEFAULT 'good',
    FOREIGN KEY (shop_id) REFERENCES shops(shop_id) ON DELETE CASCADE
);

-- 4. Workers Portfolio
CREATE TABLE workers (
    worker_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    profile_photo VARCHAR(255),
    id_proof_url VARCHAR(255),
    skills TEXT,
    experience_years INT,
    availability_status BOOLEAN DEFAULT TRUE,
    current_lat DOUBLE,
    current_lng DOUBLE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Bookings & Rentals
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    worker_id INT NOT NULL,
    tool_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    status ENUM('pending', 'accepted', 'rejected', 'rented', 'returned', 'completed') DEFAULT 'pending',
    total_amount DECIMAL(10, 2),
    payment_status ENUM('pending', 'success', 'refunded') DEFAULT 'pending',
    tracking_id VARCHAR(100),
    FOREIGN KEY (worker_id) REFERENCES workers(worker_id),
    FOREIGN KEY (tool_id) REFERENCES tools(tool_id)
);

-- 6. Real-time Chat
CREATE TABLE conversations (
    conv_id INT AUTO_INCREMENT PRIMARY KEY,
    participant_a INT NOT NULL,
    participant_b INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    conv_id INT NOT NULL,
    sender_id INT NOT NULL,
    message TEXT,
    image_url VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conv_id) REFERENCES conversations(conv_id)
);

-- 7. Reviews & Ratings
CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    target_id INT NOT NULL, -- shop_id or worker_id
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- 8. Audit Logs
CREATE TABLE audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(255),
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SET FOREIGN_KEY_CHECKS = 1;
