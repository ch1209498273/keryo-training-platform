-- Public synthetic question fixture for the isolated baseline database.
-- No customer question bank or uploaded training material is included.
-- ============================================================
-- 重建演示考试题目（被 T05 清理误删，2026-08-07）
-- 演示题库 1910524655864012801，5radio+5multi+5judge+1fill+1essay
-- 状态直接 APPROVED（T-05 时代的题目，已通过审核）
-- ============================================================
SET @repo_id = '1910524655864012801';
SET @admin_id = '1000000000000000001';

-- Deactivate historical mixed-topic questions without deleting rows that may
-- already be referenced by papers or answer records. Only the t05 questions
-- below remain eligible for exam assembly because the service selects
-- APPROVED questions.
UPDATE el_repo_qu
SET review_status = 'REJECTED',
    review_remark = 'BK-03: legacy non-hemodialysis demo question',
    update_by = @admin_id,
    update_time = NOW()
WHERE repo_id = @repo_id
  AND id NOT LIKE 't05%';

-- Deactivate historical mixed-topic questions without deleting rows that may
-- already be referenced by papers or answer records. Only the t05 questions
-- below remain eligible for exam assembly because the service selects
-- APPROVED questions.
UPDATE el_repo_qu
SET review_status = 'REJECTED',
    review_remark = 'BK-03: legacy non-hemodialysis demo question',
    update_by = @admin_id,
    update_time = NOW()
WHERE repo_id = @repo_id
  AND id NOT LIKE 't05%';

-- 单选题（5）
INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05radio1', @repo_id, 'radio', '血液透析的英文缩写是？', 'Hemodialysis 缩写为 HD', '简单', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05radio1');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio1a', 't05radio1', 1, 'HD'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio1a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio1b', 't05radio1', 0, 'CRRT'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio1b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio1c', 't05radio1', 0, 'PD'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio1c');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio1d', 't05radio1', 0, 'HF'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio1d');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05radio2', @repo_id, 'radio', '透析干体重评估最常用的指标是？', '干体重=透析后无水肿且血压正常时的体重', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05radio2');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio2a', 't05radio2', 1, '透析后体重'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio2a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio2b', 't05radio2', 0, '透前体重'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio2b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio2c', 't05radio2', 0, '理想体重'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio2c');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05radio3', @repo_id, 'radio', '血透患者内瘘穿刺首选部位是？', '首选自体动静脉内瘘', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05radio3');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio3a', 't05radio3', 1, '自体动静脉内瘘'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio3a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio3b', 't05radio3', 0, '中心静脉导管'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio3b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio3c', 't05radio3', 0, '人工血管'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio3c');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05radio4', @repo_id, 'radio', '透析中低血压最常见的原因是？', '超滤过快导致有效血容量不足', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05radio4');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio4a', 't05radio4', 1, '超滤过快'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio4a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio4b', 't05radio4', 0, '透析液温度过高'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio4b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio4c', 't05radio4', 0, '血流量过慢'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio4c');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05radio5', @repo_id, 'radio', 'Kt/V 是评估什么的指标？', 'Kt/V 是透析充分性指标', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05radio5');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio5a', 't05radio5', 1, '透析充分性'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio5a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio5b', 't05radio5', 0, '营养状况'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio5b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05radio5c', 't05radio5', 0, '贫血程度'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05radio5c');

-- 多选题（5，每题2正确选项）
INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05multi1', @repo_id, 'multi', '血液透析的适应证包括？', '慢性肾衰竭、急性肾损伤、药物中毒等', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05multi1');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi1a', 't05multi1', 1, '慢性肾衰竭'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi1a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi1b', 't05multi1', 1, '急性肾损伤'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi1b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi1c', 't05multi1', 0, '单纯高血压'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi1c');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi1d', 't05multi1', 0, '普通感冒'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi1d');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05multi2', @repo_id, 'multi', '内瘘成熟的标准包括？', '震颤明显、血管直径足够、血流量充足', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05multi2');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi2a', 't05multi2', 1, '震颤明显'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi2a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi2b', 't05multi2', 1, '血流量充足'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi2b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi2c', 't05multi2', 0, '皮肤发红'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi2c');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi2d', 't05multi2', 0, '疼痛明显'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi2d');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05multi3', @repo_id, 'multi', '透析中抗凝监测的指标有？', 'APTT、ACT、肝素浓度', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05multi3');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi3a', 't05multi3', 1, 'APTT'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi3a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi3b', 't05multi3', 1, 'ACT'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi3b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi3c', 't05multi3', 0, '血糖'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi3c');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi3d', 't05multi3', 0, '血脂'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi3d');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05multi4', @repo_id, 'multi', '透析中预防肌肉痉挛的措施有？', '控制超滤量、补充钙剂、适当运动', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05multi4');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi4a', 't05multi4', 1, '控制超滤量'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi4a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi4b', 't05multi4', 1, '补充钙剂'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi4b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi4c', 't05multi4', 0, '减少饮水量'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi4c');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi4d', 't05multi4', 0, '增加血流量'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi4d');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05multi5', @repo_id, 'multi', '透析充分性评估的指标包括？', 'Kt/V、URR、TACurea', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05multi5');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi5a', 't05multi5', 1, 'Kt/V'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi5a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi5b', 't05multi5', 1, 'URR'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi5b');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi5c', 't05multi5', 0, '血压'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi5c');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05multi5d', 't05multi5', 0, '心率'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05multi5d');

-- 判断题（5）
INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05judge1', @repo_id, 'judge', '血液透析患者的干体重应保持固定不变。', '干体重随病情变化需定期调整', '简单', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05judge1');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge1a', 't05judge1', 0, '正确'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge1a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge1b', 't05judge1', 1, '错误'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge1b');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05judge2', @repo_id, 'judge', '内瘘术后应避免在术侧肢体测血压。', '术侧测血压影响内瘘血流', '简单', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05judge2');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge2a', 't05judge2', 1, '正确'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge2a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge2b', 't05judge2', 0, '错误'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge2b');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05judge3', @repo_id, 'judge', '透析液电导度越高，透析效果越好。', '电导度过高易致高钠血症', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05judge3');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge3a', 't05judge3', 0, '正确'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge3a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge3b', 't05judge3', 1, '错误'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge3b');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05judge4', @repo_id, 'judge', '透析中进食会增加低血压风险。', '进食致血流重分布', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05judge4');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge4a', 't05judge4', 1, '正确'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge4a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge4b', 't05judge4', 0, '错误'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge4b');

INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05judge5', @repo_id, 'judge', '透析患者应限制高钾食物的摄入。', '高钾可致心律失常', '简单', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05judge5');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge5a', 't05judge5', 1, '正确'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge5a');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05judge5b', 't05judge5', 0, '错误'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05judge5b');

-- 填空题（1）
INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05fill1', @repo_id, 'fill', '透析充分性指标 Kt/V 的推荐目标值是____（血液透析每周3次）。', 'KDIGO 指南推荐 Kt/V≥1.2', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05fill1');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05fill1a', 't05fill1', 1, '1.2'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05fill1a');

-- 简答题（1）
INSERT INTO el_repo_qu (id, repo_id, qu_type, content, analysis, difficulty_level, review_status, create_by, create_time, update_time)
SELECT 't05essay1', @repo_id, 'essay', '简述血液透析患者内瘘的日常自我护理要点。', '参考：保持清洁、每日触摸震颤、避免受压、避免术侧测血压/提重物、异常及时就诊', '中等', 'APPROVED', @admin_id, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu WHERE id='t05essay1');
INSERT INTO el_repo_qu_answer (id, qu_id, is_right, content)
SELECT 't05essay1a', 't05essay1', 1, '保持穿刺部位清洁干燥；每日触摸内瘘震颤；避免内瘘侧肢体受压、测血压、提重物；穿刺后压迫止血时间适当；发现震颤减弱或红肿及时就诊。'
WHERE NOT EXISTS (SELECT 1 FROM el_repo_qu_answer WHERE id='t05essay1a');

-- 验证
SELECT '-- 重建后题型分布 --' AS info;
SELECT qu_type, COUNT(*) AS cnt FROM el_repo_qu WHERE repo_id=@repo_id AND review_status='APPROVED' GROUP BY qu_type;


