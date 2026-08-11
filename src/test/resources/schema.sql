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

MERGE INTO employee (id, name, username, password, status, create_time, update_time, create_user, update_user)
KEY(id)
VALUES (1, 'Admin', 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);
