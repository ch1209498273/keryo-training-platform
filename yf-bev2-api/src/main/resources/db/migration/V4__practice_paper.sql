-- 需求2：培训-固定练习卷（练习卷 / 卷题关联 / 发布对象）
CREATE TABLE IF NOT EXISTS el_practice_paper (
    id VARCHAR(64) NOT NULL COMMENT 'ID',
    title VARCHAR(255) NOT NULL COMMENT '练习卷标题',
    courseware_id VARCHAR(64) DEFAULT NULL COMMENT '关联课件(培训)',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    status INT DEFAULT 0 COMMENT '0=草稿 1=已发布 2=停用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(255) DEFAULT '',
    update_by VARCHAR(255) DEFAULT '',
    PRIMARY KEY (id),
    KEY idx_courseware (courseware_id),
    KEY idx_time (start_time, end_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习卷(培训-固定练习)';

CREATE TABLE IF NOT EXISTS el_practice_paper_qu (
    id VARCHAR(64) NOT NULL COMMENT 'ID',
    paper_id VARCHAR(64) NOT NULL COMMENT '练习卷ID',
    qu_id VARCHAR(64) NOT NULL COMMENT '题目ID',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_qu (qu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习卷题目';

CREATE TABLE IF NOT EXISTS el_practice_paper_target (
    id VARCHAR(64) NOT NULL COMMENT 'ID',
    paper_id VARCHAR(64) NOT NULL COMMENT '练习卷ID',
    target_type VARCHAR(16) NOT NULL COMMENT '发布对象类型: role/dept/user',
    target_id VARCHAR(64) NOT NULL COMMENT '对象ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习卷发布对象';

