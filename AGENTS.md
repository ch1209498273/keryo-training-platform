# AGENTS.md — 项目宪法与接任 Agent 导航

> 本文件是本项目对 AI Agent 和开发者的最高工作约束。详细业务背景、决策和经验分别见 `PROJECT_CONTEXT.md`、`DECISIONS.md`、`LESSONS_LEARNED.md`。

## 0. 当前有效项目口径（2026-08-30）

项目目标是 **V1.0 Portfolio Release**，不是正式医疗生产交付。当前阶段为 **M1.5 Manual UAT**。

| 项目状态 | 当前结论 |
|---|---|
| Engineering Baseline | 历史 PASS（2026-08-15：`baseline-report/baseline-20260815-232130.md`） |
| Automated Baseline Verification | 历史 PASS（同一份 2026-08-15 隔离 Baseline 证据） |
| 当前工作区后端复核 | 2026-08-30 分项 PASS：JDK17 新目录 package/测试编译通过；另由 JUnitCore 执行 12 类、36 个测试通过（package 使用 -DskipTests，本身不执行测试） |
| 当前工作区前端复核 | 2026-08-30 分项 PASS：2 个文件、5 个测试，类型检查、生产构建通过；使用已有依赖，未执行 npm 安装 |
| 当前工作区 Python 回归 | 2026-08-30 分项 PASS：13 个活跃本地测试（启动器 2 项 + Java 入口 7 项 + Python 入口 4 项），不等于 API smoke |
| MySQL 运行环境 | MySQL84 服务状态本轮未检查；7 天稳定性证据尚未完成 |
| Manual UAT | 正式结果汇总未填；已有非正式用户反馈和开发修复，仍需负责人执行/补录实际结果与人工 RETEST |
| Business Requirement Validation | NOT COMPLETED |
| Portfolio Release | NOT READY |
| Production / Commercial Delivery | OUT OF CURRENT SCOPE |

`BASELINE_PASS` 只证明当次隔离环境通过，不自动证明当前未提交工作区、业务验收或生产就绪。

当前整体证据为 **PARTIAL**：控制端已记录上述分项通过，但未运行含 npm 安装、隔离数据库、服务启动和 API smoke 的完整 Baseline，也未执行人工 UAT。2026-08-18 后端打包/18 个测试、前端依赖不完整，以及 2026-08-19 后端 test-compile/36 个测试和前端类型检查/测试构建仍保留为历史记录，不与本次结果混算。

台账记录 11 条问题（5 P0、6 P1）：001～010 为 `FIXED`，等待人工 `RETEST`；011 为 `OPEN`；`CLOSED` 为 0。这不是人工 UAT 发现数量，原始发现来源不明时按历史工程评估记录，不能猜测报告人。011 的当前候选/UAT 证据与未来生产交付范围分开，见 `docs/UAT_BUG_LEDGER.md`。

本轮仅采纳 OPT-01/02：OPT-01 文档完成；OPT-02 已实现，**分项验证通过，独立复核通过**。控制端执行报告 [2026-08-30-opt01-opt02.md](docs/verification/2026-08-30-opt01-opt02.md) 已记录实际命令、数量和范围，不代表完整 Baseline PASS。审核豁免等业务疑问仍为 `NEEDS BUSINESS CONFIRMATION`，见 `DECISIONS.md`。

## 1. 项目宪法（必须遵守）

1. **范围优先**：当前只围绕 V1.0 Portfolio Release、M1.5～M2 Roadmap 工作；不提前建设正式生产、商业化、深度合规或二期功能。
2. **保持现有系统**：不因技术偏好重做架构，不主动替换框架、数据库、核心依赖或权限体系。
3. **证据优先**：任何“完成、通过、已修复”都必须有本轮可追溯的命令、报告、截图或人工结果；历史报告必须标注日期。
4. **人工 UAT 不代做**：UAT 结果由项目负责人填写。Agent 可以准备环境、读取结果、记录 Bug 和执行经确认的修复，但不能替用户伪造 PASS。
5. **先记录再修复**：UAT 问题先进入 `docs/UAT_BUG_LEDGER.md`，确认严重程度、预期行为和范围后再改代码。
6. **数据库安全**：不直接修改现有业务数据库 Schema，不执行不可逆删除，不把 baseline 数据库当生产数据源；任何数据清理必须有明确目标和复核证据。
7. **凭据安全**：密码、Token、API Key 不进入代码、文档、Git、命令输出或报告；通过环境变量传入并在报告中脱敏。
8. **兼容性约束**：后端使用 JDK17；`com.yf.*` 包名、MyBatis XML 引用和现有接口约定不得随意改动。
9. **测试入口固定**：legacy Python/Playwright 仅作历史资产；自动验收入口是隔离数据库上的 `scripts/baseline/smoke.py`。
10. **文档同步**：每次形成新测试证据、业务决定、UAT 结果或技术经验后，同步更新对应治理文档；不覆盖历史事实，追加新记录。

## 2. 自动停止条件

遇到以下情况必须停止并向项目负责人说明，不得自行猜测：核心架构变更、重大 Schema/数据风险、大量删除代码、替换核心技术栈、Roadmap 与真实代码重大冲突、业务规则不明确、测试连续失败且原因不清、需要真实生产数据/密钥/付费资源，或必须扩大当前阶段范围才能继续。

## 3. 文档治理入口

| 文档 | 责任 |
|---|---|
| `PROJECT_CONTEXT.md` | 项目背景、角色、流程、架构和当前范围 |
| `DECISIONS.md` | 已确认的业务/技术决策及其原因、影响和证据 |
| `LESSONS_LEARNED.md` | 可复用的工程经验、失败原因、人工纠偏和交接记忆 |
| `docs/ROADMAP.md` | Backlog、阶段和优先级 |
| `docs/PORTFOLIO_RELEASE.md` | V1.0 目标和交付边界 |
| `docs/UAT_MANUAL.md` | 初学者可执行的人工验收步骤 |
| `docs/UAT_BUG_LEDGER.md` | UAT 问题、严重程度、状态和回归证据 |
| `docs/CASE_STUDY_LOG.md` | 真实测试指标、反馈和项目复盘数据 |

文档冲突时，按“当前用户明确决定/最新可验证证据 → 实际代码和数据库 → 当前治理文档 → 历史交接资料”的顺序判断，并把冲突记录下来。

## 这是什么

血透中心医护培训平台：**在线培训 + 在线考试**的内部系统。上游是「云帆在线考试系统 V2.0」（二开），定制业务全部在 `com.keryo.training.*` 扩展包。

> 根目录 README.md 是当前项目说明；本文件与下列治理文档优先于历史上游说明。

## 技术栈

- 后端：Java 17 + Spring Boot 3.2.1 + MyBatis-Plus 3.5.11 + Shiro 2.0.2 + JWT
- 前端：Vue 3 + TypeScript + Element Plus + Vite 4
- 数据库：MySQL 8.4（库 `exam_keryo`）
- 缓存：Redis（本地 dev 用内存实现降级）
- AI：DeepSeek API（OpenAI 兼容），Key 优先读取 `AI_<PROVIDER>_API_KEY`、其次 `AI_API_KEY`，数据库 `el_keryo_ai_config` 仅作兼容回退（代码事实，非本轮运行验证）

## 核心目录

```
yf-bev2-api/    后端。com.yf.*（上游基底）+ com.keryo.training.*（定制业务）
yf-bev2-vue/    前端。src/views（页面）、src/api（axios）、src/store（pinia）
scripts/        启动/备份/单测脚本。start_all.sh 一键起
docs/           项目文档（验收手册/操作手册/权限模型）+ 本交接 docs/
yf_boot_exam.sql  种子 SQL（数据库初始化/恢复）
test_*.py e2e_*.js  验收自动化脚本
```

## 重要文档

| 文档 | 内容 |
|---|---|
| `docs/HANDOVER.md` | 总交接（阶段/完成度/风险/先做什么/不要动什么）|
| `docs/ARCHITECTURE.md` | 架构/技术栈/目录/数据流/API/数据库 |
| `docs/BUSINESS_RULES.md` | 业务规则（角色权限/流程/状态机/产品决策）|
| `docs/FEATURE_STATUS.md` | 功能盘点（已完成/有bug/半成品/未实现/废弃）|
| `docs/KNOWN_ISSUES.md` | 已知问题/技术债/风险/临时代码 |
| `docs/ROADMAP.md` | Backlog（P0-P4 + TD）与建议顺序 |
| `docs/PORTFOLIO_RELEASE.md` | V1.0 Portfolio Release 目标、范围和当前状态 |
| `docs/UAT_MANUAL.md` | 初学者可执行的人工 UAT 操作手册 |
| `docs/UAT_BUG_LEDGER.md` | UAT Bug 分级、状态和证据台账 |
| `docs/CASE_STUDY_LOG.md` | Baseline、UAT、回归和 Case Study 事实记录 |
| `PROJECT_CONTEXT.md` | 项目背景、角色、流程、架构和当前范围 |
| `DECISIONS.md` | 已确认的业务/技术决策和变更原因 |
| `LESSONS_LEARNED.md` | 可复用工程经验、失败原因和人工纠偏 |
| `docs/验收手册.md` | 用户验收手测清单（v2.1）|
| `docs/操作手册.md` | 用户操作手册 |
| `docs/权限模型梳理与设计.md` | 角色/权限矩阵（v2）|

## 如何启动（本机）

```bash
# 一键（自动起 MySQL+后端9091+前端8000，已在跑的会跳过）
export DB_PASSWORD='本机 MySQL 口令'
bash scripts/start_all.sh
# Windows 也可双击 启动服务器.bat；缺少 DB_PASSWORD 时会在窗口内安全提示
# 该入口使用相对项目路径，自动寻找 Python/Anaconda、JDK17 和 npm，失败时保留窗口

# 访问
#   前端 http://localhost:8000  （演示账号和密码见 docs/验收手册.md）
#   后端 http://localhost:9091
```

## 如何构建

```bash
# ⚠️⚠️ 必须用 JDK 17！系统默认 JDK25 会让 Lombok 失效，编译必然失败 ⚠️⚠️
cd yf-bev2-api
JAVA_HOME="D:/software/jdk-17.0.2" "D:/software/apache-maven-3.9.16/bin/mvn.cmd" -o package -DskipTests

# 前端
cd yf-bev2-vue
npm run dev          # 开发
npm run build:pro    # 生产构建
```

## 如何测试

```bash
bash scripts/run_unit_tests.sh   # 后端单测（委托统一 Java runner；2026-08-30 实测 12 类/36 项，配置见 scripts/baseline/README.md）
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\baseline\run_baseline.ps1
```

> 根目录 legacy Python/Playwright 脚本已正式归档；核心 API 自动验收使用隔离数据库上的 baseline smoke。正式 Manual UAT 结果汇总未填，已有非正式反馈不等于完整验收；请使用 `docs/UAT_MANUAL.md` 记录实际结果，不要把历史 `BASELINE_PASS` 当作业务验收通过。

## 修改代码必须遵守的规则

1. **包结构**：定制业务写 `com.keryo.training.*`；上游 `com.yf.*` 的包名**严禁改动**（MyBatis XML 类引用依赖包名）。
2. **后端编译/启动一律 JDK17**；禁止用默认 JDK25。
3. **前端 api**：必须 `import request from '@/config/axios'` + `request.post({url, data})`，不要用 `@/api/request`（Vite 静默失败）。
4. **登录接口参数**：`userName`；成功码 `code=0`。
5. **题型值全小写**：`fill/essay/radio/multi/judge`，比较用 equalsIgnoreCase。
6. **数据库字段**：实体加字段到 `@TableName` 类即可（MyBatis-Plus 自动 SQL），一般不用改 XML。
7. **新 Service 依赖老 Service 用 Mapper 注入**，避免循环依赖（Lombok 构造注入 + @Lazy 不生效）。
8. **菜单/权限改动后必须重启后端**（@Cacheable(sys:menu) 内存缓存）；`el_sys_role_menu` 有 id 主键，INSERT 需生成唯一 id。
9. **新增菜单固定 ID 全局唯一**，严禁复用已有 ID（曾因此误删菜单）。
10. **密码规则统一**（字母+数字、必须同时含、≤30、无特殊字符）：改密码逻辑复用 `SysUserServiceImpl.validatePassword()`。

## 高风险区域

- `com.yf.modules.exam.paper`（考试会话/判分/阅卷）：核心引擎，改动需全链路回归。
- `SysUserServiceImpl`（登录/注册/忘记密码）：认证核心，涉及多角色判定。
- `com.keryo.training.modules.trainingplan`（培训计划状态机）：状态流转耦合考试/学员可见性。
- 权限相关（`el_sys_role_menu` / ShiroConfig / JwtFilter）：改错会导致整站 403/越权。

## 禁止执行

- ⛔ 不使用 JDK17 编译后端（默认 JDK25 必失败）。
- ⛔ 改名/移动 `com.yf.*` 包或类。
- ⛔ 删除 `yf_boot_exam.sql`（种子 SQL）。
- ⛔ 未与用户确认前，删除 `reset-pass` 接口 / `user` 角色 / 历史备份 `backups/`。
- ⛔ 大版本升级依赖（Spring Boot / Lombok / Element Plus）——需全量回归。
- ⛔ 直接改生产数据库结构（交接期只记录）。
- ⛔ 在文档中写真实密码/Token/API Key（一律指到配置文件）。

## 交接注意事项

- 当前处于 **M1.5 Manual UAT**：2026-08-15 Baseline PASS 为历史证据；2026-08-30 当前工作区为 PARTIAL 分项回归通过、OPT-02 独立复核通过，未完成完整 Baseline/UAT；001～010 开发修复待人工 RETEST。
- 当前目标是 V1.0 Portfolio Release；正式生产交付、商业化和深度合规内容转入 `Future Production / Commercialization Backlog`，不提前建设。
- 所有标注**【待确认】**的业务规则（BUSINESS_RULES.md）需与业务负责人确认后再实现。
- 用户沟通风格：直接、务实，不要客套话。

