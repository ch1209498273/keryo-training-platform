package com.yf.modules.exam.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.modules.exam.exam.dto.ExamRecordDTO;
import com.yf.modules.exam.exam.dto.request.ExamRecordListReqDTO;
import com.yf.modules.exam.exam.entity.ExamRecord;

import java.math.BigDecimal;

/**
 * <p>
 * 考试记录业务接口类
 * </p>
 *
 * @author 沉醉寒风
 * @since 2025-04-17 14:59
 */
public interface ExamRecordService extends IService<ExamRecord> {

    /**
     * 分页查询数据
     *
     * @param reqDTO
     * @return
     */
    IPage<ExamRecordDTO> paging(PagingReqDTO<ExamRecordListReqDTO> reqDTO);

    /**
     * 加入考试记录
     *
     * @param examId
     * @param userId
     * @param paperId
     * @param score
     * @param passed
     * @return 新成绩记录ID（首次交卷返回，重复交卷返回 null，用于学分发放幂等）
     */
    String joinRecord(String examId, String userId, String paperId, BigDecimal score, Boolean passed);

    /**
     * 查找考试次数
     *
     * @param examId
     * @param userId
     * @return
     */
    int findTryCount(String examId, String userId);

    /**
     * 学员端考试记录列表
     *
     * @param reqDTO
     * @return
     */
    IPage<ExamRecordDTO> clientPaging(PagingReqDTO<ExamRecordListReqDTO> reqDTO);

    /**
     * 成绩导出 Excel（复用数据权限过滤）
     *
     * @param reqDTO
     * @return xlsx 字节数组
     */
    byte[] exportExcel(PagingReqDTO<ExamRecordListReqDTO> reqDTO);

    /**
     * 成绩修正（权限模型 v2：仅医护总监，人工仲裁）
     *
     * @param recordId 成绩记录ID
     * @param score    修正后分数
     * @param reason   修正原因（写审计）
     */
    void editScore(String recordId, BigDecimal score, String reason);

    /**
     * 补考（管理端发起）：重置该学员该考试的已考次数，允许再次进入考试
     * 保留历史成绩记录，补考交卷后按新成绩更新最近分/通过状态，且不重复发放学分
     *
     * @param examId 考试ID
     * @param userId 学员ID
     */
    void retryExam(String examId, String userId);
}

