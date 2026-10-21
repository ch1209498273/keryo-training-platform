package com.yf.modules.exam.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.utils.BeanMapper;
import com.yf.base.utils.DecimalUtils;
import com.keryo.training.common.security.SqlSafeUtil;
import com.yf.modules.exam.exam.dto.ExamDTO;
import com.yf.modules.exam.exam.dto.ExamRuleDTO;
import com.yf.modules.exam.exam.dto.request.ExamListReqDTO;
import com.yf.modules.exam.exam.dto.response.ExamDetailDTO;
import com.yf.modules.exam.exam.entity.Exam;
import com.yf.modules.exam.exam.mapper.ExamMapper;
import com.yf.modules.exam.exam.service.ExamRuleService;
import com.yf.modules.exam.exam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 考试业务实现类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-14 17:29
 */
@RequiredArgsConstructor
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    private final ExamRuleService examRuleService;

    @Override
    public IPage<ExamDTO> paging(PagingReqDTO<ExamListReqDTO> reqDTO) {
        // T-07.7：SQL 特殊字符过滤（防 jsqlparser 解析崩溃）
        if (reqDTO.getParams() != null && reqDTO.getParams().getTitle() != null) {
            reqDTO.getParams().setTitle(SqlSafeUtil.sanitize(reqDTO.getParams().getTitle()));
        }
        return baseMapper.paging(reqDTO.toPage(), reqDTO.getParams());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(ExamDetailDTO reqDTO) {
        // 保存基本信息
        Exam entity = new Exam();
        BeanMapper.copy(reqDTO, entity);

        // 组卷规则：按规则自动重算总分，避免前后端口径不一致（修复"满分与实际抽题总分不符"）
        if (reqDTO.getRuleList() != null && !reqDTO.getRuleList().isEmpty()) {
            BigDecimal total = DecimalUtils.zero();
            for (ExamRuleDTO rule : reqDTO.getRuleList()) {
                if (rule.getQuCount() != null && rule.getQuScore() != null) {
                    total = DecimalUtils.add(total,
                            DecimalUtils.multiply(BigDecimal.valueOf(rule.getQuCount()), rule.getQuScore()));
                }
            }
            entity.setTotalScore(total);
        }

        this.saveOrUpdate(entity);

        // 保存规则
        examRuleService.saveAll(entity.getId(), entity.getRepoId(), reqDTO.getRuleList());
    }

    @Override
    public void delete(List<String> ids) {
        //批量删除
        this.removeByIds(ids);

        // 移除规则
        examRuleService.deleteByExams(ids);
    }

    @Override
    public ExamDetailDTO detail(String id) {
        // 基本信息
        Exam entity = this.getById(id);
        ExamDetailDTO dto = new ExamDetailDTO();
        BeanMapper.copy(entity, dto);

        // 组卷规则
        List<ExamRuleDTO> ruleList = examRuleService.listByExam(id);
        dto.setRuleList(ruleList);
        return dto;
    }

    @Override
    public int findHandMin(String id) {

        QueryWrapper<Exam> wrapper = new QueryWrapper<>();
        wrapper.lambda().select(Exam::getHandMin).eq(Exam::getId, id);

        Exam entity = this.getOne(wrapper, false);
        if (entity != null && entity.getHandMin() != null && entity.getHandMin() > 0) {
            return entity.getHandMin();
        }

        return 0;
    }

    @Override
    public void setScoreVisible(String examId, Integer visible) {
        Exam exam = this.getById(examId);
        if (exam == null) {
            throw new com.yf.base.api.exception.ServiceException("考试不存在：" + examId);
        }
        exam.setScoreVisible(visible);
        this.updateById(exam);
        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("成绩公示切换 examId={} scoreVisible={}", examId, visible);
    }
}

