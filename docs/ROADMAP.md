# 路线图与未完成 Backlog（ROADMAP）

> 交接文档 · 2026-08-14
> 统一 Backlog，按 P0（致命）→ P1（重要）→ P2 → P3 → P4 → TD（技术债）分类。
> 每条尽量给出：当前状态 / 已完成 / 待完成 / 涉及模块 / 验收标准。

## 当前摘要与本轮采纳范围（2026-08-30）

- 仍处于 M1.5，目标为 V1.0 Portfolio Release；当前工作区整体证据为 **PARTIAL**。控制端 2026-08-30 分项 PASS：Java 12 类/36 个测试、前端 5 个测试、Python 13 个测试、JDK17 package/测试编译（-DskipTests，Java 测试另行运行）、前端类型检查/生产构建。未执行 npm 安装、含隔离数据库/服务启动/API smoke 的完整 Baseline 或人工 UAT。2026-08-15 Baseline PASS、2026-08-19 JUnitCore 36 个测试及前端回归仍为历史证据。
- 正式 UAT 结果汇总未填；已有非正式用户反馈和开发修复。台账 11 条（5 P0、6 P1）是已记录问题数，不是人工 UAT 发现数；001～010 `FIXED` 待人工 `RETEST`，011 `OPEN`，0 `CLOSED`。
- 011 的当前候选回归/UAT 证据仍是发布门禁；正式生产迁移、CI/Docker、灾备等留 Future Production / Commercialization，见台账子证据，不因生产设施缺失阻断 Portfolio。
- BK-14 本部门参与管理已存在，不重复立项；预览与 AI Key 当前代码事实见 `../PROJECT_CONTEXT.md`。审核豁免仍为 `NEEDS BUSINESS CONFIRMATION`，不改变既有业务决定。

| 本轮任务 | 授权范围 | 当前状态 | 完成/证据边界 |
|---|---|---|---|
| OPT-01 统一当前状态与证据 | 对齐现有治理摘要、问题来源/计数及 011 范围，不修改业务 | DOC-COMPLETE（仅文档） | 未决业务事项已列明；不代表 UAT、当前完整回归或发布通过 |
| OPT-02 对齐已有测试入口 | 对齐现有活跃回归入口与说明，不全面启动 TD-6、不恢复 legacy | 已实现；分项验证通过，独立复核通过 | 控制端报告已有实际结果，Java 兼容入口最终复跑通过且不重复计数；不标完整 Baseline PASS |

仅 OPT-01/02 获本轮采纳；[建议稿](SOP_PROJECT_OPTIMIZATION_PROPOSAL.md) 中 OPT-03～10 仍为提案，不是自主实施授权。控制端执行报告 [2026-08-30-opt01-opt02.md](verification/2026-08-30-opt01-opt02.md) 已有实际分项结果；其 PARTIAL 范围与最终复核待办不能省略。

评审修正已有分项证据：`python -B scripts/baseline/run_python_tests.py` 拒绝空套件和 skipped，最终 Python 13 项包含新增入口行为测试 11 项（Java 入口 7 + Python 入口 4）；数据库初始化前增加 Node TypeScript flag preflight，控制端在 Node 24.18.0 实测通过。独立复核未发现新增 Important/Critical 问题；不把这些分项检查当作完整 Baseline 运行。

以下已完成 BK/TD 项与带日期执行记录保留历史，不表示 2026-08-30 再次验证。

---

## 优先级定义
- **P0**：系统无法运行、数据错误、核心流程不可用
- **P1**：重要 Bug / 核心功能缺陷
- **P2**：已有功能尚未完成 / 明显功能缺陷
- **P3**：普通功能需求
- **P4**：UI、性能、代码质量、体验优化
- **TD**：技术债

---

## P0（历史 Backlog；当前缺陷与复测状态以 UAT 台账为准）

| ID | 任务 | 状态 | 说明 | 验收标准 |
|---|---|---|---|---|
| BK-01 | 固化 JDK17 编译环境说明（Codex 接手）| 已完成（2026-08-15）| baseline runner 要求 `BASELINE_JAVA_HOME` 指向 JDK17；显式设置后 Maven 3.9.16 使用 JDK17.0.2，后端打包和 18 个 Java 单测通过。未设置时本机仍会回落到 JDK25。 | Codex 首次构建即成功（已验证） |
| BK-02 | MySQL 掉线根因定位 | 部分完成（运行环境已修复，7 天验证未完成） | WER 记录 2026-08-04、2026-08-13 的 `mysqld.exe` 崩溃（`0x80000003`，故障偏移 `0xA8E7F2`），随后 error log 出现 XA crash recovery；2026-08-05 另有两次 `ibdata1` 不可写导致启动中止。2026-08-15 已将 Windows 服务绑定到 `D:\MySQL\my.ini`，实际 SQL 配置已核对；历史监控记录不足 168 小时，当前监控需通过 `DB_PASSWORD` 或 `BASELINE_DB_PASSWORD` 启动。 | 连续运行 7 天不掉线（尚未满足） |

## P1（重要）

| ID | 任务 | 状态 | 已完成 | 待完成 | 涉及模块 | 验收标准 |
|---|---|---|---|---|---|---|
| BK-03 | 清理演示题库脏题 | 已完成（2026-08-15） | 已删除 16 道明确的 APPROVED 泛健康/常识脏题及 62 条答案记录，保留血透题和历史试卷快照；复核目标脏题剩余 0。 | 演示考试抽题不再包含已确认脏题 | el_repo_qu / el_repo_qu_answer | 目标脏题剩余 0，题库仍有血透题 |
| BK-04 | 替换演示培训占位 PDF | 已完成（2026-08-15） | 使用项目空间 `00-原始资料/课件/应急预案课件/血液透析急性并发症处置方案培训（2）.pptx`，并补齐应用 `D:/upload/` 中对应物理文件。 | 真实培训文件已准备 | el_keryo_training / D:/upload | 真实 PPTX 文件存在 |
| BK-05 | 演示计划挂真实培训+演示考试 | 已完成（2026-08-15） | `t13_demo_plan` 已关联真实培训 `2084243871821561858` 和演示考试 `1915227067167539202`，状态为 `ACTIVE`。 | 学员计划同时具备培训和考试关联 | el_keryo_training_plan | courseware_id、exam_id 均非 NULL |

## P2（已有功能缺陷/半成品补全）

| ID | 任务 | 状态 | 说明 | 验收标准 |
|---|---|---|---|---|
| BK-06 | reset-pass 自助改密接口收敛或下线 | 已完成（2026-08-15） | 已下线匿名 `reset-pass` 控制器、服务方法、DTO、前端 API 及鉴权白名单；忘记密码保留为申请后管理员审核。 | 直接调用旧接口不再执行改密 |
| BK-07 | 登录验证码策略对齐 | 已完成（2026-08-15） | 已确认采用每次登录都要求图形验证码；前端始终展示，后端始终校验，避免接口绕过。 | 登录请求缺少或验证码错误时均不能登录 |
| BK-08 | 审计日志管理端查看页 | 已完成（2026-08-15）| 新增只读分页接口、管理端筛选页和幂等菜单授权脚本；支持按账号/姓名/模块/结果/时间检索 | 管理端可按时间/用户检索审计日志 |
| BK-09 | 强制改密 + 密码历史 + 90天过期 | 已完成（2026-08-15）| 旧账号回填密码时间并豁免首次强制改密；新账号/过期账号使用独立短时一次性临时改密流程；记录最近5次密码并提供7天到期提醒 | 临时令牌不返回普通 JWT；完成改密后重新登录 |
| BK-10 | API Key 环境变量化（数据库→密钥管理）| 已完成（2026-08-15）| 支持 `AI_<PROVIDER>_API_KEY` 和通用 `AI_API_KEY`；运行时环境变量优先，数据库值仅作兼容回退，yml 不保存 Key | 生产部署设置环境变量后不依赖数据库明文 Key |
| BK-11 | POI 5.x 升级 | 已完成（2026-08-15）| 已从 4.1.2 升级到 5.2.5；代码调用点完成 JDK 17 编译验证 | 依赖树仅保留 POI 5.2.5；仓库未配置 dependency-check 插件 |
| BK-12 | CSP 强制模式 | 暂缓（Future Production / Commercialization）| 安全头已注入；用户已确认当前暂不切换 | 未来存在生产或试点需求时，再进行 Report-Only 调优和强制切换决策 |

## P3（普通功能需求）

| ID | 任务 | 状态 | 说明 |
|---|---|---|---|
| BK-13 | 年度参训统计报表 | 已完成（2026-08-15） | 统计看板增加“本年度参训次数”，按年度内培训计划-学员参与关系计数并沿用数据权限范围 |
| BK-14 | 小组长参与管理培训能力 | 已完成（2026-08-15） | 小组长可打开计划参与管理，仅限本部门且明确绑定本部门的计划；候选学员仅返回本部门启用账号 |
| BK-15 | 培训计划考试下发体验完善 | 已完成（2026-08-15） | 学员培训卡片显示“含关联考试”并提供“去考试”入口；后端继续按参与计划返回考试关联 |
| BK-16 | 数据保留/归档策略落地 | 已完成（2026-08-15） | 增加审计日志、成绩、试卷及答题明细非破坏归档表和手动预览/归档入口；本轮不删除源数据 |

## P4（UI/性能/质量/体验）

| ID | 任务 | 状态 | 说明 |
|---|---|---|---|
| BK-17 | 清理根目录临时脚本 | 已完成（2026-08-15） | 11 个历史诊断脚本已归档到 `archive/diagnostics/`，不再占用根目录，不纳入应用和 baseline |
| BK-18 | 清理 .tmp_regtest 残留目录 | 已完成（2026-08-15） | 已核实项目根不存在该目录，且无代码引用，不执行删除操作 |
| BK-19 | 前端 courseware api 残留目录检查 | 已完成（2026-08-15） | 已确认 `src/api/courseware/index.ts` 无引用且仍指向废弃 `/api/courseware/*`，已删除残留文件 |
| BK-20 | 前端 E2E 脚本修复或归档 | 已归档（2026-08-15） | 7 个 legacy Playwright 脚本保留为历史资产，不纳入可重复 baseline；当前自动验收只使用 `scripts/baseline/smoke.py` |
| BK-21 | 重写 README（当前为上游原样）| 已完成（工作区已有版本） | 已核实根目录 README 已改为统一平台名说明，包含实际技术栈、启动/构建命令和文档入口；本次不覆盖用户现有修改 |

## TD（技术债）

| ID | 任务 | 说明 |
|---|---|---|
| TD-1 | JDK 版本标准化 | 全项目固定 JDK17，避免 JDK25 编译失败 |
| TD-2 | MySQL 稳定性加固 | 定位掉线根因，考虑 my.ini 调优/监控 |
| TD-3 | 数据库账号密钥化 | 已完成（2026-08-15）：dev/local 配置及备份恢复脚本改用 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 环境变量；缺少 `DB_PASSWORD` 时快速失败 |
| TD-4 | 上传路径可配置化 | 已核实关闭（2026-08-15）：`LocalUpServiceImpl` 从 `pl_plugin_data` 的 `upload-local` 配置读取 `localDir`，仓库代码无 `D:/upload/` 硬编码；实际目录属于部署数据配置 |
| TD-5 | 测试资产规范化 | 已归档（2026-08-15）：根目录 32 个 legacy Python 测试移至 `archive/tests/legacy-python/`；自动验收只保留隔离数据库上的 `scripts/baseline/smoke.py` |
| TD-6 | 单元测试补充 | 核心 Service（成绩公示/补考/申请记录）补单测 |
| TD-7 | 菜单缓存可刷新 | @Cacheable(sys:menu) 改可失效，避免改库必重启 |

---

## 里程碑规划

| 里程碑 | 目标 | 状态 |
|---|---|---|
| **M1**（2026-08）| Engineering Baseline：本地/内网可演示、主链路跑通、自动化验证通过 | 历史 Baseline PASS（2026-08-15）；当前有 2026-08-30 PARTIAL 分项证据，未重跑完整 Baseline |
| **M1.5** | Manual UAT：管理员、培训管理员/组长、学员和 Golden Path | ⬜ 当前阶段，见 `docs/UAT_MANUAL.md` |
| **M1.6** | UAT Bug Fix & Regression | 001～010 开发修复待人工 RETEST；011 OPEN，阶段未完成 |
| **M1.7** | Business Requirement Validation | ⬜ 尚未开始 |
| **M1.8** | Portfolio Polish：Demo、文档、截图、启动方式和演示流程 | ⬜ 尚未开始 |
| **M2** | V1.0 Portfolio Release | ⬜ 需要 UAT PASS、核心 Regression PASS、Demo 可运行和文档完整 |

## 历史交接建议顺序（已被上述本轮计划替代，不重复立项）

1. **BK-03/04/05（演示数据清理）**：已完成，演示链路数据已恢复为真实培训内容和关联考试。
2. **BK-06（reset-pass 收敛）**：消除残留攻击面。
3. **BK-02（MySQL 稳定性）**：环境可靠性。
4. **BK-08/09（审计查看页 + 强制改密）**：M2 合规前置。
5. **BK-13~16（业务功能增强）**：与用户确认后进入需求阶段。
## 2026-08-15 P1 执行更新

本次已完成 BK-03 的线上演示数据清理：对 `exam_keryo` 中演示题库明确识别的 16 道 APPROVED 泛健康/常识脏题及其 62 条答案记录执行删除；历史 `el_paper_qu` 试卷快照保留。复核结果为目标脏题剩余 0，道库仍保留血液透析相关题目。

BK-04 已完成：使用项目空间 `00-原始资料/课件/应急预案课件/血液透析急性并发症处置方案培训（2）.pptx`，并补齐应用上传目录中的实际文件。BK-05 已完成：`t13_demo_plan` 已关联真实培训 `2084243871821561858` 和演示考试 `1915227067167539202`，状态为 `ACTIVE`。

本节为本次执行结果，优先于上方交接时的待办描述。历史 MySQL 监控记录未达到 168 小时；当前不能标记为 7 天稳定性 PASS。重新启动监控前必须设置 `DB_PASSWORD` 或 `BASELINE_DB_PASSWORD`，且不得把凭据写入仓库。

## 2026-08-15 P2 BK-11 执行更新

BK-11 已完成：后端 Apache POI 从 4.1.2 升级到 5.2.5，保留现有 Excel 导入、导出和文件安全解析调用方式，仅更新过时的兼容性说明。JDK 17 下 `compile`、`package -DskipTests`、POI 依赖树核验均通过；依赖树确认 `poi`、`poi-ooxml` 和 `poi-ooxml-lite` 均为 5.2.5，现有 Java 测试保持 `OK (18 tests)`。仓库当前未配置 OWASP dependency-check 插件，本次未将未执行的扫描记为通过。

## 2026-08-15 P3 BK-15 执行更新

BK-15 已完成：学员端 `/api/keryo/training/my` 返回计划关联的 `examId`，培训卡片在存在关联考试时显示“含关联考试”和“去考试”入口，点击后复用现有学员考试详情路由。未新增数据库字段、权限规则或通知系统。后端编译、前端类型检查、前端生产构建及现有 Java 测试均通过。

## 2026-08-15 Baseline 与 E2E 执行更新

clean baseline 已在隔离前端临时目录完成 npm 依赖安装、后端打包、前端类型检查/生产构建、Java 18 个单测、服务健康检查和 Python 核心 API smoke。npm 安装使用 `--legacy-peer-deps`，因为 npm 11 会拒绝锁定的 Vue 3.3.4 与 Element Plus 2.14.x peer 范围；该参数未升级或修改业务依赖。

legacy Playwright E2E 已正式归档：脚本保留、不删除、不纳入 baseline 测试；baseline 将其记录为 `ARCHIVED`，不再因为归档项导致整体失败。当前自动验收只要求隔离数据库上的 Python baseline smoke。

## 2026-08-15 Portfolio Release 目标调整（目标仍有效，状态为历史快照）

本项目当前不以正式医疗机构商业生产交付为目标。最终目标调整为 **V1.0 Portfolio Release**：基于真实医护培训业务需求，形成具备完整业务闭环、经过 Engineering Baseline、人工 UAT、Bug 修复、回归测试、业务需求回验、Demo 部署和项目总结的求职作品集项目。

当日状态记录如下；2026-08-30 现状以上方摘要为准：

- Engineering Baseline：**PASS**；
- Automated Baseline Verification：**PASS**；
- Manual UAT：**NOT STARTED**；
- Business Requirement Validation：**NOT COMPLETED**；
- Portfolio Release：**NOT READY**；
- Production Readiness：**NOT REQUIRED AT CURRENT STAGE**；
- Commercial Delivery Readiness：**OUT OF CURRENT SCOPE**。

`BASELINE_PASS` 不代表业务验收完成。当前执行入口和记录机制见：

- `docs/PORTFOLIO_RELEASE.md`
- `docs/UAT_MANUAL.md`
- `docs/UAT_BUG_LEDGER.md`
- `docs/CASE_STUDY_LOG.md`

## M1.5～M2 Portfolio Release Roadmap

| 阶段 | 目标 | 当前状态 | 完成标准 |
|---|---|---|---|
| M1.5 Manual UAT | 项目负责人按真实用户视角完成管理员、培训管理员/组长、学员和 Golden Path 操作 | 当前阶段；正式汇总未填，已有非正式反馈 | UAT Case 有实际结果；异常、权限和体验问题有记录 |
| M1.6 UAT Bug Fix & Regression | 维护 Bug Ledger，按 P0/P1/P2/P3 修复并回归 | 001～010 FIXED 待人工 RETEST；011 OPEN | 2026-08-19 回归为历史证据；2026-08-30 PARTIAL 分项回归通过，仍需完整候选证据和人工复测，不能提前 CLOSED |
| M1.7 Business Requirement Validation | 重新核对真实医护培训流程、角色权限和业务假设 | 未开始 | 业务疑问标记【需要业务确认】并形成结论 |
| M1.8 Portfolio Polish | Demo 数据、必要 UI/UX、README、架构说明、启动方式、截图和演示流程 | 未开始 | 他人可按文档部署并完成 Demo |
| M2 Portfolio Release | 形成 **V1.0 Portfolio Release** | 未开始 | 核心闭环可运行、Baseline PASS、UAT PASS、核心 Regression PASS、Demo 可运行、文档完整 |

本阶段不主动执行 TD-6、TD-7，除非人工 UAT 证明它们直接阻塞核心流程。MySQL 7 天稳定性验证独立运行但尚未完成，CSP 强制模式暂不切换。

## 2026-08-19 P0/P1 安全与业务缺陷执行更新

本节保留当日开发记录，非 2026-08-30 重新执行结果；011 当前/未来范围按上方摘要与台账子证据区分。

- 已修复：空密码登录绕过、JWT 硬编码/解码信任、refresh 签名与用户状态、管理写接口授权、培训计划参与资格、培训/考试直达资源越权、统计口径、计划自动结束、Origin/CORS/验证码 debug、上传魔术字节/路径穿越、忘记密码写入异常吞错。
- 回归证据：JDK17 后端 `test-compile` PASS；JUnitCore `OK (36 tests)`；前端 `npm run ts:check` 和 `npm run build:test` PASS。
- UAT 台账 `BUG-UAT-001～010` 状态为 `FIXED`，等待原发现人/项目负责人人工 RETEST；`BUG-UAT-011`（生产迁移、CI/Docker、灾备、备份介质、人工 UAT）保持 OPEN。

## Future Production / Commercialization Backlog

以下内容保留，不删除，但不属于当前 Portfolio Release 的验收范围。未来如果出现真实采购方、试点机构或正式生产需求，再重新立项、确认预算、业务责任和安全要求：

- 正式医疗生产环境交付；
- 公网部署、HTTPS、备案和生产级基础设施；
- 完整渗透测试、ZAP 全量扫描和正式安全评估；
- 正式 DPIA、等保及其他合规材料；
- 正式审计交付、SLA、备份恢复演练和生产监控；
- CSP Report-Only 调优后切换强制模式；
- 当前 Portfolio Release 不需要的复杂安全建设和长期运营能力。

这些工作不是当前项目“未完成的核心功能”，而是未来商业化条件成立后的交付 backlog。

