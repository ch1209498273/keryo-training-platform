package com.keryo.training.modules.stat.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 统计看板响应（T-10）
 */
@Data
public class DashboardDTO implements Serializable {

    /** 卡片1：学员总数（启用） */
    private Long studentCount;

    /** 卡片2：已完成考试人次 */
    private Long completedCount;

    /** 卡片3：平均分 */
    private BigDecimal avgScore;

    /** 本年度培训计划参与次数（按计划-学员关系计数） */
    private Long annualTrainingCount;

    /** 通过率折线（近N次考试/时间点） */
    private List<PassRatePoint> passRateTrend;

    /** 参训名单（最近成绩记录，脱敏） */
    private List<TraineeItem> trainees;

    /** 未完成（有考试资格但无成绩记录的学员） */
    private List<TraineeItem> notFinished;

    @Data
    public static class PassRatePoint implements Serializable {
        private String name;        // 考试名或日期
        private Integer total;      // 参考人次
        private Integer passed;     // 通过人次
        private BigDecimal rate;    // 通过率 0-100
    }

    @Data
    public static class TraineeItem implements Serializable {
        private String userId;
        private String realName;    // 脱敏
        private String deptName;
        private String examTitle;
        private BigDecimal score;
        private Integer passed;
        private String handTime;
    }
}

