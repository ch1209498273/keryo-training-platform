CREATE TABLE IF NOT EXISTS el_sys_user_password_history (
    id VARCHAR(32) NOT NULL COMMENT 'ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    password VARCHAR(255) NOT NULL COMMENT '密码hash',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='密码历史';

-- MySQL 8.4 does not support ADD COLUMN IF NOT EXISTS.
SET @last_password_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'el_sys_user'
      AND COLUMN_NAME = 'last_password_time'
);
SET @add_last_password_time_sql = IF(
    @last_password_time_exists = 0,
    'ALTER TABLE el_sys_user ADD COLUMN last_password_time DATETIME DEFAULT NULL COMMENT ''最后改密时间''',
    'SELECT 1'
);
PREPARE add_last_password_time FROM @add_last_password_time_sql;
EXECUTE add_last_password_time;
DEALLOCATE PREPARE add_last_password_time;

