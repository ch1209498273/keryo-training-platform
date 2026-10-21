-- Live demo maintenance: link the demo plan to an existing real training file
-- and the existing demo exam.
SET NAMES utf8mb4;
SET collation_connection = 'utf8mb4_general_ci';

UPDATE el_keryo_training_plan
SET courseware_id = '2084243871821561858',
    exam_id = '1915227067167539202',
    update_time = NOW()
WHERE id = 't13_demo_plan';

SELECT id, title, courseware_id, exam_id, status
FROM el_keryo_training_plan
WHERE id = 't13_demo_plan';

