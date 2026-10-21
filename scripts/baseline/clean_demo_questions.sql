SET NAMES utf8mb4;
SET collation_connection = 'utf8mb4_general_ci';

-- The boot dump contains historical mixed-topic questions. This cleanup is
-- executed only against the freshly-created isolated baseline database.
SET @repo_id = '1910524655864012801';

DELETE answer_rows
FROM el_repo_qu_answer answer_rows
JOIN el_repo_qu question_rows ON question_rows.id = answer_rows.qu_id
WHERE question_rows.repo_id = @repo_id;

DELETE FROM el_repo_qu
WHERE repo_id = @repo_id;

