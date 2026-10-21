# Portfolio Case Study 数据记录

> 目的：为后续求职作品集和项目复盘保留真实、可追溯的数据。
>
> 规则：只记录实际发生并能找到证据的数据；没有执行就写“NOT STARTED/待记录”，不要为了作品集效果填充漂亮数字。

> 当前摘要（2026-08-30）：控制端已记录当前工作区 PARTIAL 分项回归通过，OPT-02 已实现且独立复核通过；完整 Baseline 未重跑，正式 UAT 结果汇总未填，已有非正式用户反馈、历史工程评估和开发修复。2026-08-15 Baseline PASS 与 2026-08-19 36 个测试/前端回归仍保留为历史证据。

## 1. 项目定位

| 项目 | 当前记录 |
|---|---|
| 项目名称 | 血透中心医护培训平台 |
| 目标版本 | V1.0 Portfolio Release |
| 当前定位 | 基于真实业务需求的可部署、可演示 Portfolio Release |
| 正式采购方 | 当前没有 |
| 正式生产交付 | OUT OF CURRENT SCOPE |
| 当前阶段 | M1.5 Manual UAT；正式结果待补，001～010 开发修复待人工 RETEST |

## 2. Baseline 事实记录（2026-08-15 历史快照）

本表保留当日结果及未开始项，不代表 2026-08-30 状态；当前台账统计见第 3 节，后续历史开发回归见第 11 节。

| 指标 | 结果 | 证据 |
|---|---|---|
| Engineering Baseline | PASS | `baseline-report/baseline-20260815-232130.md` |
| Automated Baseline Verification | PASS | 同上 |
| 后端 Java 单元测试 | 18 个通过 | Baseline 报告 `java-unit-tests` |
| 前端干净依赖安装 | PASS | Baseline 报告 `frontend-install` |
| 前端类型检查 | PASS | Baseline 报告 `frontend-typecheck` |
| 前端生产构建 | PASS | Baseline 报告 `frontend-build` |
| Python API smoke | PASS | Baseline 报告 `api-smoke-python` |
| 数据库从零初始化与校验 | PASS | Baseline 报告 `mysql_start`、`baseline-verify` |
| Legacy Python/Playwright | ARCHIVED | 当前只保留 `scripts/baseline/smoke.py` 作为验收入口 |
| Manual UAT Case 数量 | NOT STARTED | 待 `docs/UAT_MANUAL.md` 执行后填写 |
| UAT Bug 数量 | NOT STARTED | 没有执行 UAT 时不能写 0 |
| Regression 结果 | NOT STARTED | 待 UAT Bug 修复后填写 |

## 3. UAT、台账和回归统计（2026-08-30）

人工用例数量、人工发现数、已记录问题数和自动测试数分别统计；不得从台账或历史回归倒推人工 UAT 结果。

| 指标 | 数值 | 统计口径/证据 |
|---|---:|---|
| 计划 UAT Case 数量 | 15 | `docs/UAT_MANUAL.md` UAT-001～UAT-015 |
| 已执行 UAT Case 数量 | 待记录 | 以 UAT 汇总表为准 |
| PASS Case 数量 | 待记录 | 以 UAT 汇总表为准 |
| FAIL Case 数量 | 待记录 | 以 UAT 汇总表为准 |
| 人工 UAT 发现问题数 | 待记录 | 正式结果汇总未填，逐项发现方式/报告人尚未完整记录，不能用台账总数替代 |
| 台账已记录问题总数 | 11 | `docs/UAT_BUG_LEDGER.md` 索引；历史工程评估等来源，不全归因人工 UAT |
| 台账 P0 / P1 / P2 / P3 | 5 / 6 / 0 / 0 | 仅统计 11 个父问题；011 子证据不新增计数 |
| 台账 FIXED 数量 | 10 | 001～010 保留开发修复状态，等待人工 RETEST |
| 台账 OPEN 数量 | 1 | 011 保留 OPEN；当前候选/UAT 与未来生产证据分开 |
| 台账 CLOSED 数量 | 0 | 未记录人工复测通过及关闭证据；不同于历史 K/TD 关闭项 |
| 核心 Regression Case 数量 | 待记录 | UAT 修复后的回归清单 |
| 当前工作区分项测试通过数量 | 54 | 控制端 2026-08-30 实测：Java 36 + 前端 5 + Python 13；是测试方法数，不是业务场景数或覆盖率；整体 PARTIAL，独立复核通过 |
| Java 活跃测试 | 36（12 类） | JUnitCore `OK (36 tests)`，独立于 package 的 -DskipTests 构建 |
| 前端活跃测试 | 5（2 个文件） | `npm test`，skipped=0；不证明真实登录耗时或课件渲染 |
| Python 活跃本地测试 | 13 | `python -B scripts/baseline/run_python_tests.py`：启动器 2 项 + Java 入口 7 项 + Python 入口 4 项，skipped=0；新增 11 项验证入口可靠性，不是新增业务覆盖或 API smoke |
| 需求变更数量 | 待记录 | 以业务确认记录为准 |

控制端最终 Java 兼容入口复跑仍为 12 类/36 个测试通过，验证了委托链路，不重复累加为 72 个；最终测试方法数为 54。Python runner 已拒绝空套件和 skipped；数据库初始化前的 Node TypeScript flag preflight 在 Node 24.18.0 分项实测通过。独立复核未发现新增 Important/Critical 问题，入口可靠性检查不等于业务/UAT 覆盖。

本轮仅采纳 OPT-01/02；OPT-01 文档完成，OPT-02 已实现，**分项验证通过，独立复核通过**。控制端执行报告 [2026-08-30-opt01-opt02.md](verification/2026-08-30-opt01-opt02.md) 已记录实际命令、计数、失败/修复与范围。除上述测试外，JDK17 新目录 package/测试编译（-DskipTests）、前端类型检查与生产构建 PASS；前端使用已有依赖。未执行 npm 安装、含隔离数据库/服务启动/API smoke 的完整 Baseline 或人工 UAT，因此整体证据为 PARTIAL。此 sidecar 仅读取结果并更新文档，不自行运行构建/测试。审核豁免等保留 `NEEDS BUSINESS CONFIRMATION`，见 `../DECISIONS.md`。

## 4. 重要技术决策

| 日期 | 决策 | 原因/证据 | 影响 |
|---|---|---|---|
| 2026-08-15 | 使用 JDK 17 作为后端 Baseline 环境 | JDK 25 会造成旧依赖编译问题 | 固定构建入口和环境变量 |
| 2026-08-15 | POI 升级到 5.2.5 | 用户批准，JDK17 下构建和测试通过 | 保持现有 Excel 能力，暂不做架构升级 |
| 2026-08-15 | Legacy Playwright 正式归档 | 无稳定运行条件，当前只需可重复 Baseline | `scripts/baseline/smoke.py` 成为自动验收入口 |
| 2026-08-15 | CSP 暂不切换强制模式 | 用户明确暂不切换 | 保留为 Future Production / Commercialization 决策 |
| 2026-08-15 | 正式生产交付不纳入当前目标 | 当前没有采购方或试点机构 | 转入 Future Production / Commercialization Backlog |

## 5. AI Agent 与人工判断记录

只记录真实发生的事件，说明“Agent 做了什么、哪里错、人工如何纠正”。

| 日期 | Agent/阶段 | 典型错误或风险 | 人工纠正 | 最终结果 |
|---|---|---|---|---|
| 2026-08-15 | Codex Baseline | 将已归档 E2E 作为 `SKIP` 并导致整体失败 | 按用户确认改为 `ARCHIVED`，只保留 baseline smoke | Baseline PASS |
| 2026-08-15 | Codex Baseline | WindowsApps `python.exe` 占位符被误识别为 Python | 增加 `python --version` 可执行性校验并选择真实 Conda 环境 | Python smoke PASS |
| 2026-08-15 | Codex Baseline | MySQL 临时 datadir 使用含中文路径导致启动解析失败 | 使用环境变量传入 ASCII 临时目录，并修正参数传递 | MySQL 初始化 PASS |
| 2026-08-14～15 | WorkBuddy → Codex | 交接资料、代码和运行环境需要逐项核对 | 以实际代码、Baseline 和 Roadmap 重新验证 | 形成当前 Portfolio Release 计划 |

## 6. 业务反馈记录

每次有可追溯的业务反馈就新增一行，并区分非正式反馈与正式 UAT 结果：

| 日期 | 角色/业务方 | 反馈原文或准确转述 | 影响 | 是否形成需求变更 | 处理决定 |
|---|---|---|---|---|---|
| — | — | 正式 UAT 结果汇总未填；已有非正式用户反馈，但本表尚待补充可追溯原文、日期和来源 | 待记录 | 待确认 | 不猜测报告人或反馈内容 |

不能把“没有记录”写成“业务方没有意见”。

## 7. 最终项目规模记录

Portfolio Release 冻结后再填写，避免中途制造虚假指标：

- 后端 Java 文件数：待测量；
- 前端 TypeScript/Vue 文件数：待测量；
- 数据库表数量：待测量；
- API 数量：待测量；
- 自动化测试数量和类型：以最终仓库和报告为准；
- UAT Case 数量：以最终 UAT 手册和执行记录为准；
- 最终功能范围：以 `docs/FEATURE_STATUS.md` 和业务回验结果为准。

## 8. 更新方式

每完成一个 UAT Case：

1. 更新 `docs/UAT_MANUAL.md` 汇总表；
2. 如果失败，新增或更新 `docs/UAT_BUG_LEDGER.md`；
3. 如果涉及业务规则，记录【需要业务确认】和确认结论；
4. 不覆盖历史结果，只追加日期、证据和修复/回归结果；
5. 阶段结束后再更新本文件的统计数字。

## 9. 2026-08-18 治理与当前工作区复核

历史记录（含 2026-08-19 后续复核）；表内“当前/本轮”仅指记录当时，不代表 2026-08-30 已重新验证。

| 项目 | 当前事实 | 证据/影响 |
|---|---|---|
| 核心治理文档 | 已生成 `AGENTS.md`、`PROJECT_CONTEXT.md`、`DECISIONS.md`、`LESSONS_LEARNED.md` | 明确宪法、背景、决策和经验的责任边界 |
| 后端当前工作区复核 | 2026-08-19 JDK17 `test-compile` PASS；JUnitCore 36 个测试 PASS | 仅代表本轮安全回归，不替代完整 Baseline |
| 前端当前工作区复核 | 2026-08-19 `npm run ts:check`、`npm run build:test` PASS | 生成 `dist-test`；仍不替代人工 UAT |
| MySQL 7 天验证 | 尚未完成 | 历史记录不足 168 小时；监控脚本要求环境变量凭据 |
| Manual UAT | NOT STARTED | 待项目负责人提供实际 UAT Case 结果后再更新统计 |

本节只记录已发生事实，不预填 UAT 数量、Bug 数量或回归通过率。

## 11. 2026-08-19 安全与业务缺陷回归

保留 2026-08-19 历史开发回归记录；2026-08-30 新分项结果另见第 3 节与控制端执行报告。011 的当前候选/UAT 门禁与未来正式生产迁移、CI/Docker、灾备分开，详见台账子证据；不因未来生产项缺失阻断 Portfolio。

| 项目 | 当前事实 | 证据/影响 |
|---|---|---|
| 修复范围 | BUG-UAT-001～010 已完成开发修复 | 登录/JWT/refresh、角色与资源边界、考试参与资格、统计、计划调度、Origin/CORS、验证码 debug、上传和忘记密码 |
| 后端回归 | JDK17 `mvn -o -DskipTests test-compile`；JUnitCore `OK (36 tests)` | 不连接生产数据库、不写入业务数据 |
| 前端回归 | `npm run ts:check`；`npm run build:test` | 当前工作区类型检查和测试构建通过 |
| 台账状态 | 001～010=`FIXED`；011=`OPEN` | 负责人仍需按原 UAT 步骤 RETEST；生产/CI/灾备证据不伪造 |

## 10. 2026-08-19 Windows 本地启动入口复核

保留当日启动器记录；2026-08-30 控制端 Python 活跃本地测试含启动器 2 项，详见第 3 节，但不等于完整服务启动验证；此 sidecar 未修改或运行启动器。

| 项目 | 当前事实 | 证据/影响 |
|---|---|---|
| 原因 | 双击入口依赖固定 Git Bash/项目路径，并在 Windows 环境缺少 DB_PASSWORD 时提前退出 | 复现退出码 1；3306、8000、9091 当时均未监听 |
| 最小修复 | 启动服务器.bat 改为从自身目录调用 start_all.py；脚本自动寻找可验证的 Python/Conda、JDK17 和 npm | 不修改业务代码或数据库结构 |
| 凭据处理 | 缺少 DB_PASSWORD 时使用不回显的本地提示；非交互运行明确失败 | 不写入仓库、不写入启动命令 |
| 启动判定 | MySQL、后端、前端分别做端口检查，并对后端/前端做 HTTP 检查 | 不再只依赖固定等待时间 |
| 当前验证 | Python 语法通过；无密码路径明确失败并保留窗口；完整三服务启动尚待本机输入有效数据库口令后复核 | 不将未完成的完整启动写成 PASS |

