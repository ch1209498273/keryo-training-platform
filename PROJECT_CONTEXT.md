# PROJECT_CONTEXT.md — 项目背景与运行上下文

> 本文件回答“项目是什么、为谁服务、当前要交付什么”。它不记录逐次技术决策；技术决策见 `DECISIONS.md`。
>
> 当前快照：2026-08-30。当前工作区已有 PARTIAL 分项回归通过证据，OPT-02 已实现且独立复核通过；完整 Baseline 未重跑，正式人工 UAT 结果汇总未填，已有非正式用户反馈和开发修复。

## 1. 项目定位

血透中心医护培训平台是一个面向内部医护人员的“在线培训 + 在线考试”系统。项目来源于真实业务需求，但当前没有正式采购方，也没有确定进入真实生产环境。

当前最终目标是 **V1.0 Portfolio Release**：证明项目经历了需求分析、产品设计、AI 辅助开发、工程治理、自动化测试、Baseline、人工 UAT、Bug 修复、回归、业务需求回验、Demo 部署和项目总结。

当前不以正式医疗生产交付、商业化交付或深度合规为验收目标。这些内容保留在 `docs/ROADMAP.md` 的 Future Production / Commercialization Backlog。

## 2. 核心业务闭环

```text
管理员/培训管理员准备培训
  → 创建或维护培训资料
  → 创建培训计划并关联考试
  → 分配学员并提交/审批计划
  → 学员登录、学习资料、进入考试、提交答卷
  → 查看成绩（按公示规则）
  → 管理人员查看参与情况、成绩和统计
```

核心对象包括：培训资料、培训计划、计划参与关系、题库、题目、考试、试卷、答题明细、考试记录、审计日志和忘记密码申请记录。

## 3. 用户角色

| 角色 | 主要用途 | 当前边界 |
|---|---|---|
| `admin` | 系统与全量业务管理 | 超级管理员，不参加考试 |
| `keryo_director` | 医护总监业务管理、成绩仲裁 | 可成绩修正、公示、补考和题库审核 |
| `keryo_assistant` | 业务辅助管理 | 不能修改成绩或公示成绩 |
| `keryo_supervisor` | 区域督导 | 按区域数据范围管理和审核 |
| `keryo_head_nurse` | 科室医护长 | 可参加学员端流程，数据范围为本科室 |
| `keryo_nurse` | 一线医护/学员 | 查看被分配培训、学习、考试和个人成绩 |
| `keryo_team_leader` | 小组长 | 已有 BK-14 本部门参与管理：仅管理明确绑定本部门的计划，候选学员限本部门启用账号；不扩展跨部门权限 |

旧 `user` 角色保留历史数据但停用，注册默认角色为 `keryo_nurse`。

## 4. 当前技术上下文

| 层 | 当前实现 |
|---|---|
| 后端 | Java 17、Spring Boot 3.2.1、MyBatis-Plus 3.5.11、Shiro 2.0.2、JWT |
| 前端 | Vue 3、TypeScript、Element Plus、Vite 4 |
| 数据库 | MySQL 8.4，业务库 `exam_keryo` |
| 缓存 | 本地开发使用内存 Redis 降级实现；不要求本地 Redis 才能跑 Baseline |
| AI | DeepSeek/OpenAI 兼容接口；Key 优先 `AI_<PROVIDER>_API_KEY`，其次 `AI_API_KEY`，数据库值兼容回退 |
| 文件预览 | `FilePreview.vue` 使用 docx-preview、pptx-preview 和浏览器原生 PDF；实际文件渲染/权限/失败体验仍待人工验证 |
| 业务扩展 | `com.keryo.training.*`；上游 `com.yf.*` 包名保持不变 |

上述实现按 2026-08-30 工作区静态核对：`TrainingPlanController`/`TrainingPlanServiceImpl.checkParticipantAccess` 与 `participantCandidates`、`yf-bev2-vue/src/views/Training/components/FilePreview.vue`（培训管理页与学员页引用）、`AiConfigServiceImpl.configuredApiKey`。代码存在不等于运行通过；旧“小组长无额外能力 / Office Online Viewer”摘要已过时。

## 5. 运行与验证上下文

- MySQL：Windows 服务 `MySQL84`，常规端口 `3306`，配置文件为 `D:/MySQL/my.ini`。
- 后端：端口 `9091`，必须使用 JDK17；当前环境可能需要显式设置 Maven `user.home` 和本地仓库路径。
- 前端：开发端口 `8000`，代理 `/api` 到后端 `9091`。
- 数据库凭据：使用 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 环境变量；Baseline 使用 `BASELINE_DB_*`。
- 干净 Baseline：使用独立数据库、隔离运行目录和可重复的 schema/seed/verify 流程。
- 自动验收：以 `scripts/baseline/run_baseline.ps1` 和 `scripts/baseline/smoke.py` 为入口；legacy Playwright 不作为通过条件。

## 6. 当前状态与边界

| 阶段/能力 | 状态 |
|---|---|
| Engineering Baseline | 历史 PASS，2026-08-15 报告；不代表当前工作区 |
| Automated Baseline Verification | 历史 PASS，2026-08-15 包含 Java 18 个测试、Python API smoke、数据库校验和前端构建 |
| 当前工作区后端 | 2026-08-30 分项 PASS：JDK17 新目录 package/测试编译通过（-DskipTests）；另行 JUnitCore 执行 12 类、36 个测试通过 |
| 当前工作区前端 | 2026-08-30 分项 PASS：2 个文件、5 个测试；类型检查与生产构建通过，使用已有依赖，未执行 npm 安装 |
| 当前工作区 Python 回归 | 2026-08-30 分项 PASS：13 个活跃本地测试（启动器 2 项、Java 入口 7 项、Python 入口 4 项），不是 API smoke |
| MySQL 7 天稳定性 | 尚未完成；已有监控记录不足 168 小时 |
| Manual UAT | 15 个计划 Case 的正式结果汇总未填；已有非正式反馈和开发修复，负责人需执行/补录结果与人工 RETEST |
| Business Requirement Validation | NOT COMPLETED |
| Portfolio Release | NOT READY |

以上新证据来自控制端 [2026-08-30-opt01-opt02.md](docs/verification/2026-08-30-opt01-opt02.md)，整体为 **PARTIAL**：未运行含 npm 安装、隔离数据库、服务启动、API smoke 的完整 Baseline，也未执行人工 UAT。2026-08-18 后端打包/18 个测试和前端依赖问题、2026-08-19 后端 test-compile/36 个测试及前端类型检查/测试构建仍为历史记录，日期和适用范围不变。

`docs/UAT_BUG_LEDGER.md` 记录 11 条问题（5 P0、6 P1），001～010 `FIXED` 待人工 `RETEST`，011 `OPEN`，0 `CLOSED`；不作为人工 UAT 发现数。011 的当前候选回归/人工验收仍是发布门禁，正式生产迁移、CI/Docker、灾备留未来商业化，不因缺少这些生产设施阻断 Portfolio。

## 7. 当前工作顺序

1. 本轮仅采纳 OPT-01 文档对齐和 OPT-02 现有测试入口对齐；OPT-02 已实现，分项验证通过且独立复核通过；实际命令、计数和局限见上述控制端执行报告。
2. 后续由负责人按 `docs/UAT_MANUAL.md` 执行/补录正式人工验收，并对 001～010 按已记录的问题步骤人工复测；不预填 PASS。
3. 失败项先写入 `docs/UAT_BUG_LEDGER.md`，不直接猜测业务规则。
4. UAT 基本完成后进入 M1.6 Bug Fix & Regression，再进入 M1.7 业务需求回验。
5. M1.8 只做影响 Portfolio 展示的必要整理，最后形成 V1.0 Portfolio Release。

OPT-03～10 仍为建议，未获本轮自主实施授权。审核豁免、培训完成/统计含义等保留 `NEEDS BUSINESS CONFIRMATION`，以 `DECISIONS.md` 为入口；未决业务问题不阻止 OPT-01 文档完成。

