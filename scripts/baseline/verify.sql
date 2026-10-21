SET NAMES utf8mb4;
SET collation_connection = 'utf8mb4_general_ci';

SELECT IF(COUNT(*) >= 40, 'PASS', 'FAIL') AS table_count_check
FROM information_schema.tables WHERE table_schema=DATABASE();

SELECT IF(COUNT(*)=6, 'PASS', 'FAIL') AS extension_tables_check
FROM information_schema.tables
WHERE table_schema=DATABASE()
  AND table_name IN ('el_keryo_training','el_keryo_training_plan','el_keryo_plan_user',
                     'el_keryo_audit_log','el_keryo_ai_config','el_keryo_forget_pass');

SELECT IF(COUNT(*)=6, 'PASS', 'FAIL') AS retention_archive_tables_check
FROM information_schema.tables
WHERE table_schema=DATABASE()
  AND table_name IN ('el_keryo_audit_log_archive','el_exam_record_archive','el_paper_archive',
                     'el_paper_qu_archive','el_paper_qu_answer_archive','el_exam_cheat_log_archive');

SELECT IF(COUNT(*)=3, 'PASS', 'FAIL') AS account_check
FROM el_sys_user WHERE user_name IN ('admin','nurse_head','student') AND state=0;

SELECT IF(COUNT(*)=1, 'PASS', 'FAIL') AS team_leader_account_check
FROM el_sys_user WHERE user_name='team_lead1' AND state=0;

SELECT IF(COUNT(*)=3, 'PASS', 'FAIL') AS password_policy_account_check
FROM el_sys_user
WHERE user_name IN ('admin','nurse_head','student')
  AND last_password_time IS NOT NULL;

SELECT IF(COUNT(*) >= 3, 'PASS', 'FAIL') AS password_history_check
FROM el_sys_user_password_history h
JOIN el_sys_user u ON u.id=h.user_id
WHERE u.user_name IN ('admin','nurse_head','student');

SELECT IF(COUNT(*)=2, 'PASS', 'FAIL') AS trigger_check
FROM information_schema.triggers
WHERE trigger_schema=DATABASE()
  AND trigger_name IN ('trg_exam_record_locked','trg_audit_log_no_delete');

SELECT IF(COUNT(*)=0, 'PASS', 'FAIL') AS plan_reference_check
FROM el_keryo_training_plan p
LEFT JOIN el_exam e ON e.id=p.exam_id
LEFT JOIN el_keryo_training t ON t.id=p.courseware_id
WHERE (p.exam_id IS NOT NULL AND e.id IS NULL)
   OR (p.courseware_id IS NOT NULL AND t.id IS NULL);

SELECT IF(COUNT(*)=0, 'PASS', 'FAIL') AS participant_reference_check
FROM el_keryo_plan_user pu
LEFT JOIN el_keryo_training_plan p ON p.id=pu.plan_id
LEFT JOIN el_sys_user u ON u.id=pu.user_id
WHERE p.id IS NULL OR u.id IS NULL;

SELECT IF(COUNT(*) >= 1, 'PASS', 'FAIL') AS team_leader_plan_participation_check
FROM el_keryo_plan_user pu
JOIN el_sys_user u ON u.id=pu.user_id
JOIN el_keryo_training_plan p ON p.id=pu.plan_id
WHERE u.user_name='team_lead1' AND p.dept_code='BASELINE_DEPT';

SELECT IF(
  COUNT(*) = 17
  AND SUM(id LIKE 't05%') = 17
  AND SUM(qu_type = 'radio') = 5
  AND SUM(qu_type = 'multi') = 5
  AND SUM(qu_type = 'judge') = 5
  AND SUM(qu_type = 'fill') = 1
  AND SUM(qu_type = 'essay') = 1
  AND SUM(content REGEXP '驾驶|牛奶|维生素|三角形|漠河|机动车') = 0,
  'PASS', 'FAIL'
) AS demo_question_bank_check
FROM el_repo_qu
WHERE repo_id='1910524655864012801';

