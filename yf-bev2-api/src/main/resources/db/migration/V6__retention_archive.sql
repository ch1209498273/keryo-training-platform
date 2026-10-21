SET NAMES utf8mb4;

-- BK-16: non-destructive retention archives. Source rows remain available.
CREATE TABLE IF NOT EXISTS el_keryo_audit_log_archive (
  id varchar(64) NOT NULL,
  user_id varchar(64) DEFAULT NULL,
  user_name varchar(64) DEFAULT NULL,
  real_name varchar(64) DEFAULT NULL,
  module varchar(64) DEFAULT NULL,
  operation varchar(200) DEFAULT NULL,
  method varchar(255) DEFAULT NULL,
  url varchar(500) DEFAULT NULL,
  ip varchar(64) DEFAULT NULL,
  params varchar(2000) DEFAULT NULL,
  result varchar(16) DEFAULT NULL,
  error_msg varchar(1000) DEFAULT NULL,
  cost_time bigint DEFAULT NULL,
  create_time datetime NOT NULL,
  archived_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_audit_archive_time (create_time), KEY idx_audit_archive_at (archived_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS el_exam_record_archive (
  id varchar(64) NOT NULL,
  user_id varchar(64) NOT NULL,
  exam_id varchar(64) NOT NULL,
  paper_id varchar(255) DEFAULT NULL,
  try_count int DEFAULT 0,
  max_score decimal(10,2) DEFAULT NULL,
  last_score decimal(10,2) DEFAULT NULL,
  passed tinyint NOT NULL DEFAULT 0,
  locked tinyint NOT NULL DEFAULT 0,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  archived_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_exam_record_archive_time (create_time), KEY idx_exam_record_archive_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS el_paper_archive (
  id varchar(64) NOT NULL,
  user_id varchar(64) NOT NULL,
  exam_id varchar(64) NOT NULL,
  title varchar(500) NOT NULL,
  total_time int DEFAULT NULL,
  user_time int DEFAULT NULL,
  total_score decimal(10,2) DEFAULT NULL,
  qualify_score decimal(10,2) DEFAULT NULL,
  user_score decimal(10,2) DEFAULT NULL,
  limit_time datetime NOT NULL,
  hand_time datetime DEFAULT NULL,
  hand_state int DEFAULT NULL,
  exam_ip varchar(64) DEFAULT NULL,
  session_secret varchar(128) DEFAULT NULL,
  last_answer_time datetime DEFAULT NULL,
  passed tinyint NOT NULL DEFAULT 0,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_by varchar(255) DEFAULT NULL,
  update_by varchar(255) DEFAULT NULL,
  archived_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_paper_archive_time (create_time), KEY idx_paper_archive_user (user_id), KEY idx_paper_archive_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS el_paper_qu_archive (
  id varchar(64) NOT NULL,
  paper_id varchar(64) NOT NULL,
  qu_id varchar(64) NOT NULL,
  qu_type varchar(32) DEFAULT NULL,
  answered int DEFAULT 0,
  mark int DEFAULT 0,
  sort int DEFAULT 0,
  score decimal(10,2) DEFAULT NULL,
  actual_score decimal(10,2) DEFAULT NULL,
  answer_text text,
  answer_hash varchar(128) DEFAULT NULL,
  is_right tinyint NOT NULL DEFAULT 0,
  archived_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_paper_qu_archive_paper (paper_id), KEY idx_paper_qu_archive_qu (qu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS el_paper_qu_answer_archive (
  id varchar(64) NOT NULL,
  paper_id varchar(64) NOT NULL,
  answer_id varchar(32) DEFAULT NULL,
  qu_id varchar(32) NOT NULL,
  is_right tinyint NOT NULL DEFAULT 0,
  answer text,
  checked int DEFAULT 0,
  sort int DEFAULT 0,
  abc varchar(16) DEFAULT NULL,
  archived_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_paper_answer_archive_paper (paper_id), KEY idx_paper_answer_archive_qu (qu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS el_exam_cheat_log_archive (
  id varchar(64) NOT NULL,
  paper_id varchar(64) NOT NULL,
  exam_id varchar(64) DEFAULT NULL,
  user_id varchar(64) DEFAULT NULL,
  qu_id varchar(64) DEFAULT NULL,
  cheat_type varchar(64) DEFAULT NULL,
  content varchar(500) DEFAULT NULL,
  ip varchar(64) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  archived_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_cheat_archive_paper (paper_id), KEY idx_cheat_archive_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

