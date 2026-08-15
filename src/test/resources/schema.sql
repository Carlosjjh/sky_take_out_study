CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    phone VARCHAR(11),
    sex VARCHAR(2),
    id_number VARCHAR(18),
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type INT NOT NULL,
    name VARCHAR(32) NOT NULL UNIQUE,
    sort INT DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE IF NOT EXISTS dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    category_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(512),
    description VARCHAR(255),
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE IF NOT EXISTS customer_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(32) NOT NULL,
    phone VARCHAR(11) NOT NULL,
    address VARCHAR(128) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status INT NOT NULL,
    order_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    number INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL
);

MERGE INTO employee (id, name, username, password, status, create_time, update_time, create_user, update_user)
KEY(id)
VALUES (1, 'Admin', 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);
