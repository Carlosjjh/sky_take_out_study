CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type INT NOT NULL COMMENT '1 dish category, 2 set-meal category',
    name VARCHAR(32) NOT NULL UNIQUE COMMENT 'category name',
    sort INT DEFAULT 0 COMMENT 'display order',
    status INT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL
);
