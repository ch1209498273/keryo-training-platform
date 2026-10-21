# 项目交接总文档（HANDOVER）

> **WorkBuddy → Codex 总交接**
> 交接日期：2026-08-14
> 本文档面向**完全不了解项目历史**的接任 AI（OpenAI Codex / 其他编程 Agent）。

---

## 1. 项目是什么

> **2026-08-18 当前有效治理口径**：请先阅读根目录 `AGENTS.md`、`PROJECT_CONTEXT.md`、`DECISIONS.md` 和 `LESSONS_LEARNED.md`。本交接文档保留 WorkBuddy 历史上下文，其中较早的建议可能已被后续 BK 执行结果 supersede；以最新 Roadmap、代码和 Baseline/UAT 证据为准。

**血透中心医护培训平台**：企业内部培训系统。核心场景——血透中心医护通过平台**学习培训内容、参加在线考试、查看成绩与统计**。

- 上游基底：云帆在线考试系统 V2.0 开源版（二开）。
- 业务定制：在原系统之上实现了**培训内容/培训计划/考试全链路/成绩公示/AI 出题**等血透业务能力。
- 已完成：核心业务闭环和 Engineering Baseline；当前目标已调整为 **V1.0 Portfolio Release**，不以正式商业生产交付为目标。

## 2. 项目当前处于什么阶段

- **阶段**：M1.5 Manual UAT 准备（2026-08-15）。Engineering Baseline：PASS；Automated Baseline Verification：PASS；Manual UAT：NOT STARTED；Business Requirement Validation：NOT COMPLETED；Portfolio Release：NOT READY。
- **最近完成的工作**（2026-08-13）：
  1. 注册密码规则强制 + 注册后跳登录页 + 默认角色改为一线医护
  2. 学员端考试列表改为按「参与的培训计划」过滤
  3. 忘记密码功能完整实现（账号+姓名+验证码申请 → 管理员审核重置 → 申请记录管理页）
  4. 题库分类精简为「职业技能/培养管理/其他」
- **当前不做**：正式生产环境、商业交付、深度合规和复杂生产基础设施。相关内容保留在 `docs/ROADMAP.md` 的 Future Production / Commercialization Backlog。

## 3. 真实完成度

| 维度 | 完成度 | 说明 |
|---|---|---|
| 核心业务闭环（登录→培训→考试→出分→公示→看板）| ✅ 90%+ | 功能完整，演示数据需清理 |
| 管理后台（RBAC/用户/菜单/字典/配置）| ✅ 完整 | 权限矩阵测试 14/14 |
| 安全基线 | ✅ 主体完成 | bcrypt/防注入/防XSS/防切屏/审计/锁定；M2 项未做 |
| AI 出题 | ✅ 基础可用 | 可换供应商、可测试、可降级 |
| 测试 | ⚠️ 中等 | 3 个单测 + 隔离数据库 baseline smoke；根目录 Python/Playwright 历史脚本已归档 |
| 二期（大屏/小程序/监考/深度合规）| ⬜ 0% | 未排期 |

> 历史说明：README 曾经是上游「云帆」原样说明；当前根目录 README 已更新为本项目说明。若历史段落与当前文档冲突，以 `AGENTS.md` 和最新证据为准。

## 4. 最近正在开发什么

**忘记密码功能（2026-08-13 刚完成）**：登录页「忘记密码」→ 账号+真实姓名+验证码提交申请 → 管理端「组织架构→忘记密码申请」列表查看/标记处理。涉及：`el_keryo_forget_pass` 表、`com.keryo.training.modules.user` 包、`ForgetPass.vue`、`ForgetPasswordForm.vue`、菜单 `2088000000000000001`。

## 5. 最重要的未完成工作（接手后优先）

| 优先级 | 事项 | 原因 |
|---|---|---|
| 1 | **清理演示数据**（脏题/占位PDF/演示计划不关联考试）| 直接影响验收：演示考试会抽到非血透题目、培训内容打开空白、学员端考试列表为空 |
| 2 | **收敛 reset-pass 自助改密接口** | 已完成：旧匿名改密接口已下线，改为管理员审核 |
| 3 | **MySQL 稳定性根因** | 环境反复掉线，生产不可接受 |
| 4 | 强制改密（M2 前置）| 已完成：旧账号回填豁免；新账号/过期账号走独立一次性临时改密流程 |

## 6. 最大风险

1. **MySQL 进程反复掉线**（≥3次），未定位根因 —— 环境稳定性是第一风险。
2. **演示数据污染**（脏题/占位文件），验收观感差。
3. **reset-pass 残留攻击面**：已处理，旧路由不再提供密码修改能力。
4. **JDK 版本陷阱**（默认 JDK25 编译必失败，必须用 JDK17）。
5. **README 与现状不符**，接任 Agent 若只读 README 会严重误判。

## 7. Codex 接手后建议先做什么

1. **读文档**：先读 `AGENTS.md` → `docs/BUSINESS_RULES.md` → `docs/ARCHITECTURE.md` → `docs/FEATURE_STATUS.md`，再读代码。
2. **跑起来**：按 AGENTS.md 启动三服务（MySQL/后端/前端），用 admin/<本地管理员密码> 登录确认。
3. **清演示数据**：处理 BK-03/04/05，让演示链路完整真实。
4. **跑回归**：执行 test_t*.py 关键脚本 + 手测清单（docs/验收手册.md）。
5. **确认需求**：所有标注【待确认】的业务规则，找业务负责人确认后再动。

## 8. 哪些东西暂时不要动

- `user` 角色数据保持不变；`reset-pass` 已在用户确认后下线，忘记密码走管理员审核流程。
- 数据库配置已通过 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 环境变量注入；启动本地服务前必须设置 `DB_PASSWORD`，baseline 使用独立的 `BASELINE_DB_*` 变量。
- ⛔ **不要**大规模重构 `com.yf.*` 上游模块（包名保留是 T-01 决策，改包名会导致 MyBatis XML ClassNotFoundException）。
- ⛔ **不要**升级 Lombok/Spring Boot 大版本（JDK25+Lombok1.18.38 已有坑，升级需全量回归）。
- ⛔ **不要**动数据库结构（交接期只记录不迁移）。
- ⛔ **不要**删除 `yf_boot_exam.sql`（种子 SQL，初始化/恢复用）。
- ⛔ **不要**把 .tmp_regtest / diag_*.py 等当"垃圾"直接删前先确认（部分可能有参考价值）；清理建议走 ROADMAP BK-17/18。

## 9. 与历史文档的冲突说明

| 冲突点 | 历史文档说法 | 实际现状 |
|---|---|---|
| 练习功能 | WBS V3.1 有 T-04.4~4.6 练习 | 已删除（T-13）|
| 学分系统 | 需求 V1.3 有学分 | 已删除（T-16）|
| 课件 | 技术方案 V1.4 称课件 | 已更名「培训」（T-13）|
| LibreOffice 预览 | 方案有 LibreOffice 转 PDF | 已改 Office Online Viewer（CR-03）|
| Docker 本地开发 | WBS 用 docker-compose | 本机 Windows 原生 MySQL + 内存 Redis（CR-02）|
| README | 上游云帆原样 | 不符，以 docs/ 为准 |
| 登录验证码 | 每次登录始终要求验证码 | 前后端已统一按始终校验执行（BK-07 已完成）|

> 权威口径：**实际代码 > docs/ 交接文档 > 历史需求/方案文档**。

## 10. 环境速查

- 启动：`bash scripts/start_all.sh`（或双击 `启动服务器.bat`）——自动起 MySQL/后端/前端，已在跑的会跳过；Windows 双击入口从自身目录定位项目并在缺少 `DB_PASSWORD` 时安全提示输入，失败窗口会保留错误信息。
- 后端编译/打包：`JAVA_HOME=D:/software/jdk-17.0.2 mvn.cmd -o package -DskipTests`（**必须 JDK17**）。
- 后端启动：`D:/software/jdk-17.0.2/bin/java.exe -jar target/yf-bev2-api.jar --server.port=9091`。
- 单测：`bash scripts/run_unit_tests.sh`。
- 数据库：MySQL 8.4，库 `exam_keryo`；root 账号见 `application-dev.yml`（交接文档不写明文，见 AGENTS.md 配置说明）。
- 前端：`npm run dev`（端口 8000，代理 /api → 9091）。

