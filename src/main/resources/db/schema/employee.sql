CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'primary key',
    name VARCHAR(32) NOT NULL COMMENT 'employee name',
    username VARCHAR(32) NOT NULL UNIQUE COMMENT 'login name',
    password VARCHAR(64) NOT NULL COMMENT 'MD5 password hash',
    phone VARCHAR(11) DEFAULT NULL,
    sex VARCHAR(2) DEFAULT NULL,
    id_number VARCHAR(18) DEFAULT NULL,
    status INT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL
);

INSERT INTO employee (name, username, password, status, create_user, update_user)
SELECT 'Admin', 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE username = 'admin');
