package com.yf.modules.exam.paper.service.impl;

import com.keryo.training.modules.trainingplan.entity.PlanUser;
import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.keryo.training.modules.trainingplan.mapper.PlanUserMapper;
import com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper;
import com.yf.ability.task.service.JobService;
import com.yf.modules.exam.exam.entity.Exam;
import com.yf.modules.exam.exam.service.ExamRecordService;
import com.yf.modules.exam.exam.service.ExamRuleService;
import com.yf.modules.exam.exam.service.ExamService;
import com.yf.modules.exam.paper.dto.response.PaperCheckRespDTO;
import com.yf.modules.exam.paper.mapper.PaperMapper;
import com.yf.modules.exam.paper.service.PaperQuService;
import com.yf.modules.exam.repo.service.RepoQuService;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaperServiceImplTest {

    private ExamService examService;
    private TrainingPlanMapper trainingPlanMapper;
    private PlanUserMapper planUserMapper;
    private PaperServiceImpl service;

    @Before
    public void setUp() throws Exception {
        examService = mock(ExamService.class);
        ExamRuleService examRuleService = mock(ExamRuleService.class);
        RepoQuService repoQuService = mock(RepoQuService.class);
        PaperQuService paperQuService = mock(PaperQuService.class);
        ExamRecordService examRecordService = mock(ExamRecordService.class);
        JobService jobService = mock(JobService.class);
        trainingPlanMapper = mock(TrainingPlanMapper.class);
        planUserMapper = mock(PlanUserMapper.class);
        service = new PaperServiceImpl(examService, examRuleService, repoQuService, paperQuService,
                examRecordService, jobService, trainingPlanMapper, planUserMapper);
        PaperMapper paperMapper = mock(PaperMapper.class);
        when(paperMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        Field baseMapper = com.baomidou.mybatisplus.extension.repository.CrudRepository.class
                .getDeclaredField("baseMapper");
        baseMapper.setAccessible(true);
        baseMapper.set(service, paperMapper);
    }

    @Test
    public void preCheck_rejectsUserOutsideActivePlan() {
        when(examService.getById("exam-1")).thenReturn(openExam());
        TrainingPlan plan = new TrainingPlan();
        plan.setId("plan-1");
        when(trainingPlanMapper.selectList(any())).thenReturn(List.of(plan));
        when(planUserMapper.selectCount(any())).thenReturn(0L);

        PaperCheckRespDTO result = service.preCheck("exam-1", "user-1");

        assertFalse(result.getValidated());
    }

    @Test
    public void preCheck_acceptsAssignedUser() {
        when(examService.getById("exam-1")).thenReturn(openExam());
        TrainingPlan plan = new TrainingPlan();
        plan.setId("plan-1");
        when(trainingPlanMapper.selectList(any())).thenReturn(List.of(plan));
        when(planUserMapper.selectCount(any())).thenReturn(1L);

        PaperCheckRespDTO result = service.preCheck("exam-1", "user-1");

        assertTrue(result.getValidated());
    }

    private Exam openExam() {
        Exam exam = new Exam();
        exam.setStartTime(new Date(System.currentTimeMillis() - 60_000));
        exam.setEndTime(new Date(System.currentTimeMillis() + 60_000));
        exam.setLateMax(0);
        exam.setChance(0);
        return exam;
    }

}

