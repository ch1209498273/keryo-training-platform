# V1.0 Portfolio Release：目标与交付策略

> 当前有效项目定位：2026-08-15 起生效

## 1. 最终目标

本项目来源于真实医护培训业务需求，但当前没有正式采购方，也没有确定进入真实生产环境。因此，本项目当前不以“达到医疗机构正式商业生产交付标准”为目标。

当前目标是完成一个基于真实业务需求、具备完整业务闭环、经过工程验证和人工 UAT、可部署和可演示的 Portfolio Release，用于证明完整的 AI-assisted / Agentic Software Engineering 落地能力。

最终版本暂定为：**V1.0 Portfolio Release**。

项目需要能够展示以下完整过程：

```text
需求分析 → 产品设计 → AI 辅助开发 → 工程治理 → 自动化测试
→ Engineering Baseline → 人工 UAT → Bug 分级与修复
→ 回归测试 → 业务需求回验 → Demo 部署 → 项目总结
```

后续技术和产品决策以“工程完整性、可验证性、可展示性”为主要判断标准，避免为不存在的正式客户提前建设生产级复杂系统。

## 2. 当前状态（2026-08-30）

| 维度 | 状态 | 说明 |
|---|---|---|
| Engineering Baseline | **历史 PASS（2026-08-15）** | 当次隔离环境的 JDK17、MySQL 初始化、打包、前端构建、服务健康检查通过；2026-08-30 有下述 PARTIAL 分项证据，未重跑完整 Baseline |
| Automated Baseline Verification | **历史 PASS（2026-08-15）** | 当次 Java 18 个测试、Python API smoke、数据库校验通过，不是当前候选结论 |
| 当前工作区 Regression | **PARTIAL（2026-08-30）** | 控制端 Java 12 类/36 个测试、前端 5 个测试、Python 13 个测试分项 PASS；OPT-02 已实现，分项验证通过且独立复核通过 |
| 当前工作区构建/类型检查 | **分项 PASS（2026-08-30）** | JDK17 package/测试编译（-DskipTests，Java 测试另行执行）、前端类型检查/生产构建通过；使用已有依赖，未执行 npm 安装 |
| Manual UAT | **正式结果汇总未填** | 已有非正式用户反馈和开发修复；仍需负责人实际验收、补录结果与人工 RETEST |
| Business Requirement Validation | **NOT COMPLETED** | 尚未完成真实业务场景回验，业务疑问不得由 AI 自行猜测 |
| Portfolio Release | **NOT READY** | 需要完成 UAT、Bug 修复、回归、业务回验和 Demo 整理 |
| Production Readiness | **NOT REQUIRED AT CURRENT STAGE** | 当前不以正式生产交付为验收目标 |
| Commercial Delivery Readiness | **OUT OF CURRENT SCOPE** | 没有正式采购方或试点机构 |

**重要边界：`BASELINE_PASS` 不代表业务验收完成。**

台账现有 11 条问题（5 P0、6 P1），001～010 `FIXED` 待人工 `RETEST`、011 `OPEN`、0 `CLOSED`；这是已记录问题数，不是人工 UAT 发现数。本轮仅采纳 OPT-01 文档对齐与 OPT-02 测试入口对齐；OPT-02 已实现，分项验证通过且独立复核通过，实际结果见下方控制端执行报告。未执行 npm 安装、含隔离数据库/服务启动/API smoke 的完整 Baseline 或人工 UAT，不宣告完整 Baseline PASS 或发布就绪。审核豁免等仍为 `NEEDS BUSINESS CONFIRMATION`，见 `../DECISIONS.md`。

当前发布仍需当前候选的 Baseline/活跃回归、人工 UAT/Golden Path、原问题 RETEST 和 Demo 证据。`BUG-UAT-011` 子证据 011-A 保留这一门禁；011-B 的正式生产迁移、CI/Docker、灾备等留未来商业化，不因生产设施缺失阻断 Portfolio，父项保持 OPEN。

> 历史状态口径（2026-08-19）：当时记录 JDK17 后端 `test-compile` 与 36 项安全回归、前端 `npm run ts:check`/`npm run build:test` 通过；完整 Baseline 为 2026-08-15 隔离报告。两者均非 2026-08-30 重跑结果，不代表当前工作区完整 PASS，也不替代 Manual UAT。

### 2026-08-19 安全与业务边界回归（历史记录）

- `BUG-UAT-001～010` 已完成开发修复，台账状态为 `FIXED`，等待负责人按原步骤 `RETEST`；未标记 `CLOSED`。
- 后端：JDK17 `mvn -o -DskipTests test-compile` PASS；JUnitCore `OK (36 tests)`。
- 前端：`npm run ts:check` PASS；`npm run build:test` PASS（生成 `dist-test`）。
- 未完成项保持明确：人工 UAT、生产迁移/CI/Docker、灾备和备份介质清理仍不纳入本轮已完成声明。

## 3. 当前版本范围

### 纳入 V1.0 Portfolio Release

- 管理员、培训管理员/组长、学员的核心角色流程；
- 培训资料、培训计划、学员分配、考试、交卷、成绩和统计闭环；
- 登录验证码、权限隔离、密码流程、审计日志等已完成的基础安全能力；
- 可重复运行的数据库初始化、构建、启动和 API smoke；
- 人工 UAT、UAT Bug Ledger、回归测试和业务需求复核；
- 可部署的 Demo 环境、启动说明、截图和演示流程；
- 真实记录工程决策、自动化验证、UAT 反馈和 AI Agent 纠错案例。

### 暂不纳入当前版本

- 正式医疗机构生产交付；
- 公网长期运营、备案、正式 SLA 和生产级基础设施；
- 正式审计交付、完整 DPIA/等保材料和深度渗透测试；
- 没有 UAT 或业务需求依据的功能扩展；
- TD-6、TD-7，除非人工 UAT 证明它们阻塞核心演示流程；
- CSP 强制模式切换，当前继续保持 Report-Only。

## 4. 工作原则

1. 先完成可重复验证，再进行人工验收；不把自动化通过误写成业务验收通过。
2. UAT 发现问题先进入 `docs/UAT_BUG_LEDGER.md`，未经确认不进行大范围修改。
3. P0/P1 优先修复；每次修复后只执行与问题直接相关的回归，并在阶段末执行核心回归。
4. 业务规则无法从代码和现有资料确定时，标记 **【需要业务确认】**，不由 AI 猜测。
5. 保持 Baseline 已验证能力，不为了架构美化进行无关重构。
6. MySQL 7 天稳定性验证独立于人工 UAT，不阻塞开发；已有监控记录不足 168 小时，只有重新完成 7 天记录后才能标记 PASS。CSP 暂不切换。

## 5. 证据入口

- 当前 Roadmap：`docs/ROADMAP.md`
- 初学者 UAT 手册：`docs/UAT_MANUAL.md`
- UAT 问题台账：`docs/UAT_BUG_LEDGER.md`
- Case Study 数据记录：`docs/CASE_STUDY_LOG.md`
- 项目背景：`PROJECT_CONTEXT.md`
- 决策记录：`DECISIONS.md`
- 成长记忆：`LESSONS_LEARNED.md`
- Baseline 运行说明：`scripts/baseline/README.md`
- 最近一次 Baseline 报告：`baseline-report/baseline-20260815-232130.md`
- OPT-01/02 控制端执行报告：[2026-08-30-opt01-opt02.md](verification/2026-08-30-opt01-opt02.md) — 已记录实际分项 PASS，整体 **PARTIAL**；OPT-02 已实现且独立复核通过，完整 Baseline/UAT 未执行。

