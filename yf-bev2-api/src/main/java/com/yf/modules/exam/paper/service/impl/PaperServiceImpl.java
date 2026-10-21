package com.yf.modules.exam.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yf.ability.task.enums.JobGroup;
import com.yf.ability.task.service.JobService;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.BeanMapper;
import com.yf.base.utils.CronUtils;
import com.yf.base.utils.DecimalUtils;
import com.yf.base.utils.jackson.JsonHelper;
import com.yf.modules.exam.exam.dto.ExamRuleDTO;
import com.yf.modules.exam.exam.entity.Exam;
import com.yf.modules.exam.exam.service.ExamRecordService;
import com.yf.modules.exam.exam.service.ExamRuleService;
import com.yf.modules.exam.exam.service.ExamService;
import com.yf.modules.exam.jobs.HandPaperJob;
import com.yf.modules.exam.paper.dto.PaperDTO;
import com.yf.modules.exam.paper.dto.response.PaperCheckRespDTO;
import com.yf.modules.exam.paper.dto.response.PaperDetailRespDTO;
import com.yf.modules.exam.paper.dto.response.PaperRealTimeRespDTO;
import com.yf.modules.exam.paper.entity.Paper;
import com.yf.modules.exam.paper.mapper.PaperMapper;
import com.yf.modules.exam.paper.service.PaperQuService;
import com.yf.modules.exam.paper.service.PaperService;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import com.yf.modules.exam.repo.service.RepoQuService;
import com.keryo.training.modules.trainingplan.entity.PlanUser;
import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.keryo.training.modules.trainingplan.mapper.PlanUserMapper;
import com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper;
import com.yf.system.modules.user.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/**
 * <p>
 * 试卷业务实现类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-14 17:40
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    private final ExamService examService;
    private final ExamRuleService examRuleService;
    private final RepoQuService repoQuService;
    private final PaperQuService paperQuService;
    private final ExamRecordService examRecordService;
    private final JobService jobService;
    private final TrainingPlanMapper trainingPlanMapper;
    private final PlanUserMapper planUserMapper;

    @Override
    public IPage<PaperDTO> paging(PagingReqDTO<PaperDTO> reqDTO) {

        //查询条件
        QueryWrapper<Paper> wrapper = new QueryWrapper<>();

        // 请求参数
        PaperDTO params = reqDTO.getParams();

        if (params!=null) {
            if (StringUtils.isNotBlank(params.getExamId())) {
                wrapper.lambda().eq(Paper::getExamId, params.getExamId());
            }
            if (StringUtils.isNotBlank(params.getUserId()) && isManagementRole()) {
                wrapper.lambda().eq(Paper::getUserId, params.getUserId());
            }
        }

        // 学员分页只能查看自己的试卷；管理端才允许按用户筛选。
        if (!isManagementRole()) {
            wrapper.lambda().eq(Paper::getUserId, UserUtils.getUserId());
        }

        // 考试时间倒序
        wrapper.lambda().orderByDesc(Paper::getCreateTime);

        //获得数据
        IPage<Paper> page = this.page(reqDTO.toPage(), wrapper);
        //转换结果
        return JsonHelper.parseObject(page, new TypeReference<Page<PaperDTO>>() {});
    }


    @Override
    public PaperDTO detail(String id) {
        // IDOR 防护：校验试卷归属
        this.checkPaperOwner(id);
        Paper entity = this.getById(id);
        PaperDTO dto = new PaperDTO();
        BeanMapper.copy(entity, dto);

        // 成绩公示控制（T-15）：考试未公示(score_visible!=1)时，学员端不返回分数/是否通过
        if (entity.getExamId() != null) {
            Exam exam = examService.getById(entity.getExamId());
            if (exam != null && !Integer.valueOf(1).equals(exam.getScoreVisible())) {
                dto.setUserScore(null);
                dto.setPassed(null);
            }
        }
        return dto;
    }


    @Override
    public PaperCheckRespDTO preCheck(String examId, String userId) {


        PaperCheckRespDTO respDTO = new PaperCheckRespDTO();
        respDTO.setValidated(false);


        // 查找考试基本信息
        Exam exam = examService.getById(examId);
        if (exam == null) {
            respDTO.setMessage("考试不存在或已被删除！");
            return respDTO;
        }

        // 计划资格是考试入口的强制条件，不能只依赖前端列表过滤。
        if (!this.isPlanParticipant(examId, userId)) {
            respDTO.setMessage("您尚未被分配到该培训计划，无法参加考试！");
            return respDTO;
        }

        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            respDTO.setMessage("考试时间尚未配置，暂不能进入考试！");
            return respDTO;
        }

        if (exam.getStartTime().after(new Date())) {
            respDTO.setMessage("考试尚未开始，请耐心等待！");
            return respDTO;
        }

        if (exam.getEndTime().before(new Date())) {
            respDTO.setMessage("来迟一步，考试已结束！");
            return respDTO;
        }

        // 进行中的
        String paperId = this.findProcess(examId, userId);
        if (StringUtils.isNotBlank(paperId)) {
            respDTO.setMessage("有正在进行中的考试！");
            respDTO.setPaperId(paperId);
            return respDTO;
        }

        // 校验迟到
        if (exam.getLateMax() != null && exam.getLateMax() > 0) {
            Calendar cl = Calendar.getInstance();
            cl.setTime(exam.getStartTime());
            cl.add(Calendar.MINUTE, exam.getLateMax());

            if (cl.getTime().before(new Date())) {
                respDTO.setMessage(String.format("迟到超过%s分钟，无法进入考试！", exam.getLateMax()));
                return respDTO;
            }
        }

        // 考试机会校验
        if (exam.getChance() != null && exam.getChance() > 0) {
            int tryCount = examRecordService.findTryCount(examId, userId);
            if (exam.getChance() <= tryCount) {
                respDTO.setMessage(String.format("考试机会已用完，最多允许考试%s次！", exam.getChance()));
                return respDTO;
            }
        }

        respDTO.setValidated(true);
        return respDTO;

    }

    private boolean isPlanParticipant(String examId, String userId) {
        if (StringUtils.isBlank(examId) || StringUtils.isBlank(userId)) {
            return false;
        }
        List<TrainingPlan> plans = trainingPlanMapper.selectList(
                new QueryWrapper<TrainingPlan>().select("id")
                        .eq("exam_id", examId)
                        .eq("status", "ACTIVE"));
        if (CollectionUtils.isEmpty(plans)) {
            return false;
        }
        List<String> planIds = new ArrayList<>();
        for (TrainingPlan plan : plans) {
            planIds.add(plan.getId());
        }
        Long count = planUserMapper.selectCount(
                new QueryWrapper<PlanUser>().eq("user_id", userId).in("plan_id", planIds));
        return count != null && count > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createPaper(String examId, String userId) {

        // 校验
        PaperCheckRespDTO checkDTO = this.preCheck(examId, userId);
        if (Boolean.FALSE.equals(checkDTO.getValidated())) {
            throw new ServiceException(checkDTO.getMessage());
        }

        // 做基础校验
        Exam exam = examService.getById(examId);

        // 复制数据
        Paper paper = new Paper();
        paper.setTitle(exam.getTitle());
        paper.setExamId(examId);
        paper.setUserId(userId);
        paper.setTotalScore(exam.getTotalScore());
        paper.setQualifyScore(exam.getQualifyScore());
        paper.setUserScore(DecimalUtils.zero());
        paper.setUserTime(0);
        // 安全检测字段：M7 会话HMAC密钥 + IP_CHANGE 首访IP
        paper.setSessionSecret(UUID.randomUUID().toString().replace("-", ""));
        paper.setExamIp(getClientIp());

        // 计算交卷时间
        Integer totalTime = exam.getTotalTime();
        paper.setTotalTime(totalTime);
        if (totalTime != null && totalTime > 0) {
            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(System.currentTimeMillis());
            cl.add(Calendar.MINUTE, totalTime);
            Date limitTime = cl.getTime();

            // 整个考试的时间
            if (limitTime.before(exam.getEndTime())) {
                paper.setLimitTime(limitTime);
            } else {
                paper.setLimitTime(exam.getEndTime());
            }

        } else {
            paper.setLimitTime(exam.getEndTime());
        }

        // 增加定时任务
        this.save(paper);

        // 构建随机题目
        List<ExamRuleDTO> ruleList = examRuleService.listByExam(examId);

        if (CollectionUtils.isEmpty(ruleList)) {
            throw new ServiceException("考试进入失败，没有组卷规则！");
        }

        // 循环保存
        int sort = 1;
        for (ExamRuleDTO rule : ruleList) {
            // 未抽题的
            if (rule.getQuCount() == null || rule.getQuCount() == 0) {
                continue;
            }
            List<RepoQuDetailDTO> quList = repoQuService.listForPaper(rule.getRepoId(), rule.getQuType(), rule.getQuCount());
            paperQuService.saveToPaper(paper.getId(), rule.getQuScore(), quList, sort);

            // 序号增加
            sort+=quList.size();
        }


        // 到期执行任务
        String paperId = paper.getId();
        // 执行阅卷或完成
        String jobName = "force:hand:paper:" + paperId;
        jobService.addCronJob(HandPaperJob.class, jobName, JobGroup.SYSTEM, CronUtils.dateToCron(paper.getLimitTime()), paperId);

        return paperId;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handPaper(String paperId) {

        // IDOR 防护：只能交自己的试卷
        this.checkPaperOwner(paperId);
        Paper paper = this.getById(paperId);

        if (paper == null) {
            throw new ServiceException("试卷不存在或已被删除！");
        }

        if (paper.getHandState().equals(1)) {
            return;
        }

        // 统计分数
        BigDecimal userScore = paperQuService.sumTotalScore(paperId);
        paper.setUserScore(userScore);
        paper.setHandState(1);
        paper.setHandTime(new Date());
        // 考试用时
        long useTime = (System.currentTimeMillis() - paper.getCreateTime().getTime()) / 1000 / 60;
        paper.setUserTime((int) useTime);

        // 最低交卷时间校验
        int handMin = examService.findHandMin(paper.getExamId());
        if (handMin > 0 && useTime < handMin) {
            throw new ServiceException(String.format("请至少作答%s分钟后再交卷！", handMin));
        }

        // 是否合格
        boolean passed = DecimalUtils.ge(userScore, paper.getQualifyScore());
        paper.setPassed(passed);
        this.updateById(paper);

        // 汇总表
        examRecordService.joinRecord(paper.getExamId(), paper.getUserId(), paperId, userScore, passed);

    }

    @Override
    public PaperRealTimeRespDTO realTimeState(String paperId) {

        // IDOR 防护：只能查自己的试卷
        this.checkPaperOwner(paperId);
        Paper paper = this.getById(paperId);

        PaperRealTimeRespDTO respDTO = new PaperRealTimeRespDTO();

        long limit = paper.getLimitTime().getTime();
        long leftSeconds = (limit - System.currentTimeMillis()) / 1000;

        respDTO.setLeftSeconds((int) leftSeconds);
        respDTO.setHanded(paper.getHandState() != null && paper.getHandState().equals(1));

        return respDTO;
    }

    @Override
    public PaperDetailRespDTO fullDetail(String id) {
        // IDOR 防护：校验试卷归属（管理端阅卷放行）
        this.checkPaperOwner(id);
        PaperDetailRespDTO dto = baseMapper.selectPaperDetail(id);

        // T-15：成绩公示控制。管理端角色阅卷完整可见；学员端在考试未公示时掩码分数/结果
        boolean adminRole = isManagementRole();
        if (!adminRole && dto != null && dto.getExamId() != null) {
            Exam exam = examService.getById(dto.getExamId());
            if (exam != null && !Integer.valueOf(1).equals(exam.getScoreVisible())) {
                dto.setUserScore(null);
                dto.setPassed(null);
            }
        }
        return dto;
    }

    @Override
    public Paper checkPaperOwner(String paperId) {
        Paper paper = this.getById(paperId);
        if (paper == null) {
            throw new ServiceException("试卷不存在或已被删除！");
        }
        // 管理端角色放行（阅卷/复查）
        if (isManagementRole()) {
            return paper;
        }
        // 学员：只能操作自己的试卷
        String userId = UserUtils.getUserId();
        if (userId == null || !userId.equals(paper.getUserId())) {
            throw new ServiceException("无权操作他人试卷！");
        }
        return paper;
    }

    private boolean isManagementRole() {
        List<String> roles = UserUtils.getRoles();
        return roles != null && (roles.contains("admin") || roles.contains("keryo_head_nurse")
                || roles.contains("keryo_assistant") || roles.contains("keryo_director")
                || roles.contains("keryo_supervisor"));
    }

    /**
     * 获取客户端 IP（兼容代理头）
     */
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

    /**
     * 查找是否有进行中的考试
     *
     * @param examId
     * @param userId
     * @return
     */
    private String findProcess(String examId, String userId) {
        //查询条件
        QueryWrapper<Paper> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Paper::getExamId, examId)
                .eq(Paper::getUserId, userId)
                .eq(Paper::getHandState, 0);

        Paper paper = this.getOne(wrapper, false);
        if (paper != null) {
            return paper.getId();
        }
        return null;
    }
}

