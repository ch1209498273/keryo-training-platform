CREATE TABLE IF NOT EXISTS el_keryo_courseware (
    id VARCHAR(32) NOT NULL COMMENT 'ID',
    title VARCHAR(255) NOT NULL COMMENT '课件标题',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL(上传后返回)',
    file_type VARCHAR(10) NOT NULL COMMENT 'PPTX/DOCX/PDF',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    dept_code VARCHAR(32) DEFAULT NULL COMMENT '所属中心(空=全部可见)',
    status INT DEFAULT 0 COMMENT '0=草稿 1=已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(255) DEFAULT '',
    update_by VARCHAR(255) DEFAULT '',
    PRIMARY KEY (id),
    KEY idx_dept (dept_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课件';

