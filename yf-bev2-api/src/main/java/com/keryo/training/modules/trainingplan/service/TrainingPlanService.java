package com.keryo.training.modules.trainingplan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.keryo.training.modules.trainingplan.entity.TrainingPlan;
import com.yf.base.api.api.dto.PagingReqDTO;

import com.yf.system.modules.user.entity.SysUser;

import java.util.List;

public interface TrainingPlanService extends IService<TrainingPlan> {

    /** 分页查询（含数据权限：医护长看本科室/助理管理员看全部） */
    IPage<TrainingPlan> paging(PagingReqDTO<TrainingPlan> reqDTO);

    /** 保存（DRAFT 可编辑；非 DRAFT 拒绝修改） */
    void savePlan(TrainingPlan plan);

    /** 按当前用户的数据范围读取计划，防止直接传 ID 越权。 */
    TrainingPlan getAccessible(String id);

    /** 删除草稿计划，并复核创建者/管理角色。 */
    void deletePlan(String id);

    /** 提交审批 DRAFT→PENDING */
    void submit(String id);

    /** 审批通过 PENDING→ACTIVE（助理管理员操作） */
    void approve(String id, String remark);

    /** 审批驳回 PENDING→DRAFT（助理管理员操作） */
    void reject(String id, String remark);

    /** 归档 ENDED→ARCHIVED */
    void archive(String id);

    /** 自动流转：到期 ACTIVE→ENDED（定时任务调用） */
    void autoEnd();

    /** 校验计划参与关系管理权限；小组长仅限本部门计划和学员。 */
    void checkParticipantAccess(String planId, List<String> userIds);

    /** 返回当前操作者可分配到该计划的启用用户。 */
    List<SysUser> participantCandidates(String planId);
}

