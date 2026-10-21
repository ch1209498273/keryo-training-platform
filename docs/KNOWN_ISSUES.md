# 已知问题与技术债（KNOWN_ISSUES）

> 交接文档 · 2026-08-14
> 已扫描：TODO/FIXME、临时代码、hardcode、安全风险、数据一致性、测试缺口、构建 warning。
> 记录为主，**不在本轮修复**。优先级对应 ROADMAP。

## 当前证据摘要（2026-08-30）

当前工作区整体证据为 **PARTIAL**：控制端 2026-08-30 已记录 Java 12 类/36 个测试、前端 5 个测试、Python 13 个测试、JDK17 package/测试编译、前端类型检查/生产构建分项 PASS。package 使用 -DskipTests，Java 测试另行运行；前端使用已有依赖。未执行 npm 安装、含隔离数据库/服务启动/API smoke 的完整 Baseline 或人工 UAT。本 sidecar 仅据报告更新文档，不自行运行测试/构建。

2026-08-15 Baseline PASS 与 2026-08-19 36 个测试/前端回归通过仍为历史记录；下文未单列执行日期的“已验证”也保留为历史描述，不用新分项结果追认所有旧结论。

正式 UAT 结果汇总未填，已有非正式用户反馈与开发修复；台账记录 11 条（5 P0、6 P1），001～010 `FIXED` 待人工 `RETEST`，011 `OPEN`，0 `CLOSED`，不等于人工 UAT 发现数量。历史 K/TD/R 关闭项也不计入 UAT CLOSED 数量。

011 按 `UAT_BUG_LEDGER.md` 子证据分开：当前候选回归与人工 UAT 缺证据仍阻断发布；正式生产迁移、CI/Docker、灾备留未来商业化，不因生产设施缺失阻断 Portfolio。审核豁免保持 `NEEDS BUSINESS CONFIRMATION`，见 `../DECISIONS.md`，不变更既有业务决定。

本轮仅采纳 OPT-01/02。OPT-02 已实现，**分项验证通过，独立复核通过**；控制端执行报告 [2026-08-30-opt01-opt02.md](verification/2026-08-30-opt01-opt02.md) 已记录实际结果与初次失败/修复过程，不等于完整 Baseline PASS。

评审发现的 Python 空套件退出 0 假通过已由控制端修正并验证：`python -B scripts/baseline/run_python_tests.py` 拒绝空套件和 skipped，13 项通过（原启动器 2 项 + Java 入口 7 项 + Python 入口 4 项）。Node TypeScript flag preflight 已置于数据库初始化之前，当前 Node 24.18.0 分项执行通过；独立复核未发现新增 Important/Critical 问题，不推断其他 Node 版本兼容或完整流程通过。

UAT 编号提示：现有 `docs/UAT_MANUAL.md` 的旧 FAIL 示例仍从 `BUG-UAT-001` 起写；正式人工 UAT 时，复测既有问题应回填原台账条目，发现新问题应使用台账中下一个未占用编号。该手册提示待后续文档维护，不新增或关闭 Bug。

---

## 1. Bug（历史处理记录；当前问题状态见 UAT 台账）

| ID | 问题 | 影响 | 定位 | 优先级 |
|---|---|---|---|---|
| K-01 | 已处理：演示题库中明确识别的 16 道 APPROVED 泛健康/常识脏题及 62 条答案记录已删除 | 目标脏题剩余 0；历史试卷快照保留 | el_repo_qu / el_repo_qu_answer（repo_id=1910524655864012801） | 已关闭 |
| K-02 | 已处理：演示计划使用的真实血液透析 PPTX 已补齐到当前上传目录 | 真实培训文件可供后端下载 | el_keryo_training；物理文件 D:/upload/ | 已关闭 |
| K-03 | 已处理：演示培训计划 t13_demo_plan 已关联真实培训和演示考试 | 计划具备培训与考试关联 | el_keryo_training_plan | 已关闭 |
| K-04 | 已处理：匿名 `reset-pass` 自助改密接口已下线，忘记密码改为管理员审核 | 旧接口不再执行密码修改 | SysUserController / ShiroConfig / JwtFilter | 已关闭 |
| K-05 | 已处理：登录始终要求图形验证码，前后端策略已统一 | 每次登录均校验验证码 | LoginForm.vue / SysUserServiceImpl.login | 已关闭 |
| K-06 | 已处理：baseline runner 登录 smoke 已稳定通过 | 根因包括长时间运行子进程的 stdout/stderr 管道未及时消费、启动参数中的 JDBC 配置未可靠覆盖 datasource，以及仅启用 baseline profile 时未加载内存 Redis。现已异步消费输出、预检端口、使用 JVM `-D` 数据源参数并保留 `dev,baseline` profile；完整 runner、三角色登录和核心 API smoke 已通过 | scripts/baseline/run_baseline.ps1；scripts/baseline/smoke.py；baseline-report/baseline-20260815-183104.md | 已关闭 |

## 2. 技术债（Technical Debt）

| ID | 债务 | 说明 | 优先级 |
|---|---|---|---|
| TD-1 | **JDK 版本陷阱** | 本机默认 JDK25，Lombok 1.18.38 在 JDK25 下注解失效；编译/启动必须 `JAVA_HOME=D:/software/jdk-17.0.2`。Codex 接手**必读**。baseline 已验证显式 JDK17 路径可构建。 | P0 环境 |
| TD-2 | **MySQL 偶发掉线** | 已确认 2026-08-04、2026-08-13 存在 `mysqld.exe` WER 崩溃，异常码 `0x80000003`、故障偏移 `0xA8E7F2`；2026-08-14 error log 出现 XA crash recovery。2026-08-05 还有两次 `ibdata1` 不可写导致启动中止。2026-08-15 已修复服务配置未加载问题，实测 SQL 配置正确；历史监控记录不足 168 小时，当前需通过 `DB_PASSWORD` 或 `BASELINE_DB_PASSWORD` 重新启动监控。 | P0 环境（7 天验证未完成） |
| TD-3 | 数据库账号密码明文写死在 yml | 已处理：dev/local 配置及备份恢复脚本改用 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 环境变量；缺少 `DB_PASSWORD` 时启动/操作会快速失败 | 已完成（2026-08-15） |
| TD-4 | 上传文件 localDir 硬编码 `D:/upload/` | 已核实：`LocalUpServiceImpl` 从 `pl_plugin_data` 的 `upload-local` 配置读取 `localDir`，仓库代码无该路径硬编码；生产只需维护部署配置 | 已关闭（2026-08-15） |
| TD-5 | 测试资产已正式归档 | 根目录 32 个 legacy Python 脚本和 7 个 Playwright 脚本均保留在归档目录，不纳入验收；当前自动验收只使用隔离数据库上的 `scripts/baseline/smoke.py` | 已归档（2026-08-15） |
| TD-6 | 离线 surefire 缺 junit-platform provider | 2026-08-19 的 12 类/36 个测试为历史记录；2026-08-30 统一 Java runner 已按源文件发现并执行 12 类/36 个测试通过，补齐现有 spring-test 6.1.2 测试运行依赖；分项验证通过且独立复核通过，不等于全面测试债治理完成 | P3 |
| TD-7 | 菜单改动后需重启后端（@Cacheable 内存缓存）| 改库直接不生效，排查易踩坑 | P3 |
| TD-8 | 前端 api 模块残留 courseware 目录 | 已确认无引用并删除 `src/api/courseware/index.ts`，培训 API 保留在 `src/api/training/` | 已处理 |
| TD-9 | 根目录诊断脚本 | 已归档到 `archive/diagnostics/`，保留历史文件但不纳入应用和 baseline | 已处理 |
| TD-10 | 项目根 `.tmp_regtest` 临时目录 | 已核实不存在，且无代码引用 | 已处理 |
| TD-11 | 演示数据/测试脚本会污染数据库（test_t*.py 跑多次数据翻倍）| 跑测试前需先清数据 | P3 |
| TD-12 | 已处理：审计日志、成绩、试卷及答题明细已有非破坏归档策略 | 通过 `RetentionController` 预览/归档，源表保留；自动删除仍不在本轮范围 | P3 已完成（归档基础） |
| TD-13 | 已解除：真实培训材料位于项目空间 `00-原始资料`，本次已使用血液透析培训 PPTX | 真实文件已准备并关联演示计划 | 已关闭 |

## 3. 风险

| ID | 风险 | 等级 | 说明 |
|---|---|---|---|
| R-1 | MySQL 稳定性 | 高 | 历史上存在 mysqld 崩溃和一次启动写权限异常；服务配置绑定已修复，但连续 7 天稳定性验证尚未完成，仍不可视为生产级稳定。|
| R-2 | 演示数据污染 | 中 | 已确认的脏题、占位培训和计划关联问题已按 BK-03/04/05 处理；仍需在人工 UAT 中复核展示和文件打开体验。 |
| R-3 | 已关闭：reset-pass 残留攻击面 | — | 旧匿名改密路由已移除，保留 forget-check 申请流程 |
| R-4 | 密码/账号硬编码配置 | 已降低 | 数据库口令已改为环境变量注入；生产仍应使用专用数据库账号和密钥管理 |
| R-5 | 已关闭：审计日志无查看页 | — | BK-08 已提供只读分页接口和管理端按账号/时间检索页面；日志表仍禁止删除 |
| R-6 | 当前文件预览的实际渲染/权限待验证 | 中 | 2026-08-30 静态核对：`yf-bev2-vue/src/views/Training/components/FilePreview.vue` 使用 docx-preview、pptx-preview 与浏览器原生 PDF，管理端/学员端均引用；旧 Office Online Viewer 公网依赖描述为历史。代码存在不证明实际文件渲染通过，仍需 UAT |
| R-7 | AI Key 安全性 | 中 | 已支持 `AI_<PROVIDER>_API_KEY`/`AI_API_KEY` 环境变量优先；数据库值仍保留兼容回退，生产应只配置环境变量并逐步清空 DB Key |
| R-8 | 单测覆盖仍有限 | 中 | 2026-08-19 已增加 JWT、refresh、登录空密码、忘记密码写入失败、Origin、计划成员资格、文件魔术字节/路径穿越等 12 个测试类共 36 项；仍不替代人工 UAT 和完整业务覆盖 |

## 4. 临时代码 / 疑似无效

- `ForgetPasswordForm.vue` 与 `Login.vue` 的 forget 视图：采用管理员审核方案；旧 `reset-pass` 后端/前端代码已下线。
- `yf-bev2-vue/src/api/courseware/`：历史记录项；BK-19 已核实无引用并删除，当前不应作为未完成任务重复处理。
- 根目录 `check_exam_detail.js`、`gen_xlsx.py`、`grant_courseware_menu.sql`、`batch_upload_courseware.py`：一次性工具脚本。
- `README.md`（当前根目录）已更新为项目说明；历史交接文档中的“上游原样”描述已过时。

## 5. 数据一致性 / 潜在问题

- `el_repo.cat_id` 引用字典 `repo_catalog` 值：分类精简后「演示」题库已归「其他」（2089000000000000002），无悬空引用（已核对）。
- `el_keryo_training_plan.exam_id` 可为 NULL：部分计划未关联考试，学员端考试列表按计划过滤后为空（K-03）。
- `el_sys_user.dept_code` 与 `el_sys_depart.dept_code`：学员可自选中心，后端已校验存在性；历史数据可能有 dept_code 不在 depart 的（如 student=A01 不存在于 depart 树）。
- `el_exam_record` 数据随测试累积，无清理策略。

## 6. 前后端接口一致性

- 前端 api 统一走 `@/config/axios` + `request.post({url, data})`；**不能用 `@/api/request`**（Vite 静默失败，已知坑）。
- 登录参数 `userName`（非 username）；成功码 `code=0`。
- 接口入参对象：考试 preCheck/create 用 `{id: examId}`（BaseIdReqDTO），不是 `examId` 字段。

## 7. 构建/编译 warning

- Lombok `@EqualsAndHashCode` 未声明 callSuper 的类有 warning（不影响构建）。
- 后端编译用 JDK17 时仅常规 warning；JDK25 下会编译失败（TD-1）。
- 前端 `defineExpose` 编译器宏 import 冗余 warning（vite 日志可见）。

## 8. 缺少测试的关键逻辑

- 忘记密码申请流程（forget-check → 写记录 → 管理端处理）——写入失败异常已有 `ForgetPassServiceTest` 负向回归；完整管理端流程仍需人工 UAT。
- 成绩公示掩码（未公示时直接调 /paper/detail、/full-detail 拿不到分数）——已手工验证。
- 学员端考试列表按计划过滤——已手工验证。
- 年度参训统计与小组长本部门参与管理——已完成代码/编译回归，业务口径与体验仍需人工 UAT。
- 培训计划审批状态机流转（DRAFT→...→ARCHIVED）——有 T-06 脚本覆盖。
- 注册必填/密码规则边界（特殊字符/纯数字/超长）——已手工验证。

## 2026-08-19 P0/P1 安全与业务边界修复

本节保留 2026-08-19 的历史工程记录；2026-08-30 的新分项结果另见当前摘要及执行报告，不改写历史。011 范围按当前摘要与台账子证据区分。

- 空密码、JWT 签名/外置密钥、refresh 用户状态、管理写接口授权、考试计划参与资格、统计口径、自动结束、Origin/CORS、验证码 debug、上传类型/路径和忘记密码异常均已完成开发修复。
- JDK17 后端 `test-compile` PASS；JUnitCore 回归 `OK (36 tests)`；前端 `npm run ts:check` 与 `npm run build:test` PASS。
- 对应 UAT 台账 `BUG-UAT-001～010` 当前为 `FIXED`，等待原发现人/项目负责人按原步骤 RETEST；不将本轮自动回归写成 `CLOSED`。
- `BUG-UAT-011`（生产迁移、CI/Docker、灾备、备份介质和人工 UAT 证据）继续保持 OPEN。
## 2026-08-15 P1 执行更新

- K-01：已处理。演示题库中明确识别的 16 道 APPROVED 泛健康/常识脏题及 62 条答案记录已从 `el_repo_qu` / `el_repo_qu_answer` 删除；历史试卷快照保留。
- K-02：已处理。演示计划使用的真实血液透析 PPTX 已补齐到当前 `D:/upload/` 上传目录。
- K-03：已处理。`t13_demo_plan` 已关联真实培训和演示考试。
- TD-13：已解除阻塞。真实培训材料位于项目空间 `00-原始资料`，本次已使用其中的血液透析培训 PPTX。

