package com.keryo.training.modules.trainingplan;

import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.keryo.training.modules.trainingplan.mapper.TrainingPlanMapper;
import com.keryo.training.modules.trainingplan.service.impl.TrainingPlanServiceImpl;
import com.yf.base.api.exception.ServiceException;
import com.yf.system.modules.user.UserUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * T-08.4 单元测试基线：培训计划状态机
 * 注：ServiceImpl 泛型父类的 baseMapper 用反射注入，避免 @InjectMocks 对泛型父类失效
 */
public class TrainingPlanServiceTest {

    private TrainingPlanMapper mapper;
    private TrainingPlanServiceImpl service;
    private MockedStatic<UserUtils> userUtilsMock;

    @Before
    public void setUp() throws Exception {
        mapper = mock(TrainingPlanMapper.class);
        service = new TrainingPlanServiceImpl();
        // 反射注入 baseMapper（3.5.11 中位于父类链 CrudRepository）
        java.lang.reflect.Field field = null;
        Class<?> cls = service.getClass();
        while (cls != null && field == null) {
            try {
                field = cls.getDeclaredField("baseMapper");
            } catch (NoSuchFieldException ignored) {
                cls = cls.getSuperclass();
            }
        }
        assertNotNull("未找到 baseMapper 字段", field);
        field.setAccessible(true);
        field.set(service, mapper);

        userUtilsMock = mockStatic(UserUtils.class);
        userUtilsMock.when(UserUtils::getUserId).thenReturn("u1");
        userUtilsMock.when(UserUtils::getRoleLevel).thenReturn(90);
        userUtilsMock.when(UserUtils::getRoles).thenReturn(java.util.List.of("助理管理员"));
    }

    @After
    public void tearDown() {
        if (userUtilsMock != null) {
            userUtilsMock.close();
        }
    }

    private TrainingPlan plan(String status) {
        TrainingPlan p = new TrainingPlan();
        p.setId("p1");
        p.setTitle("测试计划");
        p.setStatus(status);
        return p;
    }

    @Test
    public void submit_从草稿到待审批() {
        TrainingPlan draft = plan("DRAFT");
        when(mapper.selectById("p1")).thenReturn(draft);
        service.submit("p1");
        assertEquals("PENDING", draft.getStatus());
        verify(mapper).updateById(draft);
    }

    @Test
    public void submit_非草稿状态拒绝() {
        when(mapper.selectById("p1")).thenReturn(plan("PENDING"));
        try {
            service.submit("p1");
            fail("应抛出 ServiceException");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("DRAFT"));
        }
    }

    @Test
    public void approve_待审批到进行中() {
        TrainingPlan p = plan("PENDING");
        when(mapper.selectById("p1")).thenReturn(p);
        service.approve("p1", "同意");
        assertEquals("ACTIVE", p.getStatus());
        assertEquals("同意", p.getApproveRemark());
        assertNotNull(p.getApproveTime());
        verify(mapper).updateById(p);
    }

    @Test
    public void approve_非待审批状态拒绝() {
        when(mapper.selectById("p1")).thenReturn(plan("DRAFT"));
        try {
            service.approve("p1", "x");
            fail("应抛出 ServiceException");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("PENDING"));
        }
    }

    @Test
    public void reject_待审批驳回回草稿() {
        TrainingPlan p = plan("PENDING");
        when(mapper.selectById("p1")).thenReturn(p);
        service.reject("p1", "内容不完整");
        assertEquals("DRAFT", p.getStatus());
        assertEquals("内容不完整", p.getApproveRemark());
    }

    @Test
    public void archive_进行中归档() {
        TrainingPlan p = plan("ACTIVE");
        when(mapper.selectById("p1")).thenReturn(p);
        service.archive("p1");
        assertEquals("ARCHIVED", p.getStatus());
    }

    @Test
    public void archive_非进行中拒绝() {
        when(mapper.selectById("p1")).thenReturn(plan("DRAFT"));
        try {
            service.archive("p1");
            fail("应抛出 ServiceException");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("ACTIVE"));
        }
    }
}

