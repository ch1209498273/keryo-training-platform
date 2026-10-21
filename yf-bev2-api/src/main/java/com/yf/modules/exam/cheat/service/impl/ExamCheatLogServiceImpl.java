package com.yf.modules.exam.cheat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.base.api.exception.ServiceException;
import com.yf.modules.exam.cheat.entity.ExamCheatLog;
import com.yf.modules.exam.cheat.mapper.ExamCheatLogMapper;
import com.yf.modules.exam.cheat.service.ExamCheatLogService;
import com.yf.modules.exam.paper.entity.Paper;
import com.yf.modules.exam.paper.mapper.PaperMapper;
import com.yf.system.modules.user.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 考试违规日志服务实现（T-05 防切屏）
 * </p>
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class ExamCheatLogServiceImpl extends ServiceImpl<ExamCheatLogMapper, ExamCheatLog> implements ExamCheatLogService {

    private final PaperMapper paperMapper;

    @Override
    public void record(String paperId, String examId, String userId, String quId,
                       String cheatType, String content, String ip) {

        try {
            // 由 paperId 推导 examId/userId
            if (paperId != null) {
                Paper paper = paperMapper.selectById(paperId);
                if (paper != null) {
                    if (examId == null) examId = paper.getExamId();
                    if (userId == null) userId = paper.getUserId();
                }
            }
            if (userId == null) {
                userId = UserUtils.getUserId();
            }
            if (ip == null) {
                ip = getClientIp();
            }

            ExamCheatLog log = new ExamCheatLog();
            log.setPaperId(paperId);
            log.setExamId(examId);
            log.setUserId(userId);
            log.setQuId(quId);
            log.setCheatType(cheatType);
            log.setContent(content);
            log.setIp(ip);
            log.setCreateTime(new Date());
            this.save(log);
        } catch (Exception e) {
            // 违规日志记录失败不影响答题主流程
            log.error("记录考试违规日志失败: {}", e.getMessage());
        }
    }

    @Override
    public List<ExamCheatLog> listByPaper(String paperId) {
        QueryWrapper<ExamCheatLog> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ExamCheatLog::getPaperId, paperId)
                .orderByAsc(ExamCheatLog::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public Paper checkPaperOwner(String paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new ServiceException("试卷不存在或已被删除！");
        }
        // 管理端角色放行（阅卷/复查）
        List<String> roles = UserUtils.getRoles();
        if (roles.contains("admin") || roles.contains("keryo_head_nurse") || roles.contains("keryo_assistant")) {
            return paper;
        }
        // 学员：只能操作自己的试卷
        String userId = UserUtils.getUserId();
        if (userId == null || !userId.equals(paper.getUserId())) {
            throw new ServiceException("无权操作他人试卷！");
        }
        return paper;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                } else {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

