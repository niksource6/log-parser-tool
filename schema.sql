CREATE DATABASE IF NOT EXISTS log_parser_tool CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE log_parser_tool;

CREATE TABLE IF NOT EXISTS log_entries (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    log_time DATETIME NOT NULL,
    level VARCHAR(5) NOT NULL,
    interface_name VARCHAR(255) NOT NULL,
    request_cost_ms BIGINT NULL,
    exception_message TEXT NULL,
    INDEX idx_level_time (level, log_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
