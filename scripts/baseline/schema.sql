SET NAMES utf8mb4;

ALTER TABLE el_sys_user ADD COLUMN area_code varchar(32) DEFAULT NULL AFTER dept_code;
ALTER TABLE el_sys_user ADD COLUMN last_password_time datetime DEFAULT NULL AFTER area_code;
ALTER TABLE el_sys_depart ADD COLUMN area_code varchar(32) DEFAULT NULL AFTER dept_code;
ALTER TABLE el_exam ADD COLUMN score_visible int NOT NULL DEFAULT 1 AFTER chance;

CREATE TABLE el_sys_user_password_history (
  id varchar(32) NOT NULL,
  user_id varchar(32) NOT NULL,
  password varchar(255) NOT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_keryo_training (
  id varchar(32) NOT NULL,
  title varchar(255) NOT NULL,
  file_url varchar(500) NOT NULL,
  file_type varchar(10) NOT NULL,
  file_size bigint DEFAULT 0,
  dept_code varchar(32) DEFAULT NULL,
  status int DEFAULT 0,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by varchar(255) DEFAULT '',
  update_by varchar(255) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_training_dept (dept_code),
  KEY idx_training_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_keryo_training_plan (
  id varchar(64) NOT NULL,
  title varchar(200) NOT NULL,
  content varchar(2000) DEFAULT NULL,
  credit decimal(10,2) NOT NULL DEFAULT 0.00,
  exam_id varchar(64) DEFAULT NULL,
  courseware_id varchar(32) DEFAULT NULL,
  dept_code varchar(64) DEFAULT NULL,
  status varchar(16) NOT NULL DEFAULT 'DRAFT',
  start_time datetime DEFAULT NULL,
  end_time datetime DEFAULT NULL,
  submit_by varchar(64) DEFAULT NULL,
  submit_time datetime DEFAULT NULL,
  approve_by varchar(64) DEFAULT NULL,
  approve_time datetime DEFAULT NULL,
  approve_remark varchar(500) DEFAULT NULL,
  create_by varchar(64) DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(64) DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_plan_exam (exam_id),
  KEY idx_plan_training (courseware_id),
  KEY idx_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_keryo_plan_user (
  id varchar(32) NOT NULL,
  plan_id varchar(64) NOT NULL,
  user_id varchar(32) NOT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_plan_user (plan_id, user_id),
  KEY idx_plan_user_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_keryo_audit_log (
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
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_user (user_id),
  KEY idx_audit_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_keryo_ai_config (
  id varchar(32) NOT NULL,
  provider varchar(32) DEFAULT NULL,
  base_url varchar(500) DEFAULT NULL,
  model varchar(128) DEFAULT NULL,
  api_key varchar(1000) DEFAULT NULL,
  enabled int NOT NULL DEFAULT 0,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_keryo_forget_pass (
  id varchar(32) NOT NULL,
  user_id varchar(32) NOT NULL,
  user_name varchar(255) NOT NULL,
  real_name varchar(255) NOT NULL,
  status int NOT NULL DEFAULT 0,
  remark varchar(500) DEFAULT NULL,
  handle_by varchar(32) DEFAULT NULL,
  handle_time datetime DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_forget_user (user_id),
  KEY idx_forget_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- BK-16: non-destructive retention archives. Source rows are intentionally kept.
CREATE TABLE el_keryo_audit_log_archive (
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
  PRIMARY KEY (id),
  KEY idx_audit_archive_time (create_time),
  KEY idx_audit_archive_at (archived_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_exam_record_archive (
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
  PRIMARY KEY (id),
  KEY idx_exam_record_archive_time (create_time),
  KEY idx_exam_record_archive_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_paper_archive (
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
  PRIMARY KEY (id),
  KEY idx_paper_archive_time (create_time),
  KEY idx_paper_archive_user (user_id),
  KEY idx_paper_archive_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_paper_qu_archive (
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
  PRIMARY KEY (id),
  KEY idx_paper_qu_archive_paper (paper_id),
  KEY idx_paper_qu_archive_qu (qu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_paper_qu_answer_archive (
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
  PRIMARY KEY (id),
  KEY idx_paper_answer_archive_paper (paper_id),
  KEY idx_paper_answer_archive_qu (qu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE el_exam_cheat_log_archive (
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
  PRIMARY KEY (id),
  KEY idx_cheat_archive_paper (paper_id),
  KEY idx_cheat_archive_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DELIMITER $$
CREATE TRIGGER trg_exam_record_locked
BEFORE UPDATE ON el_exam_record
FOR EACH ROW
BEGIN
  IF OLD.locked = 1 AND (
      NEW.max_score <> OLD.max_score OR NEW.last_score <> OLD.last_score
      OR NEW.passed <> OLD.passed OR NEW.exam_id <> OLD.exam_id
      OR NEW.user_id <> OLD.user_id OR NEW.paper_id <> OLD.paper_id
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'locked exam record cannot be changed';
  END IF;
END$$

CREATE TRIGGER trg_audit_log_no_delete
BEFORE DELETE ON el_keryo_audit_log
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit log cannot be deleted';
END$$
DELIMITER ;

