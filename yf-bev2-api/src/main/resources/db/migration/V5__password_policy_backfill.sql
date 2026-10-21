-- Existing accounts are grandfathered in: their current password is considered
-- an already-established password and is not forced through first-login change.
-- MySQL 8.4 does not support ADD COLUMN IF NOT EXISTS. Use a metadata check so
-- this migration remains safe to run again after a partial execution.
SET @last_password_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'el_sys_user'
      AND COLUMN_NAME = 'last_password_time'
);
SET @add_last_password_time_sql = IF(
    @last_password_time_exists = 0,
    'ALTER TABLE el_sys_user ADD COLUMN last_password_time DATETIME DEFAULT NULL',
    'SELECT 1'
);
PREPARE add_last_password_time FROM @add_last_password_time_sql;
EXECUTE add_last_password_time;
DEALLOCATE PREPARE add_last_password_time;

CREATE TABLE IF NOT EXISTS el_sys_user_password_history (
    id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    password VARCHAR(255) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

UPDATE el_sys_user
SET last_password_time = COALESCE(last_password_time, update_time, create_time, NOW())
WHERE last_password_time IS NULL;

INSERT INTO el_sys_user_password_history (id, user_id, password, create_time)
SELECT MD5(CONCAT('legacy-password-', u.id, '-', u.password)),
       u.id,
       u.password,
       COALESCE(u.last_password_time, u.update_time, u.create_time, NOW())
FROM el_sys_user u
WHERE u.password IS NOT NULL
  AND u.password <> ''
    AND NOT EXISTS (
      SELECT 1
      FROM el_sys_user_password_history h
      WHERE BINARY h.user_id = BINARY u.id
        AND BINARY h.password = BINARY u.password
  );

