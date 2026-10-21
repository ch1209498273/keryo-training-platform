package com.keryo.training.modules.retention.service.impl;

import com.keryo.training.modules.retention.service.RetentionService;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.user.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BK-16 retention archive implementation.
 *
 * The source tables are never deleted here. INSERT IGNORE makes a repeated run
 * idempotent by using the source row id as the archive primary key.
 */
@Service
@RequiredArgsConstructor
public class RetentionServiceImpl implements RetentionService {

    private static final String AUDIT_LOG = "auditLog";
    private static final String EXAM_RECORD = "examRecord";
    private static final String PAPER = "paper";
    private static final String PAPER_QU = "paperQuestion";
    private static final String PAPER_ANSWER = "paperAnswer";
    private static final String CHEAT_LOG = "cheatLog";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Long> preview() {
        requireRetentionOperator();
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> result = new LinkedHashMap<>();
        result.put(AUDIT_LOG, count("SELECT COUNT(*) FROM el_keryo_audit_log WHERE create_time < ?",
                now.minusYears(1)));
        result.put(EXAM_RECORD, count("SELECT COUNT(*) FROM el_exam_record WHERE create_time < ?",
                now.minusYears(3)));
        Timestamp paperCutoff = timestamp(now.minusYears(1));
        result.put(PAPER, count("SELECT COUNT(*) FROM el_paper WHERE create_time < ?", paperCutoff));
        result.put(PAPER_QU, count("SELECT COUNT(*) FROM el_paper_qu q "
                + "JOIN el_paper p ON p.id=q.paper_id WHERE p.create_time < ?", paperCutoff));
        result.put(PAPER_ANSWER, count("SELECT COUNT(*) FROM el_paper_qu_answer a "
                + "JOIN el_paper p ON p.id=a.paper_id WHERE p.create_time < ?", paperCutoff));
        result.put(CHEAT_LOG, count("SELECT COUNT(*) FROM el_exam_cheat_log c "
                + "JOIN el_paper p ON p.id=c.paper_id WHERE p.create_time < ?", paperCutoff));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Long> archive() {
        requireRetentionOperator();
        LocalDateTime now = LocalDateTime.now();
        Timestamp auditCutoff = timestamp(now.minusYears(1));
        Timestamp recordCutoff = timestamp(now.minusYears(3));
        Timestamp paperCutoff = timestamp(now.minusYears(1));
        Map<String, Long> result = new LinkedHashMap<>();

        result.put(AUDIT_LOG, update("INSERT IGNORE INTO el_keryo_audit_log_archive "
                + "(id,user_id,user_name,real_name,module,operation,method,url,ip,params,result,error_msg,cost_time,create_time) "
                + "SELECT id,user_id,user_name,real_name,module,operation,method,url,ip,params,result,error_msg,cost_time,create_time "
                + "FROM el_keryo_audit_log WHERE create_time < ?", auditCutoff));
        result.put(EXAM_RECORD, update("INSERT IGNORE INTO el_exam_record_archive "
                + "(id,user_id,exam_id,paper_id,try_count,max_score,last_score,passed,locked,create_time,update_time) "
                + "SELECT id,user_id,exam_id,paper_id,try_count,max_score,last_score,passed,locked,create_time,update_time "
                + "FROM el_exam_record WHERE create_time < ?", recordCutoff));
        result.put(PAPER, update("INSERT IGNORE INTO el_paper_archive "
                + "(id,user_id,exam_id,title,total_time,user_time,total_score,qualify_score,user_score,limit_time,hand_time,hand_state,exam_ip,session_secret,last_answer_time,passed,create_time,update_time,create_by,update_by) "
                + "SELECT id,user_id,exam_id,title,total_time,user_time,total_score,qualify_score,user_score,limit_time,hand_time,hand_state,exam_ip,session_secret,last_answer_time,passed,create_time,update_time,create_by,update_by "
                + "FROM el_paper WHERE create_time < ?", paperCutoff));
        result.put(PAPER_QU, update("INSERT IGNORE INTO el_paper_qu_archive "
                + "(id,paper_id,qu_id,qu_type,answered,mark,sort,score,actual_score,answer_text,answer_hash,is_right) "
                + "SELECT q.id,q.paper_id,q.qu_id,q.qu_type,q.answered,q.mark,q.sort,q.score,q.actual_score,q.answer_text,q.answer_hash,q.is_right "
                + "FROM el_paper_qu q JOIN el_paper p ON p.id=q.paper_id WHERE p.create_time < ?", paperCutoff));
        result.put(PAPER_ANSWER, update("INSERT IGNORE INTO el_paper_qu_answer_archive "
                + "(id,paper_id,answer_id,qu_id,is_right,answer,checked,sort,abc) "
                + "SELECT a.id,a.paper_id,a.answer_id,a.qu_id,a.is_right,a.answer,a.checked,a.sort,a.abc "
                + "FROM el_paper_qu_answer a JOIN el_paper p ON p.id=a.paper_id WHERE p.create_time < ?", paperCutoff));
        result.put(CHEAT_LOG, update("INSERT IGNORE INTO el_exam_cheat_log_archive "
                + "(id,paper_id,exam_id,user_id,qu_id,cheat_type,content,ip,create_time) "
                + "SELECT c.id,c.paper_id,c.exam_id,c.user_id,c.qu_id,c.cheat_type,c.content,c.ip,c.create_time "
                + "FROM el_exam_cheat_log c JOIN el_paper p ON p.id=c.paper_id WHERE p.create_time < ?", paperCutoff));
        return result;
    }

    private long count(String sql, Object parameter) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, parameter);
        return value == null ? 0L : value;
    }

    private long update(String sql, Object parameter) {
        return jdbcTemplate.update(sql, parameter);
    }

    private Timestamp timestamp(LocalDateTime value) {
        return Timestamp.valueOf(value);
    }

    private void requireRetentionOperator() {
        List<String> roles = UserUtils.getRoles();
        boolean allowed = roles != null && (roles.contains("admin")
                || roles.contains("keryo_director")
                || roles.contains("keryo_assistant"));
        if (!allowed) {
            throw new ServiceException("Retention archive requires an administrator or training manager");
        }
    }
}

