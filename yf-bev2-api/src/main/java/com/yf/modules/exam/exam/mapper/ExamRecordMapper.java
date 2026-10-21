package com.yf.modules.exam.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yf.modules.exam.exam.dto.ExamRecordDTO;
import com.yf.modules.exam.exam.dto.request.ExamRecordListReqDTO;
import com.yf.modules.exam.exam.entity.ExamRecord;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 考试记录Mapper
 * </p>
 *
 * @author 沉醉寒风
 * @since 2025-04-17 14:59
 */
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 学员端考试记录列表
     *
     * @param page
     * @param query
     * @return
     */
    IPage<ExamRecordDTO> clientPaging(IPage<?> page, @Param("query") ExamRecordListReqDTO query);

    /**
     * 管理端考试列表
     *
     * @param page
     * @param query
     * @return
     */
    IPage<ExamRecordDTO> paging(IPage<?> page, @Param("query") ExamRecordListReqDTO query);

    /**
     * 成绩导出（联查姓名/科室/考试名，含数据权限过滤）
     *
     * @param query
     * @return
     */
    java.util.List<ExamRecordDTO> exportList(@Param("query") ExamRecordListReqDTO query);
}

