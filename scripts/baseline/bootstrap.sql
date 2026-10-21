-- Public portfolio bootstrap schema.
--
-- This file contains table definitions and non-sensitive system metadata only.
-- It intentionally excludes customer records, user records, uploaded files, logs, and provider credentials.
-- Demo users, exam, and question data are added by seed.sql and demo_questions.sql.
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `el_cfg_base`;
CREATE TABLE `el_cfg_base` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `site_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '系统名称',
  `login_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '登录LOGO',
  `login_bg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '登录背景',
  `back_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '后台LOGO',
  `copy_right` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '版权信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统设置';
DROP TABLE IF EXISTS `el_cfg_switch`;
CREATE TABLE `el_cfg_switch` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '功能名称',
  `val` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开关或值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='功能配置';
DROP TABLE IF EXISTS `el_exam`;
CREATE TABLE `el_exam` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试名称',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '考试描述',
  `open_type` int NOT NULL DEFAULT '1' COMMENT '1公开2部门3定员',
  `state` int NOT NULL DEFAULT '0' COMMENT '考试状态',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
  `total_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总分数',
  `total_time` int NOT NULL DEFAULT '0' COMMENT '总时长（分钟）',
  `qualify_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '及格分数',
  `chance` int NOT NULL DEFAULT '0' COMMENT '考试机会',
  `hand_min` int NOT NULL DEFAULT '0' COMMENT '最低交卷分钟',
  `late_max` int NOT NULL DEFAULT '0' COMMENT '允许迟到分钟',
  `thanks` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '感谢文字',
  `repo_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组卷题库ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程';
DROP TABLE IF EXISTS `el_exam_record`;
CREATE TABLE `el_exam_record` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `exam_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试ID',
  `paper_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结算的试卷ID',
  `try_count` int NOT NULL DEFAULT '1' COMMENT '考试次数',
  `max_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最高分数',
  `last_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最近分数',
  `passed` tinyint NOT NULL DEFAULT '0' COMMENT '是否通过',
  `locked` tinyint NOT NULL DEFAULT '0' COMMENT '成绩锁定: 0未锁/1锁定(T-08.2)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`exam_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试记录';
DROP TABLE IF EXISTS `el_exam_rule`;
CREATE TABLE `el_exam_rule` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `exam_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试ID',
  `repo_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题库ID',
  `qu_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '题型',
  `qu_count` int DEFAULT NULL COMMENT '出题数量',
  `qu_score` decimal(10,0) NOT NULL DEFAULT '1' COMMENT '每题分数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试规则';
DROP TABLE IF EXISTS `el_paper`;
CREATE TABLE `el_paper` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `exam_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试标题',
  `total_time` int NOT NULL DEFAULT '0' COMMENT '考试时长',
  `user_time` int NOT NULL DEFAULT '0' COMMENT '用户时长',
  `total_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '试卷总分',
  `qualify_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '试卷及格分',
  `user_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '用户总得分',
  `limit_time` datetime NOT NULL COMMENT '最后截止时间',
  `hand_time` datetime DEFAULT NULL COMMENT '实际交卷时间',
  `hand_state` int NOT NULL DEFAULT '0' COMMENT '交卷状态,0未交卷,1已交卷,2强制交卷',
  `exam_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '考试首访IP(IP_CHANGE检测)',
  `session_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会话HMAC密钥(M7答案签名)',
  `last_answer_time` datetime DEFAULT NULL COMMENT '上次答题时间(M2逐题间隔)',
  `passed` tinyint NOT NULL DEFAULT '0' COMMENT '成绩是否合格',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `exam_id` (`exam_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷';
DROP TABLE IF EXISTS `el_paper_qu`;
CREATE TABLE `el_paper_qu` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `paper_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷ID',
  `qu_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目ID',
  `qu_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目类型',
  `answered` tinyint NOT NULL DEFAULT '0' COMMENT '是否已答',
  `mark` tinyint NOT NULL DEFAULT '0' COMMENT '是否标记',
  `sort` int NOT NULL DEFAULT '0' COMMENT '问题排序',
  `score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单题分分值',
  `actual_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '实际得分(主观题)',
  `answer_text` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '考生主观题答案',
  `answer_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'M7答案HMAC签名',
  `is_right` tinyint NOT NULL DEFAULT '0' COMMENT '是否答对',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `paper_id` (`paper_id`) USING BTREE,
  KEY `qu_id` (`qu_id`) USING BTREE,
  KEY `sort` (`sort`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷考题';
DROP TABLE IF EXISTS `el_exam_cheat_log`;
CREATE TABLE `el_exam_cheat_log` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `paper_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '试卷ID',
  `exam_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '考试ID',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户ID',
  `qu_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目ID',
  `cheat_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '违规类型: CUT_SCREEN切屏/IP_CHANGE换IP/FAST_ANSWER答题过快',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详情',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `paper_id` (`paper_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试违规日志';
DROP TABLE IF EXISTS `el_paper_qu_answer`;
CREATE TABLE `el_paper_qu_answer` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `paper_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷ID',
  `answer_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回答项ID',
  `qu_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目ID',
  `is_right` tinyint NOT NULL DEFAULT '0' COMMENT '是否正确项',
  `answer` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '填空题',
  `checked` tinyint NOT NULL DEFAULT '0' COMMENT '是否选中',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `abc` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '选项标签',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `paper_id` (`paper_id`) USING BTREE,
  KEY `qu_id` (`qu_id`) USING BTREE,
  KEY `paper_qu_id` (`paper_id`,`qu_id`) USING BTREE,
  KEY `sort` (`sort`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷考题备选答案';
DROP TABLE IF EXISTS `el_repo`;
CREATE TABLE `el_repo` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '题库名称',
  `cat_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '分类ID',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '题库备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题库';
DROP TABLE IF EXISTS `el_repo_qu`;
CREATE TABLE `el_repo_qu` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `repo_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属题库',
  `chapter_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属章节',
  `qu_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目类型',
  `difficulty_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '难度等级',
  `score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '题目默认分值(AI出题时设定,建考试自动带出)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目内容',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '整题解析',
  `review_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT '审核状态: DRAFT/REVIEW2/APPROVED/REJECTED',
  `review_level` int NOT NULL DEFAULT '0' COMMENT '审核级别',
  `first_reviewer` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '一审人',
  `first_review_time` datetime DEFAULT NULL COMMENT '一审时间',
  `second_reviewer` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '二审人',
  `second_review_time` datetime DEFAULT NULL COMMENT '二审时间',
  `review_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `qu_type` (`qu_type`) USING BTREE,
  KEY `repo_id` (`repo_id`) USING BTREE,
  KEY `chapter_id` (`chapter_id`) USING BTREE,
  KEY `level` (`difficulty_level`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='问题题目';
DROP TABLE IF EXISTS `el_repo_qu_answer`;
CREATE TABLE `el_repo_qu_answer` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `qu_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题ID',
  `is_right` tinyint NOT NULL DEFAULT '0' COMMENT '是否正确',
  `content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '答案内容',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片地址',
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ABCD标签',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `qu_id` (`qu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='候选答案';
DROP TABLE IF EXISTS `el_sys_depart`;
CREATE TABLE `el_sys_depart` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `dept_type` int NOT NULL DEFAULT '1' COMMENT '1公司2部门',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '所属上级',
  `dept_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '部门名称',
  `dept_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '部门编码',
  `dept_level` int NOT NULL DEFAULT '0' COMMENT '部门层级',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `dept_code` (`dept_code`) USING BTREE,
  KEY `parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门信息';
DROP TABLE IF EXISTS `el_sys_dic`;
CREATE TABLE `el_sys_dic` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典编码',
  `type` int DEFAULT NULL COMMENT '1分类字典,2数据字典',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `code` (`dic_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类字典';
DROP TABLE IF EXISTS `el_sys_dic_value`;
CREATE TABLE `el_sys_dic_value` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID/字典编码',
  `dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '归属字典',
  `dic_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子项编码',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类名称',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上级ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类字典值';
DROP TABLE IF EXISTS `el_sys_menu`;
CREATE TABLE `el_sys_menu` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上级菜单',
  `menu_type` int NOT NULL DEFAULT '1' COMMENT '1菜单2功能',
  `permission_tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '访问路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '视图或Layout',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '跳转地址',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `meta_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由标题',
  `meta_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由标题',
  `meta_active_menu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '高亮菜单',
  `meta_no_cache` tinyint DEFAULT NULL COMMENT '是否缓存',
  `hidden` tinyint DEFAULT NULL COMMENT '是否隐藏',
  `sort` int DEFAULT NULL COMMENT '越小越前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `permission_tag` (`permission_tag`) USING BTREE,
  UNIQUE KEY `path` (`path`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE,
  KEY `parent_id` (`parent_id`) USING BTREE,
  KEY `menu_type` (`menu_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统菜单';
DROP TABLE IF EXISTS `el_sys_role`;
CREATE TABLE `el_sys_role` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '角色名称',
  `data_scope` int NOT NULL COMMENT '数据权限',
  `role_level` int NOT NULL COMMENT '越大越高',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色';
DROP TABLE IF EXISTS `el_sys_role_menu`;
CREATE TABLE `el_sys_role_menu` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  `menu_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  `data_flag` int NOT NULL DEFAULT '0' COMMENT '数据标识',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `role_id` (`role_id`) USING BTREE,
  KEY `menu_id` (`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单授权';
DROP TABLE IF EXISTS `el_sys_user`;
CREATE TABLE `el_sys_user` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户名',
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '密码',
  `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '密码盐',
  `state` int NOT NULL DEFAULT '0' COMMENT '状态',
  `id_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '身份证号码',
  `mobile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `dept_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '部门编码',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `dept_code` (`dept_code`) USING BTREE,
  KEY `user_name` (`user_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理用户';
DROP TABLE IF EXISTS `el_sys_user_role`;
CREATE TABLE `el_sys_user_role` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  KEY `role_id` (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色';
DROP TABLE IF EXISTS `pl_plugin_data`;
CREATE TABLE `pl_plugin_data` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '唯一识别码',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件名称',
  `schema_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '元数据ID',
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组ID',
  `config_data` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置数据',
  `service_clazz` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '后端服务类',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '前端页面',
  `in_use` tinyint NOT NULL COMMENT '是否使用',
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件信息';
DROP TABLE IF EXISTS `pl_plugin_group`;
CREATE TABLE `pl_plugin_group` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组名称',
  `single` tinyint DEFAULT NULL COMMENT '独立排斥',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件分组';
DROP TABLE IF EXISTS `pl_plugin_schema`;
CREATE TABLE `pl_plugin_schema` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `schema_data` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '元数据',
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='插件元数据';
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CALENDAR_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CRON_EXPRESSION` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TIME_ZONE_ID` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ENTRY_ID` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `INSTANCE_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`) USING BTREE,
  KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`) USING BTREE,
  KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`) USING BTREE,
  KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `IS_DURABLE` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `IS_UPDATE_DATA` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`) USING BTREE,
  KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `LOCK_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `INSTANCE_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `STR_PROP_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `STR_PROP_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `STR_PROP_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `INT_PROP_1` int DEFAULT NULL,
  `INT_PROP_2` int DEFAULT NULL,
  `LONG_PROP_1` bigint DEFAULT NULL,
  `LONG_PROP_2` bigint DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint DEFAULT NULL,
  `PREV_FIRE_TIME` bigint DEFAULT NULL,
  `PRIORITY` int DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_TYPE` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `MISFIRE_INSTR` smallint DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`) USING BTREE,
  KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`) USING BTREE,
  KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`) USING BTREE,
  KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`) USING BTREE,
  KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`) USING BTREE,
  KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`) USING BTREE,
  KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`) USING BTREE,
  KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`) USING BTREE,
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`) USING BTREE,
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Non-sensitive system metadata required for menu routing and authorization.
INSERT INTO el_cfg_base (id, site_name, login_logo, login_bg, back_logo, copy_right) VALUES ('1', '医护培训平台', '', '', '', 'Portfolio demo environment') ON DUPLICATE KEY UPDATE site_name=VALUES(site_name), login_logo=VALUES(login_logo), login_bg=VALUES(login_bg), back_logo=VALUES(back_logo), copy_right=VALUES(copy_right);
INSERT INTO el_cfg_switch (id, val) VALUES ('loginTick', '0'), ('userReg', 'true') ON DUPLICATE KEY UPDATE val=VALUES(val);
INSERT INTO el_sys_role (id, role_name, data_scope, role_level, remark, create_time, create_by, update_time, update_by) VALUES ('admin', '超级管理员', 4, 999, 'portfolio demo administrator', NOW(), 'bootstrap', NOW(), 'bootstrap'), ('user', '学员', 1, 1, 'portfolio demo learner', NOW(), 'bootstrap', NOW(), 'bootstrap') ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), data_scope=VALUES(data_scope), role_level=VALUES(role_level), remark=VALUES(remark);
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399935914490269698', 'data_scope', 1, '数据权限', '角色数据权限范围', '2021-06-02 11:48:26', '2021-06-02 11:48:26', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1492388856845103105', 'state', 1, '通用状态', '系统全局通用状态', '2022-02-12 14:43:25', '2022-02-12 14:43:25', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1546399651832299522', 'user_state', 1, '用户状态', '用户状态', '2022-07-11 15:43:02', '2022-07-11 15:43:02', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552482298987851778', 'menu_type', 1, '菜单类型', '目录/菜单/功能', '2022-07-28 10:33:18', '2022-07-28 10:33:18', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552554336742055937', 'dic_type', 1, '字典类型', '数据字典类型枚举', '2022-07-28 15:19:33', '2022-07-28 15:19:33', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1698896002028761089', 'plugin_group', 1, '插件类型', '插件分组类型', '2023-09-05 11:09:05', '2023-09-05 11:09:05', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910524211750133761', 'repo_catalog', 2, '题库分类', '题库分类', '2025-04-11 10:44:02', '2025-04-11 10:44:02', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910602708967395330', 'qu_type', 1, '试题题型', '试题题型', '2025-04-11 15:55:57', '2025-04-11 15:55:57', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910603515049709569', 'qu_difficulty_level', 1, '试题难度', '试题难度', '2025-04-11 15:59:09', '2025-04-11 15:59:09', '', '');
INSERT INTO `el_sys_dic` (`id`, `dic_code`, `type`, `title`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914160770006573057', 'yes_no', 1, '是否', '通用是否选项', '2025-04-21 11:34:25', '2025-04-21 11:34:25', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1356065526639505409', 'exam_open_type', '1', '完全公开', '0', NULL, '2021-02-01 10:23:10', '2021-02-01 10:23:10', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399936107176595458', 'data_scope', '1', '仅本人数据', '0', '仅本人数据', '2021-06-02 11:49:12', '2021-06-02 11:49:12', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399936146162651137', 'data_scope', '2', '本部门数据', '0', '本部门数据', '2021-06-02 11:49:21', '2021-06-02 11:49:21', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399936188902608897', 'data_scope', '3', '本部门及以下', '0', '本部门及以下', '2021-06-02 11:49:31', '2021-06-02 11:49:31', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399936230719819777', 'data_scope', '4', '全部数据', '0', '全部数据', '2021-06-02 11:49:41', '2021-06-02 11:49:41', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1492388894015025153', 'state', '0', '正常', '0', NULL, '2022-02-12 14:43:34', '2022-02-12 14:43:34', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1492388933202407426', 'state', '1', '禁用', '0', NULL, '2022-02-12 14:43:43', '2022-02-12 14:43:43', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1546399693989249025', 'user_state', '0', '正常', '0', NULL, '2022-07-11 15:43:12', '2022-07-11 15:43:12', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1546399745172340738', 'user_state', '1', '已禁用', '0', NULL, '2022-07-11 15:43:24', '2022-07-11 15:43:24', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1546399778231844866', 'user_state', '2', '待审核', '0', NULL, '2022-07-11 15:43:32', '2022-07-11 15:43:32', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552483045594935298', 'menu_type', '1', '目录', '0', NULL, '2022-07-28 10:36:16', '2022-07-28 10:36:16', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552483057787777025', 'menu_type', '2', '菜单', '0', NULL, '2022-07-28 10:36:19', '2022-07-28 10:36:19', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552483066511929345', 'menu_type', '3', '功能', '0', NULL, '2022-07-28 10:36:21', '2022-07-28 10:36:21', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552554710609731586', 'dic_type', '1', '数据字典', '0', NULL, '2022-07-28 15:21:02', '2022-07-28 15:21:02', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552554758366076929', 'dic_type', '2', '分类字典', '0', NULL, '2022-07-28 15:21:14', '2022-07-28 15:21:14', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1698896091577151490', 'plugin_group', 'upload', '文件上传', '0', NULL, '2023-09-05 11:09:26', '2023-09-05 11:09:26', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910524409985523713', 'repo_catalog', '1910524409985523713', '企业文化', '0', NULL, '2025-04-11 10:44:49', '2025-04-11 10:44:49', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910524529837760513', 'repo_catalog', '1910524529837760513', '岗位晋升', '0', NULL, '2025-04-11 10:45:18', '2025-04-11 10:45:18', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910524561106296834', 'repo_catalog', '1910524561106296834', '职业技能', '0', NULL, '2025-04-11 10:45:25', '2025-04-11 10:45:25', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910602921656356865', 'qu_type', 'radio', '单选题', '0', NULL, '2025-04-11 15:56:48', '2025-04-11 15:56:48', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910602957609930754', 'qu_type', 'multi', '多选题', '0', NULL, '2025-04-11 15:56:57', '2025-04-11 15:56:57', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910603005953478658', 'qu_type', 'judge', '判断题', '0', NULL, '2025-04-11 15:57:08', '2025-04-11 15:57:08', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910603051000000001', 'qu_type', 'fill', '填空题', '0', NULL, '2025-04-11 15:56:48', '2025-04-11 15:56:48', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910603051000000002', 'qu_type', 'essay', '简答题', '0', NULL, '2025-04-11 15:56:48', '2025-04-11 15:56:48', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910603594192031746', 'qu_difficulty_level', 'easy', '简单', '0', NULL, '2025-04-11 15:59:28', '2025-04-11 15:59:28', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910603630212714498', 'qu_difficulty_level', 'normal', '一般', '0', NULL, '2025-04-11 15:59:37', '2025-04-11 15:59:37', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910604188910784514', 'qu_difficulty_level', 'hard', '较难', '0', NULL, '2025-04-11 16:01:50', '2025-04-11 16:01:50', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910604353570770946', 'qu_difficulty_level', 'extreme', '极难', '0', NULL, '2025-04-11 16:02:29', '2025-04-11 16:02:29', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914160827720196097', 'yes_no', '1', '是', '0', NULL, '2025-04-21 11:34:39', '2025-04-21 11:34:39', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914160855650066434', 'yes_no', '0', '否', '0', NULL, '2025-04-21 11:34:46', '2025-04-21 11:34:46', '', '');
INSERT INTO `el_sys_dic_value` (`id`, `dic_code`, `dic_value`, `title`, `parent_id`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1915226614203678722', 'repo_catalog', '1915226614203678722', '演示', '0', NULL, '2025-04-24 10:09:42', '2025-04-24 10:09:42', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1367010529435996174', '0', 1, NULL, '/admin/sys', '#', '/admin/sys/menu', 'Sys', '系统设置', 'carbon:settings', NULL, 1, 0, 17, '2021-03-07 11:08:18', '2022-01-06 18:10:33', '', '10001');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1367010529435996176', '1367010529435996174', 2, NULL, '/admin/sys/menu', 'views/System/Menu/Menu', NULL, 'SysMenu', '菜单管理', '', NULL, 1, 0, 2, '2021-03-07 11:08:18', '2021-05-31 10:14:26', '', '10001');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1367010529435996178', '1367010529435996174', 2, NULL, '/admin/sys/role', 'views/System/Role/Role', NULL, 'SysRole', '角色管理', '', NULL, 1, 0, 3, '2021-03-07 11:08:18', '2022-01-06 18:10:17', '', '10001');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1367010529435996179', '1367010529435996174', 2, NULL, '/admin/sys/dict', 'views/System/DataDict/DataDict', NULL, 'SysDataDict', '数据字典', '', NULL, 1, 0, 3, '2021-03-07 11:08:18', '2022-01-06 18:10:17', '', '10001');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399266644605394945', '1367010529435996176', 3, 'sys:menu:add', NULL, NULL, NULL, 'SysMenuAdd', '添加', NULL, NULL, 1, 1, 1, '2021-05-31 15:29:00', '2021-05-31 15:28:59', '10001', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399266713060630529', '1367010529435996176', 3, 'sys:menu:update', NULL, NULL, NULL, 'SysMenuUpdate', '修改', NULL, NULL, 1, 1, 2, '2021-05-31 15:29:16', '2021-05-31 15:29:16', '10001', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399266787438223361', '1367010529435996176', 3, 'sys:menu:delete', NULL, NULL, NULL, 'SysMenuDelete', '删除', NULL, NULL, 1, 1, 3, '2021-05-31 15:29:34', '2021-05-31 15:29:33', '10001', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1399266868300210177', '1367010529435996176', 3, 'sys:menu:sort', NULL, NULL, NULL, 'SysMenuSort', '排序', NULL, NULL, 1, 1, 4, '2021-05-31 15:29:53', '2021-05-31 15:29:53', '10001', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552509704679235586', '1367010529435996178', 3, 'sys:role:paging', NULL, NULL, NULL, 'SysRolePaging', '查看', '', NULL, NULL, 1, 1, '2022-07-28 12:22:12', '2022-07-28 12:22:12', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552510807093321730', '1367010529435996178', 3, 'sys:role:add', NULL, NULL, NULL, 'SysRoleAdd', '添加', NULL, NULL, NULL, 1, 2, '2022-07-28 12:26:35', '2022-07-28 12:26:35', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552510872314748929', '1367010529435996178', 3, 'sys:role:edit', NULL, NULL, NULL, 'SysRoleEdit', '修改', NULL, NULL, NULL, 1, 3, '2022-07-28 12:26:50', '2022-07-28 12:26:50', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552510963301785601', '1367010529435996178', 3, 'sys:role:delete', NULL, NULL, NULL, 'SysRoleDelete', '删除', NULL, NULL, NULL, 1, 4, '2022-07-28 12:27:12', '2022-07-28 12:27:12', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552547259067965442', '0', 1, NULL, '/admin/org', '#', NULL, 'SysDept', '组织架构', 'carbon:category', NULL, NULL, 0, 16, '2022-07-28 14:51:26', '2022-07-28 14:51:26', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552549085188235265', '1367010529435996176', 3, 'sys:menu:paging', NULL, NULL, NULL, 'SysMenuPaging', '查看', NULL, NULL, NULL, 1, 5, '2022-07-28 14:58:41', '2022-07-28 14:58:41', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552615713615716353', '1367010529435996179', 3, 'sys:dict:add', NULL, NULL, NULL, 'SysDictAdd', '添加', NULL, NULL, NULL, NULL, 1, '2022-07-28 19:23:26', '2022-07-28 19:23:26', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552615821107339266', '1367010529435996179', 3, 'sys:dict:edit', NULL, NULL, NULL, 'SysDictEdit', '修改', NULL, NULL, NULL, NULL, 2, '2022-07-28 19:23:52', '2022-07-28 19:23:52', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552615880121196546', '1367010529435996179', 3, 'sys:dict:delete', NULL, NULL, NULL, 'SysDictDelete', '删除', NULL, NULL, NULL, NULL, 3, '2022-07-28 19:24:06', '2022-07-28 19:24:06', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552616085025529857', '1367010529435996179', 3, 'sys:dict:paging', NULL, NULL, NULL, 'SysDictPaging', '查看', NULL, NULL, NULL, NULL, 4, '2022-07-28 19:24:55', '2022-07-28 19:24:55', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1552842968174489602', '1552547259067965442', 2, NULL, '/admin/org/depart', 'views/System/Depart/Depart', NULL, 'OrgDepart', '部门管理', NULL, NULL, NULL, NULL, 1, '2022-07-29 10:26:28', '2022-07-29 10:26:28', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1553180631951761409', '1367010529435996178', 3, 'sys:role:grant', NULL, NULL, NULL, 'SysRoleGrant', '角色授权', NULL, NULL, NULL, NULL, 5, '2022-07-30 08:48:14', '2022-07-30 08:48:14', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1556906844813914114', '1552547259067965442', 2, NULL, '/admin/org/user', 'views/System/User/User', NULL, 'OrgUser', '人员管理', NULL, NULL, NULL, NULL, 2, '2022-08-09 15:34:52', '2022-08-09 15:34:52', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1556907609842380801', '1556906844813914114', 3, 'sys:user:edit', NULL, NULL, NULL, 'SysUserEdit', '修改', NULL, NULL, NULL, 1, 3, '2022-08-09 15:37:54', '2022-08-09 15:37:54', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1556907683607605249', '1556906844813914114', 3, 'sys:user:add', NULL, NULL, NULL, 'SysUserAdd', '添加', NULL, NULL, NULL, 1, 2, '2022-08-09 15:38:12', '2022-08-09 15:38:12', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1556907994690744321', '1556906844813914114', 3, 'sys:user:paging', NULL, NULL, NULL, 'SysUserPaging', '查看', NULL, NULL, NULL, 1, 1, '2022-08-09 15:39:26', '2022-08-09 15:39:26', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1556908088521519105', '1556906844813914114', 3, 'sys:user:delete', NULL, NULL, NULL, 'SysUserDelete', '删除', NULL, NULL, NULL, 1, 4, '2022-08-09 15:39:48', '2022-08-09 15:39:48', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1556911576896446466', '1556906844813914114', 3, 'sys:user:state', NULL, NULL, NULL, 'SysUserState', '启用/禁用', NULL, NULL, NULL, 1, 5, '2022-08-09 15:53:40', '2022-08-09 15:53:40', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1557199944846876674', '1552842968174489602', 3, 'sys:depart:view', NULL, NULL, NULL, 'SysDeptView', '查看', NULL, NULL, NULL, 1, 3, '2022-08-10 10:59:32', '2022-08-10 10:59:32', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1557199992607416322', '1552842968174489602', 3, 'sys:depart:add', NULL, NULL, NULL, 'SysDeptAdd', '添加', NULL, NULL, NULL, 1, 1, '2022-08-10 10:59:44', '2022-08-10 10:59:44', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1557200059632394242', '1552842968174489602', 3, 'sys:depart:edit', NULL, NULL, NULL, 'SysDeptEdit', '修改', NULL, NULL, NULL, 1, 4, '2022-08-10 11:00:00', '2022-08-10 11:00:00', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1557200122416930817', '1552842968174489602', 3, 'sys:depart:delete', NULL, NULL, NULL, 'SysDeptDelete', '删除', NULL, NULL, NULL, 1, 2, '2022-08-10 11:00:15', '2022-08-10 11:00:15', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1564510476430225410', '1367010529435996174', 2, NULL, '/admin/sys/config', 'views/System/Config/Config', NULL, 'SysConfig', '个性配置', NULL, NULL, NULL, NULL, 4, '2022-08-30 15:08:59', '2022-08-30 15:08:59', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1566612797037527042', '1367010529435996174', 2, NULL, '/admin/sys/plugin', 'views/System/Plugin/Plugin', NULL, 'Plugin', '插件管理', NULL, NULL, NULL, NULL, 5, '2022-09-05 10:22:51', '2022-09-05 10:22:51', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1700043149512056834', '0', 1, NULL, '/admin', '#', '/admin/dashboard', 'HomeDashboard', '管理首页', 'ant-design:dashboard-outlined', NULL, NULL, NULL, 12, '2023-09-08 15:07:27', '2023-09-08 15:11:37', '1000000000000000001', '1000000000000000001');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1700044482038243330', '1700043149512056834', 2, NULL, '/admin/dashboard', 'views/Dashboard/Dashboard', NULL, 'Dashboard', '工作台', '', NULL, NULL, NULL, 1, '2023-09-08 15:12:44', '2023-09-08 15:12:52', '1000000000000000001', '1000000000000000001');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910230973105442817', '0', 1, NULL, '/admin/repo', '#', NULL, 'HomeRepo', '题库管理', 'ant-design:product-outlined', NULL, NULL, NULL, 14, '2025-04-10 15:18:49', '2025-04-10 15:18:49', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910231460139634690', '1910230973105442817', 2, NULL, '/admin/repo/repo', 'views/Exam/Repo/Repo', NULL, 'Repo', '题库管理', NULL, NULL, NULL, 0, 1, '2025-04-10 15:20:45', '2025-04-10 15:20:45', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910540102659358722', '1910230973105442817', 2, NULL, '/admin/repo/qu', 'views/Exam/Repo/Qu', NULL, 'Qu', '试题管理', NULL, NULL, NULL, NULL, 2, '2025-04-11 11:47:11', '2025-04-11 11:47:11', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910584408183029762', '1910231460139634690', 3, 'repo:repo:add', '1', '1', NULL, 'RepoAdd', '添加', NULL, NULL, NULL, 1, 1, '2025-04-11 14:43:14', '2025-04-11 14:43:14', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910584475480637442', '1910231460139634690', 3, 'repo:repo:delete', NULL, NULL, NULL, 'RepoDelete', '删除', NULL, NULL, NULL, 1, 2, '2025-04-11 14:43:30', '2025-04-11 14:43:30', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910584582947094529', '1910231460139634690', 3, 'repo:repo:edit', NULL, NULL, NULL, 'RepoEdit', '修改', NULL, NULL, NULL, 1, 3, '2025-04-11 14:43:56', '2025-04-11 14:43:56', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910585687147954177', '1910231460139634690', 3, 'repo:repo:view', NULL, NULL, NULL, 'RepoView', '查看', NULL, NULL, NULL, 1, 4, '2025-04-11 14:48:19', '2025-04-11 14:48:19', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910640225917784066', '1910540102659358722', 2, 'repo:qu:add', '11', '11', NULL, 'QuAdd', '添加', NULL, NULL, NULL, 1, 1, '2025-04-11 18:25:02', '2025-04-11 18:25:02', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910640675547172865', '1910540102659358722', 3, 'repo:qu:delete', NULL, NULL, NULL, 'QuDelete', '删除', NULL, NULL, NULL, 1, 2, '2025-04-11 18:26:49', '2025-04-11 18:26:49', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910640779146481666', '1910540102659358722', 3, 'repo:qu:edit', NULL, NULL, NULL, 'QuEdit', '修改', NULL, NULL, NULL, 1, 3, '2025-04-11 18:27:14', '2025-04-11 18:27:14', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1910640845336793090', '1910540102659358722', 3, 'repo:qu:view', NULL, NULL, NULL, 'QuView', '查看', NULL, NULL, NULL, 1, 4, '2025-04-11 18:27:30', '2025-04-11 18:27:30', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1911703533823365121', '0', 1, NULL, '/admin/admin', '#', NULL, 'HomeExam', '考试管理', 'ant-design:file-text-outlined', NULL, NULL, NULL, 15, '2025-04-14 16:50:14', '2025-04-14 16:50:14', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1911705220688547841', '1911703533823365121', 2, NULL, '/admin/exam/exam', 'views/Exam/Exam/Exam', NULL, 'Exam', '考试管理', NULL, NULL, NULL, NULL, 1, '2025-04-14 16:56:57', '2025-04-14 16:56:57', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1911956917277069314', '1911705220688547841', 3, 'exam:exam:add', '/admin/exam/add', 'views/Exam/Exam/Form', NULL, 'ExamAdd', '添加', NULL, NULL, NULL, 1, 1, '2025-04-15 09:37:06', '2025-04-15 09:37:06', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1911956988110475266', '1911705220688547841', 3, 'exam:exam:delete', NULL, NULL, NULL, 'ExamDelete', '删除', NULL, NULL, NULL, 1, 2, '2025-04-15 09:37:23', '2025-04-15 09:37:23', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1911957176237592577', '1911705220688547841', 3, 'exam:exam:edit', '/admin/exam/edit', 'views/Exam/Exam/Form', NULL, 'ExamEdit', '修改', NULL, NULL, NULL, 1, 3, '2025-04-15 09:38:07', '2025-04-15 09:38:07', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1911957247897276417', '1911705220688547841', 3, 'exam:exam:view', NULL, NULL, NULL, 'ExamView', '查看', NULL, NULL, NULL, 1, 4, '2025-04-15 09:38:25', '2025-04-15 09:38:25', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1912388980346134529', '0', 1, NULL, '/client/exam', '#', NULL, 'ClientExam', '考试中心', 'ant-design:credit-card-outlined', NULL, NULL, NULL, 13, '2025-04-16 14:13:58', '2025-04-16 14:13:58', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1912389902866522113', '1912388980346134529', 2, 'exam:client:list', '/client/exam/list', 'views/Exam/Exam/Client/List', NULL, 'ClientExamList', '在线考试', NULL, NULL, NULL, 0, 1, '2025-04-16 14:17:37', '2025-04-16 14:17:37', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1912445044980776961', '1912389902866522113', 2, 'exam:client:detail', '/client/exam/detail', 'views/Exam/Exam/Client/Detail', NULL, 'ExamClientDetail', '考试详情', NULL, NULL, NULL, 1, 1, '2025-04-16 17:56:44', '2025-04-16 17:56:44', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1912452280306171906', '1912389902866522113', 2, 'exam:client:enter', '/client/exam/enter', 'views/Exam/Exam/Client/Enter', NULL, 'ExamClientEnter', '进入考试', NULL, NULL, NULL, 1, 2, '2025-04-16 18:25:29', '2025-04-16 18:25:29', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1912766674995073025', '1912389902866522113', 2, 'exam:client:result', '/client/exam/result', 'views/Exam/Exam/Client/Result', NULL, 'ExamClientResult', '考试结果', NULL, NULL, NULL, 1, 3, '2025-04-17 15:14:47', '2025-04-17 15:14:47', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914135464021499905', '1911705220688547841', 3, 'exam:record:list', '/admin/exam/record', 'views/Exam/Exam/Record', NULL, 'ExamRecord', '考试记录', NULL, NULL, NULL, 1, 5, '2025-04-21 09:53:52', '2025-04-21 09:53:52', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914145046580039681', '1912388980346134529', 2, 'exam:client:record', '/client/exam/record', 'views/Exam/Exam/Client/Record', NULL, 'ClientExamRecord', '考试记录', NULL, NULL, NULL, 0, 2, '2025-04-21 10:31:56', '2025-04-21 10:31:56', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914244224937299969', '0', 1, 'user', '/client/uc', '#', NULL, 'UCenter', '用户设置', 'ant-design:user-outlined', NULL, NULL, NULL, 18, '2025-04-21 17:06:02', '2025-04-21 17:06:02', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914244884357386242', '1914244224937299969', 2, 'client:user:info', '/client/user/info', 'views/System/User/Client/Info', NULL, 'UserInfoUpdate', '资料修改', NULL, NULL, NULL, NULL, 1, '2025-04-21 17:08:40', '2025-04-21 17:08:40', '', '');
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('1914245167460323330', '1914244224937299969', 2, 'client:user:passwd', '/client/user/passwd', 'views/System/User/Client/Passwd', NULL, 'UserPassUpdate', '密码安全', NULL, NULL, NULL, NULL, 2, '2025-04-21 17:09:47', '2025-04-21 17:09:47', '', '');
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054338392065', 'user', '1912388980346134529', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054338392066', 'user', '1912389902866522113', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054338392067', 'user', '1912445044980776961', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054338392068', 'user', '1912452280306171906', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054338392069', 'user', '1912766674995073025', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054342586369', 'user', '1914145046580039681', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054342586370', 'user', '1914244224937299969', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054342586371', 'user', '1914244884357386242', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914256054342586372', 'user', '1914245167460323330', '2025-04-21 17:53:03', '2025-04-21 17:53:03', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409390628865', 'admin', '1700043149512056834', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409394823170', 'admin', '1700044482038243330', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409394823171', 'admin', '1910230973105442817', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409394823172', 'admin', '1910231460139634690', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409394823173', 'admin', '1910584408183029762', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409399017473', 'admin', '1910584475480637442', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409399017474', 'admin', '1910584582947094529', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409399017475', 'admin', '1910585687147954177', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211777', 'admin', '1910540102659358722', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211778', 'admin', '1910640225917784066', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211779', 'admin', '1910640675547172865', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211780', 'admin', '1910640779146481666', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211781', 'admin', '1910640845336793090', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211782', 'admin', '1911703533823365121', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409403211783', 'admin', '1911705220688547841', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406082', 'admin', '1911956917277069314', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406083', 'admin', '1911956988110475266', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406084', 'admin', '1911957176237592577', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406085', 'admin', '1911957247897276417', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406086', 'admin', '1914135464021499905', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406087', 'admin', '1552547259067965442', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406088', 'admin', '1552842968174489602', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406089', 'admin', '1557199992607416322', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409407406090', 'admin', '1557200122416930817', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600386', 'admin', '1557199944846876674', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600387', 'admin', '1557200059632394242', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600388', 'admin', '1556906844813914114', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600389', 'admin', '1556907994690744321', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600390', 'admin', '1556907683607605249', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600391', 'admin', '1556907609842380801', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600392', 'admin', '1556908088521519105', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600393', 'admin', '1556911576896446466', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409411600394', 'admin', '1367010529435996174', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794689', 'admin', '1367010529435996176', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794690', 'admin', '1399266644605394945', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794691', 'admin', '1399266713060630529', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794692', 'admin', '1399266787438223361', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794693', 'admin', '1399266868300210177', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794694', 'admin', '1552549085188235265', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794695', 'admin', '1367010529435996178', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794696', 'admin', '1552509704679235586', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794697', 'admin', '1552510807093321730', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409415794698', 'admin', '1552510872314748929', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419988994', 'admin', '1552510963301785601', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419988995', 'admin', '1553180631951761409', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419988996', 'admin', '1367010529435996179', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419988997', 'admin', '1552615713615716353', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419988998', 'admin', '1552615821107339266', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419988999', 'admin', '1552615880121196546', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419989000', 'admin', '1552616085025529857', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419989001', 'admin', '1564510476430225410', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419989002', 'admin', '1566612797037527042', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419989003', 'admin', '1914244224937299969', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409419989004', 'admin', '1914244884357386242', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('1914593409424183298', 'admin', '1914245167460323330', '2025-04-22 16:13:34', '2025-04-22 16:13:34', '', '', 0);
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `redirect`, `name`, `meta_title`, `meta_icon`, `meta_active_menu`, `meta_no_cache`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES ('9000000000000000023', '1910230973105442817', 2, NULL, '/admin/practice/paper', 'views/Exam/Practice/PracticePaper', NULL, 'PracticePaper', '练习卷管理', '', NULL, 1, 0, 6, NOW(), NOW(), 'admin', 'admin');
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`) VALUES ('9000000000000000031', 'keryo_assistant', '9000000000000000023', NOW(), NOW(), 'admin', 'admin', 0);
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`)
SELECT CONCAT('R', id), 'keryo_head_nurse', id, NOW(), NOW(), 'admin', 'admin', 0 FROM `el_sys_menu`;
INSERT INTO `el_sys_menu` (`id`, `parent_id`, `menu_type`, `permission_tag`, `path`, `component`, `name`, `meta_title`, `meta_icon`, `hidden`, `sort`, `create_time`, `update_time`, `create_by`, `update_by`)
VALUES ('9000000000000000022', '9000000000000000011', 2, 'client:practice', '/client/practice', 'views/Exam/Practice/ClientPractice', 'PracticeClient', '练习中心', '', 0, 22, NOW(), NOW(), 'admin', 'admin');
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`)
SELECT CONCAT('RM', '9000000000000000022'), 'user', '9000000000000000022', NOW(), NOW(), 'admin', 'admin', 0
WHERE NOT EXISTS (SELECT 1 FROM `el_sys_role_menu` WHERE role_id='user' AND menu_id='9000000000000000022');
INSERT INTO `el_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `data_flag`)
SELECT CONCAT('RMN', '9000000000000000022'), 'keryo_nurse', '9000000000000000022', NOW(), NOW(), 'admin', 'admin', 0
WHERE NOT EXISTS (SELECT 1 FROM `el_sys_role_menu` WHERE role_id='keryo_nurse' AND menu_id='9000000000000000022');

SET FOREIGN_KEY_CHECKS = 1;

