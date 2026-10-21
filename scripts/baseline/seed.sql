SET NAMES utf8mb4;

-- User records and their password hashes are generated in memory by
-- run_baseline.ps1 from BASELINE_*_PASSWORD environment variables.
-- No account credential material is stored in this public fixture.

INSERT INTO el_sys_role
  (id, role_name, data_scope, role_level, remark, create_time, create_by, update_time, update_by)
VALUES
  ('keryo_director', '培训管理员', 4, 100, 'baseline manager', NOW(), 'baseline', NOW(), 'baseline'),
  ('keryo_nurse', '学员', 1, 40, 'baseline learner', NOW(), 'baseline', NOW(), 'baseline'),
  ('keryo_team_leader', '小组长', 1, 30, 'baseline team leader: department-scoped participation management', NOW(), 'baseline', NOW(), 'baseline')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), data_scope=VALUES(data_scope), role_level=VALUES(role_level);

INSERT INTO el_sys_depart
  (id, dept_type, parent_id, dept_name, dept_code, area_code, dept_level, sort, create_time, update_time, create_by, update_by)
VALUES
  ('baseline_dept', 1, '0', 'Baseline Center', 'BASELINE_DEPT', 'BASELINE_AREA', 1, 1, NOW(), NOW(), 'baseline', 'baseline')
ON DUPLICATE KEY UPDATE dept_name=VALUES(dept_name), area_code=VALUES(area_code);

UPDATE el_sys_user
SET dept_code='BASELINE_DEPT',
    area_code='BASELINE_AREA'
WHERE user_name='nurse_head';

UPDATE el_sys_user SET dept_code='BASELINE_DEPT', area_code=NULL WHERE user_name='student';

INSERT INTO el_sys_role_menu (id, role_id, menu_id, create_time, update_time, create_by, update_by, data_flag)
SELECT MD5(CONCAT('baseline-manager-menu-',menu_id)), 'keryo_director', menu_id, NOW(), NOW(), 'baseline', 'baseline', data_flag
FROM el_sys_role_menu source WHERE source.role_id='admin'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

INSERT INTO el_sys_role_menu (id, role_id, menu_id, create_time, update_time, create_by, update_by, data_flag)
SELECT MD5(CONCAT('baseline-student-menu-',menu_id)), 'keryo_nurse', menu_id, NOW(), NOW(), 'baseline', 'baseline', data_flag
FROM el_sys_role_menu source WHERE source.role_id='user'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

INSERT INTO el_sys_role_menu (id, role_id, menu_id, create_time, update_time, create_by, update_by, data_flag)
SELECT MD5(CONCAT('baseline-team-leader-menu-',menu_id)), 'keryo_team_leader', menu_id, NOW(), NOW(), 'baseline', 'baseline', data_flag
FROM el_sys_role_menu source WHERE source.role_id='user'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

INSERT INTO el_sys_role_menu (id, role_id, menu_id, create_time, update_time, create_by, update_by, data_flag)
SELECT MD5(CONCAT('baseline-team-leader-plan-',menu_id)), 'keryo_team_leader', menu_id, NOW(), NOW(), 'baseline', 'baseline', data_flag
FROM el_sys_role_menu source
JOIN el_sys_menu menu ON menu.id=source.menu_id
WHERE source.role_id='admin' AND menu.path LIKE '/admin/plan%'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

-- BK-08 audit log page: keep baseline menu data repeatable and read-only.
INSERT INTO el_sys_menu
  (id, menu_type, parent_id, path, component, redirect, name, meta_title, meta_icon, sort, create_time)
SELECT '9900000000000000095', 1, '0', '/admin/audit', '#', NULL,
       'AdminAudit', '审计日志', 'Document', 18, NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_sys_menu WHERE path = '/admin/audit');

INSERT INTO el_sys_menu
  (id, menu_type, parent_id, path, component, redirect, name, meta_title, meta_icon,
   permission_tag, sort, create_time)
SELECT '9900000000000000096', 2,
       (SELECT id FROM el_sys_menu WHERE path = '/admin/audit'), '/admin/audit/log',
       'views/System/Audit/AuditLog', NULL, 'AdminAuditLog', '审计日志查看', NULL,
       'keryo:audit:view', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_sys_menu WHERE path = '/admin/audit/log');

UPDATE el_sys_menu
SET permission_tag = 'keryo:audit:view',
    component = 'views/System/Audit/AuditLog'
WHERE path = '/admin/audit/log';

INSERT INTO el_sys_role_menu (id, role_id, menu_id, data_flag)
SELECT MD5(CONCAT('baseline-audit-menu-', r.id, '-', m.id)), r.id, m.id, 1
FROM el_sys_role r
         CROSS JOIN el_sys_menu m
WHERE r.id IN ('admin', 'keryo_director', 'keryo_assistant', 'keryo_supervisor', 'keryo_head_nurse')
  AND m.path IN ('/admin/audit', '/admin/audit/log')
  AND NOT EXISTS (
    SELECT 1 FROM el_sys_role_menu rm
    WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- Synthetic exam fixture used by the public baseline smoke test.
INSERT INTO el_repo
  (id, title, cat_id, remark, create_time, update_time, create_by, update_by)
VALUES
  ('1910524655864012801', 'Baseline Question Bank', NULL,
   'Synthetic hemodialysis training questions for repeatable verification', NOW(), NOW(),
   '1000000000000000001', '1000000000000000001')
ON DUPLICATE KEY UPDATE title=VALUES(title), remark=VALUES(remark), update_time=NOW();

INSERT INTO el_exam
  (id, title, content, open_type, state, start_time, end_time, total_score,
   total_time, qualify_score, chance, hand_min, late_max, thanks, repo_id,
   create_time, update_time, create_by, update_by)
VALUES
  ('1915227067167539202', 'Baseline Demo Exam',
   'Synthetic exam fixture for local portfolio verification.', 1, 0,
   '2020-01-01 00:00:00', '2050-12-31 23:59:59', 180.00, 10, 90.00,
   0, 0, 0, NULL, '1910524655864012801', NOW(), NOW(),
   '1000000000000000001', '1000000000000000001')
ON DUPLICATE KEY UPDATE title=VALUES(title), content=VALUES(content),
  start_time=VALUES(start_time), end_time=VALUES(end_time), repo_id=VALUES(repo_id);

INSERT INTO el_exam_rule (id, exam_id, repo_id, qu_type, qu_count, qu_score)
VALUES
  ('baseline-rule-radio', '1915227067167539202', '1910524655864012801', 'radio', 5, 10),
  ('baseline-rule-multi', '1915227067167539202', '1910524655864012801', 'multi', 5, 10),
  ('baseline-rule-judge', '1915227067167539202', '1910524655864012801', 'judge', 5, 10),
  ('baseline-rule-fill', '1915227067167539202', '1910524655864012801', 'fill', 1, 10),
  ('baseline-rule-essay', '1915227067167539202', '1910524655864012801', 'essay', 1, 20)
ON DUPLICATE KEY UPDATE repo_id=VALUES(repo_id), qu_type=VALUES(qu_type),
  qu_count=VALUES(qu_count), qu_score=VALUES(qu_score);

UPDATE el_exam
SET score_visible=1, start_time='2020-01-01 00:00:00', end_time='2050-12-31 23:59:59'
WHERE id='1915227067167539202';

INSERT INTO el_keryo_training
  (id, title, file_url, file_type, file_size, dept_code, status, create_by, update_by)
VALUES
  ('baseline_training', 'Baseline Training', '/baseline/training.pdf', 'PDF', 1, NULL, 1, 'baseline', 'baseline');

INSERT INTO el_keryo_training_plan
  (id, title, content, credit, exam_id, courseware_id, dept_code, status,
   start_time, end_time, submit_by, submit_time, approve_by, approve_time,
   approve_remark, create_by, update_by)
SELECT
  'baseline_plan', 'Baseline Plan', 'Repeatable baseline flow', 0,
  '1915227067167539202', 'baseline_training', 'BASELINE_DEPT', 'ACTIVE',
  '2020-01-01 00:00:00', '2050-12-31 23:59:59',
  admin_user.id, NOW(), admin_user.id, NOW(), 'baseline', admin_user.id, admin_user.id
FROM el_sys_user admin_user WHERE admin_user.user_name='admin';

INSERT INTO el_keryo_plan_user (id, plan_id, user_id)
SELECT 'baseline_plan_student', 'baseline_plan', id FROM el_sys_user WHERE user_name='student';

INSERT INTO el_keryo_plan_user (id, plan_id, user_id)
SELECT 'baseline_plan_team_leader', 'baseline_plan', id FROM el_sys_user WHERE user_name='team_lead1'
  AND NOT EXISTS (SELECT 1 FROM el_keryo_plan_user WHERE id='baseline_plan_team_leader');

