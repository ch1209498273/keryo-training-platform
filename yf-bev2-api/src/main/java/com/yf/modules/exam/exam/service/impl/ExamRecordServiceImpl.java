package com.yf.modules.exam.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.utils.DecimalUtils;
import com.keryo.training.common.security.SqlSafeUtil;
import com.yf.modules.exam.exam.dto.ExamRecordDTO;
import com.yf.modules.exam.exam.dto.request.ExamRecordListReqDTO;
import com.yf.modules.exam.exam.entity.ExamRecord;
import com.yf.modules.exam.exam.mapper.ExamRecordMapper;
import com.yf.ability.shiro.dto.SysUserLoginDTO;
import jakarta.annotation.Resource;
import java.util.stream.Collectors;
import com.yf.system.modules.depart.mapper.SysDepartMapper;
import com.yf.system.modules.depart.entity.SysDepart;
import com.yf.base.enums.DataScope;
import com.yf.modules.exam.exam.service.ExamRecordService;
import com.yf.system.modules.user.UserUtils;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 考试记录业务实现类
 * </p>
 *
 * @author 沉醉寒风
 * @since 2025-04-17 14:59
 */
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    /** 部门 Mapper（区域督导 area 过滤，权限 v2） */
    @Resource
    private com.yf.system.modules.depart.mapper.SysDepartMapper sysDepartMapper;

    /** 考试 Mapper（成绩修正查及格分） */
    @Resource
    private com.yf.modules.exam.exam.mapper.ExamMapper examMapper;

    @Override
    public IPage<ExamRecordDTO> paging(PagingReqDTO<ExamRecordListReqDTO> reqDTO) {
        ExamRecordListReqDTO params = reqDTO.getParams();
        if (params == null) {
            params = new ExamRecordListReqDTO();
            reqDTO.setParams(params);
        }
        // T-07.7：过滤 SQL 特殊字符（防 jsqlparser 解析崩溃/注入）
        if (params.getUserName() != null) {
            params.setUserName(SqlSafeUtil.sanitize(params.getUserName()));
        }
        if (params.getTitle() != null) {
            params.setTitle(SqlSafeUtil.sanitize(params.getTitle()));
        }
        this.applyDataScope(params);
        return baseMapper.paging(reqDTO.toPage(), params);
    }

    @Override
    public byte[] exportExcel(PagingReqDTO<ExamRecordListReqDTO> reqDTO) {
        ExamRecordListReqDTO params = reqDTO.getParams();
        if (params == null) {
            params = new ExamRecordListReqDTO();
        }
        // T-07.7：SQL 特殊字符过滤（防 jsqlparser 解析崩溃）
        if (params.getUserName() != null) {
            params.setUserName(SqlSafeUtil.sanitize(params.getUserName()));
        }
        if (params.getTitle() != null) {
            params.setTitle(SqlSafeUtil.sanitize(params.getTitle()));
        }
        this.applyDataScope(params);
        List<ExamRecordDTO> list = baseMapper.exportList(params);
        // Hutool 生成 Excel
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet("成绩导出");
        // 表头
        writer.addHeaderAlias("realName", "考生姓名");
        writer.addHeaderAlias("deptName", "科室");
        writer.addHeaderAlias("examTitle", "考试名称");
        writer.addHeaderAlias("tryCount", "考试次数");
        writer.addHeaderAlias("maxScore", "最高分");
        writer.addHeaderAlias("lastScore", "最近得分");
        writer.addHeaderAlias("passed", "是否通过");
        writer.addHeaderAlias("createTime", "交卷时间");
        writer.write(list, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.flush(out);
        writer.close();
        return out.toByteArray();
    }

    /**
     * 数据权限（权限模型 v2）：按 data_scope 统一判断
     * 4 全量不过滤 / 3 区域(area_code) / 2 本科室 / 1 本人
     */
    private void applyDataScope(ExamRecordListReqDTO params) {
        Integer scope = UserUtils.getDataScope();
        if (scope == null || scope == DataScope.SCOPE_ALL) {
            return; // 全量：不过滤
        }
        SysUserLoginDTO user = UserUtils.getUser(false);
        if (user == null) {
            return;
        }
        if (scope == DataScope.SCOPE_SELF) {
            params.setUserId(UserUtils.getUserId());
        } else if (scope == DataScope.SCOPE_DEPT) {
            params.setDeptCode(user.getDeptCode());
        } else if (scope == DataScope.SCOPE_DEPT_DOWN) {
            // 区域督导：area_code 下所有中心科室
            params.setDeptCodes(areaDeptCodes(user.getAreaCode()));
        }
    }

    /** 查询 area 下所有 dept_code（中心+科室） */
    private List<String> areaDeptCodes(String areaCode) {
        if (areaCode == null || areaCode.isBlank()) {
            return List.of();
        }
        return sysDepartMapper.selectList(
                new QueryWrapper<SysDepart>().eq("area_code", areaCode))
                .stream().map(SysDepart::getDeptCode).collect(Collectors.toList());
    }

    @Override
    public void editScore(String recordId, BigDecimal score, String reason) {
        ExamRecord record = this.getById(recordId);
        if (record == null) {
            throw new com.yf.base.api.exception.ServiceException("成绩记录不存在：" + recordId);
        }
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.yf.base.api.exception.ServiceException("分数不合法：" + score);
        }
        // 修正分数 + 重算通过状态（需查考试及格分）
        record.setMaxScore(score);
        record.setLastScore(score);
        record.setLocked(0); // 人工仲裁：解锁覆盖
        // 查考试及格分
        try {
            com.yf.modules.exam.exam.entity.Exam exam = examMapper.selectById(record.getExamId());
            if (exam != null && exam.getQualifyScore() != null) {
                record.setPassed(score.compareTo(exam.getQualifyScore()) >= 0);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("editScore 查及格分失败", e);
        }
        this.updateById(record);
        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("总监修正成绩 recordId={} score={} reason={}", recordId, score, reason);
    }

    @Override
    public void retryExam(String examId, String userId) {
        // 查询该学员该考试的记录
        QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId);

        ExamRecord record = this.getOne(wrapper, false);
        if (record == null) {
            throw new com.yf.base.api.exception.ServiceException("该学员尚未参加该考试，无需补考！");
        }

        // 重置已考次数为 0（保留历史成绩），学员即可再次进入考试
        record.setTryCount(0);
        this.updateById(record);

        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("管理端发起补考 examId={} userId={} recordId={}", examId, userId, record.getId());
    }

    @Override
    public String joinRecord(String examId, String userId, String paperId, BigDecimal score, Boolean passed) {
        //查询条件
        QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(ExamRecord::getId, ExamRecord::getMaxScore, ExamRecord::getTryCount)
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId);

        ExamRecord record = this.getOne(wrapper, false);

        if (record == null) {
            record = new ExamRecord();
            record.setUserId(userId);
            record.setExamId(examId);
            record.setPaperId(paperId);
            record.setMaxScore(score);
            record.setLastScore(score);
            record.setPassed(passed);
            record.setTryCount(1);
            this.save(record);
            // 首次交卷：返回记录ID（学分发放锚点，幂等）
            return record.getId();
        } else {

            // 更新最高分数
            BigDecimal maxScore = record.getMaxScore();
            if (DecimalUtils.gt(score, maxScore)) {
                record.setMaxScore(score);
                record.setPassed(passed);
                record.setPaperId(paperId);
            }
            record.setTryCount(record.getTryCount() + 1);
            record.setLastScore(score);
            this.updateById(record);
            return null; // 重复交卷：不重复发放学分
        }
    }

    @Override
    public int findTryCount(String examId, String userId) {

        //查询条件
        QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(ExamRecord::getId, ExamRecord::getTryCount)
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId);

        ExamRecord record = this.getOne(wrapper, false);
        if (record != null) {
            return record.getTryCount();
        }

        return 0;
    }

    @Override
    public IPage<ExamRecordDTO> clientPaging(PagingReqDTO<ExamRecordListReqDTO> reqDTO) {
        return baseMapper.clientPaging(reqDTO.toPage(), reqDTO.getParams());
    }

}

