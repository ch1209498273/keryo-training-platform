package com.yf.modules.exam.cheat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yf.modules.exam.cheat.entity.ExamCheatLog;
import com.yf.modules.exam.paper.entity.Paper;

import java.util.List;

/**
 * <p>
 * 考试违规日志服务（T-05 防切屏）
 * </p>
 */
public interface ExamCheatLogService extends IService<ExamCheatLog> {

    /**
     * 记录一条违规日志（自动补 examId/userId/IP）
     *
     * @param paperId   试卷ID（可为空）
     * @param examId    考试ID（可为空）
     * @param userId    用户ID（可为空）
     * @param quId      题目ID（可为空）
     * @param cheatType 违规类型
     * @param content   详情
     * @param ip        IP（可为空，自动取请求IP）
     */
    void record(String paperId, String examId, String userId, String quId,
                String cheatType, String content, String ip);

    /**
     * 统计某试卷的违规记录
     *
     * @param paperId 试卷ID
     * @return 违规日志列表
     */
    List<ExamCheatLog> listByPaper(String paperId);

    /**
     * 校验试卷归属（IDOR 防护）：学员只能操作自己的试卷，管理端放行
     *
     * @param paperId 试卷ID
     * @return 试卷实体
     */
    Paper checkPaperOwner(String paperId);
}

