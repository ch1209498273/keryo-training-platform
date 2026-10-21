# 数据/知识、Prompt、模型、代码、评测集、发布记录：统一版本台账

台账版本：v0.5.5｜首次登记：2026-08-31｜最近增量：2026-10-13｜范围：Keryo V1.0 Portfolio Release。

本台账把六类资产通过固定版本 ID 关联起来，回答“这次评测/发布用了什么、依据在哪、谁审核、能否回退”。首次登记是文档与文件盘点；当前增量记录了 AI-02～AI-05 的工程实现与 Mock/定向回归证据。**这不是发布、真实模型质量评测或人工 UAT。**

2026-09-11 的 DeepSeek V4.1 Flash 配置升级、clean npm ci 和工程验证证据已登记到 `SNAP-20260911-001` / `BUNDLE-20260911-001`；模型 API ID 为 `deepseek-flash`，但未执行真实模型调用。详见 [模型升级验证记录](verification/2026-09-11-deepseek-v41-flash.md) 和 [clean npm 记录](verification/2026-09-11-clean-npm-install.md)。

台账自动盘点：[SNAP-20260911-001](version-ledger/SNAP-20260911-001.files.json) / `BUNDLE-20260911-001`；累计受控文件由 696 个增量到 701 个，登记 DeepSeek V4.1 Flash 配置、clean npm ci 证据和治理文档；只保存安全指纹与既有报告引用，未在本次维护中调用真实模型、运行数据库或应用测试。详见 [核验报告](version-ledger/VERIFICATION-20260912-100000.json)。

2026-09-20 新增 [WorkBuddy 接手交接文档](HANDOVER_TO_WORKBUDDY.md)，仅作为治理/交接知识源登记到 `KNOW-RULES@v0.3.4`；新增不可覆盖快照 [SNAP-20260920-001](version-ledger/SNAP-20260920-001.files.json)、`BUNDLE-20260920-001` 和 [独立核验报告](version-ledger/VERIFICATION-20260920-180200.json)。本轮未运行应用、数据库、模型、评测或人工 UAT，发布状态仍为 `NOT_READY`。

2026-09-21 提交 `2f510c3` 登记了人工 UAT 前的代码修复，新增 `CODE-APP@v0.3.5`、[SNAP-20260921-001](version-ledger/SNAP-20260921-001.files.json)、`BUNDLE-20260921-001` 和 [独立核验报告](version-ledger/VERIFICATION-20260921-180032.json)。提交说明声称 13 条接口级回归检查，本轮未重跑或导入外部证据，也未将其改写为人工 UAT 通过；发布状态仍为 `NOT_READY`。

## 1. 唯一入口与事实边界

| 文件 | 用途 |
|---|---|
| [ledger.json](version-ledger/ledger.json) | 唯一结构化记录：资产版本、组合、评测运行、发布与待补项；后续在此追加 |
| [SNAP-20260831-001.files.json](version-ledger/SNAP-20260831-001.files.json) | 本次文件路径、字节数、SHA-256、Git 基点及快照范围 |
| [SNAP-20260831-002.files.json](version-ledger/SNAP-20260831-002.files.json) | AI-02～AI-04 实施范围的选定文件、字节数、SHA-256、Git 基点及快照范围 |
| [SNAP-20260831-003.files.json](version-ledger/SNAP-20260831-003.files.json) | AI-05 批次状态/幂等/恢复实现与隔离 V8 迁移的选定文件、SHA-256、Git 基点及快照范围 |
| [entry-templates.json](version-ledger/entry-templates.json) | 可复制的资产、评测运行和发布记录模板；模板不计入正式记录 |
| [VERIFICATION-20260831.json](version-ledger/VERIFICATION-20260831.json) | 本轮结构/引用核验，以及源文件相对首次快照的变化明细 |
| 本文 | 盘点摘要、字段字典、维护规则；与 JSON 同步，不另建第二套事实表 |

文件路径以代码仓库 `workspace/keryo-training/` 为根。敏感材料只登记受控位置与摘要，不将原文、患者信息、密码、Token、API Key 或完整运行 Prompt 写进公共台账。需要保留输入/输出时，使用有权限控制的独立存储，台账仅引用。

首次盘点 Git 基点为 `e0058a6d5c2a42fd7b9f8a481e49c09f048f9452`，工作区有未提交修改。快照盘点 673 个相关文件，其中代码资产选择 626 个文件、工程评测资产选择 39 个文件，其他为治理与证据文件，集合存在交叉。**这不是完整仓库冻结，也没有保存未提交文件的内容副本，不能仅凭该 commit 或哈希恢复当前系统。** 原始字节摘要可识别变化，不能代替备份、签名或运行验证。

首次盘点未运行构建、安装依赖、Baseline、业务库 SQL、真实模型调用或人工 UAT；该句只描述 `SNAP-20260831-001` 的历史边界。AI-02～AI-05 增量已完成工程代码验证和 Mock/定向回归，结果见 [AI 优化验证报告](verification/2026-08-31-ai-optimization.md) 与 [AI-05 批次验证报告](verification/2026-08-31-ai-batch.md)，仍未进行真实模型调用、业务数据库写入或人工 UAT。责任角色为建议分工，具体维护人、审核人未指定；空值不得解释为默认获批。

## 2. 六类资产统一索引

以下 `v0.1.0` 内容是首次盘点的静态观察，不是持续更新的运行现状。AI-02～AI-04 的新内容已登记为 `v0.2.0`，并绑定 `SNAP-20260831-002`；旧版本仍保留用于追溯，不覆盖首次摘要。

除待登记项外，以下 `v0.1.0` 均为本台账首次赋予的盘点版本，不是应用版本、Git Tag 或供应商模型版本。完整文件列表和摘要见 JSON。标为 `SOURCE_SNAPSHOT` 只表示读取了源码/文档。

| 类别 | 资产/记录 ID | 登记版本及状态 | 已登记内容 | 关键边界 |
|---|---|---|---|---|
| 数据/知识 | KNOW-RULES | v0.1.0 / SOURCE_SNAPSHOT | PROJECT_CONTEXT、DECISIONS、BUSINESS_RULES、AI 优化计划 | 项目治理依据，不是医学知识库或实际外发输入 |
| 数据/知识 | DATA-TRAINING | 未定版 / UNREGISTERED | 经授权培训内容包的待补项 | 原始资料目录存在，不等于材料已授权外发、脱敏或审核 |
| Prompt | PROMPT-ANSWER | v0.1.0 / SOURCE_SNAPSHOT | `AiQuService.generateAnswer` | 输入题型/题干，返回答案文本；当前约束为200字以内 |
| Prompt | PROMPT-SINGLE | v0.2.0 / IMPLEMENTED_NOT_REAL_MODEL_VERIFIED | `AiQuService.generateQu` | 主题/题型/难度 → 单题 JSON；解析后执行题型结构、选项去重和答案数量硬校验 |
| Prompt | PROMPT-BATCH | v0.2.0 / IMPLEMENTED_NOT_REAL_MODEL_VERIFIED | `AiQuService.generateOne`，批量入口内调用 | `trainingId` 绑定服务端 PPT/PPTX 提取文本；按题型数量/分值严格校验总分100，成功项仍为草稿 |
| Prompt | PROMPT-GROUNDED | v0.1.0 / PROPOSED | AI 优化计划第5节的依据材料 Prompt v2 | 方案版本，不登记为生效模板 |
| 模型 | MODEL-CHAT | v0.1.0 / SOURCE_SNAPSHOT | 后端配置代码、前端供应商模板、调用参数 | 实际运行配置与供应商固定模型快照未核实 |
| 代码 | CODE-APP | v0.2.0 / IMPLEMENTED_TESTED_DIRTY_WORKTREE | AI 课件出题优化涉及的应用源码、依赖、测试及前端入口 | 脏工作区；快照仅覆盖本次选定文件，不是完整可部署候选 |
| 评测集 | EVAL-ENGINEERING | v0.2.0 / RUN_VERIFIED_MOCK_AND_ENGINEERING | 当前工程回归与 AI 材料/计划/非法输出定向测试 | 24 个 Java 测试类/94 个测试、前端11个测试；不代表 AI 内容医学质量 |
| 评测集 | EVAL-UAT | v0.1.0 / SOURCE_SNAPSHOT | 人工 UAT 手册与现有 Bug 台账 | 有规程不等于已执行或通过 |
| 评测集 | EVAL-AI | 未定版 / UNREGISTERED | 固定 AI 质量样本、划分和评分口径待登记 | 未伪造样本、医学正确率或评测结果 |
| 发布记录 | REL-20260831-001 | NOT_READY | 首次发布就绪性盘点，目标为 V1.0 Portfolio Release | 不是实际发布事件；发布时间/签收人/回退目标均为空 |

`record_id` 用于精确引用。例如 `PROMPT-ANSWER@v0.1.0`。待登记资产使用 `DATA-TRAINING@PENDING-20260831`、`EVAL-AI@PENDING-20260831`，保留明确缺口，不使用虚构的正式版本。

三个现有 Prompt 共用 `AiQuService.java` 文件摘要，并通过方法名区分。该摘要是**整个源文件**的 SHA-256，不是抽取后模板或某次填入变量后的 Prompt 摘要。实际评测时还要记录渲染后 Prompt 的受控引用及摘要。

模型条目仅记录源码默认值：provider=`deepseek`、model=`deepseek-flash`（DeepSeek V4.1 Flash 的 API 模型 ID）、endpoint=`https://api.deepseek.com/chat/completions`；出题调用 `temperature=0.7`，答案补全 `max_tokens=500`、两类出题 `max_tokens=900`，超时60秒。`top_p`、`seed` 未发送。旧数据库配置中的 `deepseek-chat` / `deepseek-v4-flash` 仅在已知 DeepSeek endpoint 上归一化为当前 API 模型 ID；自定义 endpoint 不自动改写。**未核实运行时是否使用这些值，也没有验证供应商当前型号、价格或服务能力。** 配置页连通性测试有不同参数，不能替代出题评测。

## 3. 如何把版本连起来

固定引用方向：**发布记录 → 版本组合（bundle）→ 五类资产的精确 record_id**；评测运行也指向同一个组合，并补充实际输入、Prompt 和模型配置。资产自身可通过 `depends_on_record_ids` 指向明确的代码/规则版本。

本次组合为 `BUNDLE-20260831-001`，状态 `INVENTORY_ONLY_NOT_FROZEN`；以下描述首次盘点观察：

| 组合字段 | 当前绑定 | 含义 |
|---|---|---|
| data_knowledge_record_ids | KNOW-RULES@v0.1.0、DATA-TRAINING@PENDING-20260831 | 治理依据及待选内容包；不声称已接入模型 |
| prompt_record_ids | PROMPT-ANSWER/SINGLE/BATCH 各 @v0.1.0 | 当前源码中观察到的三个入口 |
| planned_prompt_record_ids | PROMPT-GROUNDED@v0.1.0 | 单列未来方案，不混入运行模板 |
| model_record_ids | MODEL-CHAT@v0.1.0 | 静态配置代码；运行配置仍需核实 |
| code_record_id | CODE-APP@v0.1.0 | 本次选定文件盘点 |
| evaluation_record_ids | EVAL-ENGINEERING@v0.1.0、EVAL-UAT@v0.1.0、EVAL-AI@PENDING-20260831 | 工程/人工/AI 质量分别管理 |

此表中的斜杠缩写仅用于阅读，JSON 始终保存完整 ID。盘点组合允许缺口；用于正式评测或发布的冻结组合不允许关键版本为空、用 `latest` 或依赖未批准的方案。若某项不适用，必须记录原因、范围决定及批准人，不能静默省略。

AI-02～AI-04 当前实施核验组合为 `BUNDLE-20260831-002`，状态 `IMPLEMENTATION_VERIFIED_NOT_RELEASED`。它绑定 `KNOW-RULES@v0.2.0`、`PROMPT-SINGLE@v0.2.0`、`PROMPT-BATCH@v0.2.0`、`MODEL-CHAT@v0.1.0`、`CODE-APP@v0.2.0` 和 `EVAL-ENGINEERING@v0.2.0`；`DATA-TRAINING`、真实模型、AI 内容质量评测、人工审核和 UAT 仍保持未完成状态。对应运行记录为 `RUN-AI-20260831-001`，模式 `MOCK`，不是生产发布或真实模型通过结论。

AI-05 当前实施核验组合为 `BUNDLE-20260831-003`，状态 `IMPLEMENTATION_VERIFIED_NOT_RELEASED`。它绑定 `KNOW-RULES@v0.3.0`、`PROMPT-SINGLE@v0.2.0`、`PROMPT-BATCH@v0.2.0`、`MODEL-CHAT@v0.1.0`、`CODE-APP@v0.3.0` 和 `EVAL-ENGINEERING@v0.3.0`；新增批次/条目持久化、requestId 幂等、UNKNOWN 恢复、前端进度恢复及隔离 V8 迁移。对应运行记录为 `RUN-AI-20260831-002`，模式 `MOCK`；真实模型、exam_keryo、完整服务重启 HTTP、AI 内容质量评测和人工 UAT 仍未完成。

## 4. 字段字典

### 每个资产版本通用字段

| 字段 | 填写规则 |
|---|---|
| asset_id / record_id / category / name | 稳定资产编号、唯一版本记录编号、六类中的类别和名称；资产编号不随小版本变化 |
| version / predecessor_record_id | 当前版本及上一版本记录 ID；首次可无上一版本；新增版本不覆盖旧内容 |
| status / registered_at | 状态与带时区登记时间；区分方案、草稿、快照、已冻结、已废弃 |
| owner_name / reviewer_name / approved_at | 真实责任人、审核人、批准时间；建议责任角色不能代替人员签收 |
| change_summary | 变更原因、范围及关联需求/问题；更正误记也要留变更记录 |
| snapshot_id / source_paths / file_set_sha256 | 不可变快照标识、源位置、文件集合摘要；公开台账只存安全元数据 |
| depends_on_record_ids | 依赖的精确版本；区分治理依据和真实运行依赖，不猜测关联 |

### 各类补充字段

| 类别 | 必须补充的信息 |
|---|---|
| 数据/知识 | 原始来源及日期、文档版本/内容摘要、页/节位置、适用对象、授权与外发范围、脱敏记录、专业审核人；提取文本单独登记提取器版本和摘要，不能只记文件名 |
| Prompt | 入口/方法、系统与用户模板位置、变量定义、输出结构、模板摘要、变更理由；逐次运行另记填参后的 Prompt 摘要 |
| 模型 | 实际供应商、endpoint、model ID/固定快照或权重摘要、有效参数、超时、调用协议及脱敏配置引用；动态模型别名需标记不可完全固定，记录调用日期和响应模型标识 |
| 代码 | 仓库/完整commit、dirty状态、选定文件清单、依赖锁文件、构建工具/命令、产物摘要、配置引用；发布必须具备可恢复的完整交付资产 |
| 评测集 | 固定case_id、任务入口、输入版本及出处、预期结果/参考答案依据、评分规程版本、开发/验证集划分、维护与审核人；调整样本或评分规则都要升版 |
| 发布记录 | release_id、完整bundle、变更摘要、环境、精确评测run_id、门禁结论、签收人和时间、产物摘要、实际部署证据、前一发布及回退目标 |

### 评测运行不等于评测集

`runs` 记录一次实际执行，`EVAL-*` 记录用例/规程版本。重复运行同一评测集要新建 run_id，不能改写上次结果。`mode` 区分 `MOCK`、`REAL_MODEL`、`ENGINEERING`、`MANUAL_UAT`；历史摘要另标 `HISTORICAL_REPORT_REFERENCE`。

每次执行登记：组合与样本 ID、输入/渲染 Prompt 摘要、实际模型配置、时间、执行/评分人、结果、失败类别、人工修改记录、报告位置/摘要和关联 Bug ID。AI 运行同时保留独立样本数、尝试次数、已评分/通过/跳过数、延迟、token用量及实际费用和币种。未知值填 `null`，不能填0。通过率使用明确评分分母，分母为0时不计算；不能将多次生成次数当独立样本数，或把 mock 成功当真实模型正确率。

阈值与专业评分口径先由负责人/内容审核人确定，不能事后为了通过修改。验证子集不用于调 Prompt；发现泄漏或样本变动需升版并解释。缺陷继续进入 [UAT_BUG_LEDGER.md](UAT_BUG_LEDGER.md)，台账只引用 Bug ID，不另建缺陷管理系统。

## 5. 历史证据与当前差异

| 记录 | 已核实的事实 | 不可推导的结论 |
|---|---|---|
| RUN-HISTORY-ROLELOG-20260831 | [ROLELOG 验证摘要](verification/2026-08-30-rolelog.md)记载：Java20类/85测试、前端11测试、Python13测试、两次完整Baseline通过 | 本次没有重跑；摘要与当前文件快照的精确对应关系未建立 |
| 历史原始报告 | 摘要引用 `baseline-20260830-235321.md` 与 `baseline-20260830-235538.md`；本仓库的 `baseline-report/` 下未找到这两份文件 | 不代表它们在其他工作树也不存在；不补造或冒用其他报告 |
| lockfile | `yf-bev2-vue/package-lock.json` 已纳入 Git；2026-09-11 clean `npm ci` 通过 | 首次实际安装因宿主 npm cache 权限失败，改用隔离 cache 后通过；详见 `docs/verification/2026-09-11-clean-npm-install.md` |
| 模型配置 | 当前 `AiConfigServiceImpl` 已有按最新配置判断停用的代码 | 旧计划关于该行为的风险描述不能直接当作当前运行复现结果；本次不做运行验证 |
| 新增代码/测试文件 | 当前源码与历史测试报告可能不同，按本次文件清单记录 | 不把新增文件自动记为已完成、已运行或已通过 |
| AI-02～AI-04 定向验证 | [AI 优化验证报告](verification/2026-08-31-ai-optimization.md)记录：后端打包通过，实际 Java 运行器 24 类/94 测试通过；前端类型检查、11 个测试和生产构建通过；Mock 请求验证了服务端材料和100分计划 | 不代表真实模型质量、医学正确性、内容人工审核、UAT 或正式发布 |

历史运行记录的 `evaluated_bundle_id` 保持空值；不能将本次盘点组合补写为当时受测版本。摘要文件的哈希证明“看的是哪份摘要”，不证明摘要所有内容已经独立复现。

首次（12:32）核验结果为 `INTEGRITY_PASS_WITH_SOURCE_DRIFT`：台账结构、内部摘要及版本引用通过，但整理期间部分源文件发生了并行修改。核验报告保留原快照摘要和后续观察摘要；旧快照未覆盖，新源码未冒用旧版本的运行结论。首次静态描述是在快照前后人工读取，不是原子采集，尤其变化文件的语义应重新核对。后续发布必须重新盘点变更、保存完整候选并验证，不能把这次核验当作版本冻结。

## 6. 发布与回退登记

`REL-20260831-001` 是 **READINESS_ASSESSMENT_NOT_DEPLOYMENT**，状态 **NOT_READY**，没有发布时间、发布人、签收人、制品摘要或回退目标。禁止用本台账已建立来宣告 V1.0 已发布。

后续实际发布前逐项补证据：

1. 确定展示范围，冻结完整候选、依赖及配置；保存可恢复的代码、材料和构建产物，不只有 commit/哈希。
2. 使用同一组合版本执行干净安装、Baseline及活跃回归，保存本候选报告；明确环境和跳过项。
3. 由负责人完成真实 UAT、业务回验及问题 RETEST；保留 Demo 和签收证据，补齐既定稳定性观察。
4. AI 能力单列：材料授权、实际模型配置、固定质量评测及人工内容审核。若经批准停用 AI 演示，记录范围决定与停用验证，不伪造 AI 验收通过。
5. 新建真实发布记录，绑定准确 bundle/run_id、发布环境、产物摘要、批准人、部署时间与结果。状态可依次为 DRAFT → CANDIDATE → APPROVED → RELEASED；执行失败记 FAILED，撤回记 ROLLED_BACK。
6. 回退绑定上一份已验证发布的完整组合，记录触发条件、操作者、结果和复核证据。没有上一份可用版本时明确“无可验证回退目标”。代码/Prompt回退不等于数据库回退，数据迁移必须单独核验和授权。

发布记录不删除失败历史，也不倒填批准或上线时间；更正/撤回追加事件和理由。正式生产、商业化设施仍属当前范围之外，不因本台账扩大为生产交付。

## 7. 日常维护步骤与升版规则

1. **变更前**查 asset_id 与当前 record_id；确定责任人、影响入口与是否涉及资料外发。
2. **变更后**从模板复制新资产版本，填上一版本 ID、变更摘要、精确位置与哈希；涉及数据/Prompt/模型参数/代码/评测样本变化均登记。模板所在源文件发生变化时，保守新建文件快照版本，并说明是否仅为非模板代码变化。
3. **组合更新**新建 bundle_id，明确运行资产和方案资产；不要改旧组合所指向的版本。只更新模型或 Prompt 也需新组合及相应回归。
4. **评测后**新建 run_id，固定版本与报告证据；失效的历史结果留存，不移接到新组合。授权、费用与专业评分未确认时，先登记缺口，不能启动真实敏感材料外发或付费评测。
5. **发布后**登记实际环境/时间/审批/回退引用；没有执行就保持空值和未发布状态。
6. **保存校验**确认 ID 唯一、所有引用存在且类别正确、来源可读取、哈希对应当前选定快照；同步本文摘要与 `change_log`。提交时只纳入本次治理文件，不能顺带提交其他工作区修改或敏感原文。

推荐版本格式 `v主版本.次版本.修订号`：任务定义/接口/评分口径不兼容变更升主版本；新增内容或兼容能力升次版本；小幅纠错升修订号。任何影响输出、授权、数据来源、模型参数或评分的变更都不能无版本覆盖。资产名称和逻辑 ID 保持稳定，内容版本与记录 ID 变化；发布使用单独 release_id，时间统一保留时区。

摘要算法：逐文件 `SHA-256(原始字节)`；文件集合按路径排序，将每个 `path + TAB + lowercase_sha256 + LF` 连接后以 UTF-8 计算 SHA-256。路径改名或换行改变会改变摘要，均需说明。快照文件本身的摘要另存于 `ledger.json.snapshots`，避免自引用。

本次尚待补齐的责任项已进入 JSON `open_items`：授权内容包、有效模型快照、固定AI评测集、历史原始报告、完整可恢复候选、人工验收与签收、历史文档差异，以及 AI 题目证据字段、跨批次去重、完整服务重启 HTTP 验收和真实模型验证。**台账结构已经建立；这些资产和验收缺口仍然存在。**

## 8. 自动维护授权与执行方式

2026-08-31 用户明确要求后续自动维护本台账。本节为台账规则增量；初次快照不变，AI-02～AI-05 另行追加 `SNAP-20260831-002`、`SNAP-20260831-003`、组合和运行记录。

| 项目 | 配置 |
|---|---|
| 定期检查 | 每日18:00，Asia/Shanghai（北京时间） |
| 自动任务 | 本平台统一版本台账自动维护；标识 `automation-3`；创建时状态 ACTIVE |
| 任务归属 | 附着当前 Codex 任务，直接维护本地实际代码仓库；不操作同名其他项目台账 |
| 随任务更新 | 已写入仓库 AGENTS.md；后续相关资产变更或形成证据后，在任务交付前同步维护 |
| 唯一事实源 | `docs/version-ledger/ledger.json`，本文摘要同步；无变化不创建版本或重复快照 |
| 长期授权范围 | 检查安全本地元数据、追加资产版本/组合/证据与快照、修订台账摘要、核验并记录缺口 |

定期任务先读取最新项目规则和台账，再发现受控范围内新增、删除及内容变化。每次新建不可覆盖的快照与核验报告，核对 ID、精确版本引用、类别、哈希和链接。旧版本/发布事实不得覆盖；更正或撤回追加原因。初次构建目录中的生成器、finalize 脚本和固定名称核验脚本只作历史参考，不能重跑覆盖已有记录。

运行前后核对源文件和台账指纹；发现同时编辑或持续变化时不抢写、不倒填冻结结果，记录待更新项。实际输入与模型配置未知就保持未知；方案不标生效、mock不当真实模型评测、历史PASS不套新代码、人工UAT和签收不代填。材料授权、付费、隐私、数据库、开发及发布边界均保持原样。

每日检查是补漏机制，不是后台文件监听或 Git 钩子；随任务更新依赖后续执行者遵守 AGENTS.md。定期执行依赖本机执行环境与项目目录可访问，本次配置不承诺设备不可用时实时更新。用户可随后要求改频率、暂停或停止。

## 9. 2026-08-31 18:00 自动维护增量

执行时间：2026-08-31T18:09:00+08:00。最新台账版本 v0.3.1；`SNAP-20260831-004` 与 `BUNDLE-20260831-004` 为新增静态盘点，状态 `INVENTORY_ONLY_NOT_FROZEN`。

本轮将历次局部快照按路径合并后核对：已覆盖文件内容无进一步变化、未发现删除；另有2个既有文件首次纳入覆盖，并非本轮新增开发：`scripts/baseline/smoke.js`（Node备用入口）和 `docs/verification/2026-08-30-opt01-opt02.md`（既有报告）。Git基点已为 `c76f1b383b749dce89f695127fb096f918b28f1e`；本轮未提交或推送。当前工作区脏状态按快照记录为 `true`，即使干净也不等于通过运行验收。

为补全累计资产范围、登记此前未升版的整文件指纹并统一集合摘要口径，新增以下版本；未变化的 DATA-TRAINING、EVAL-UAT、EVAL-AI 沿用原ID。文件数量是源文件数量，不能当作测试用例数：

| 资产 | 新版本 | 状态 | 源文件数 |
|---|---|---|---|
| KNOW-RULES | v0.3.1 | SOURCE_SNAPSHOT | 10 |
| PROMPT-ANSWER | v0.1.1 | SOURCE_SNAPSHOT | 1 |
| PROMPT-SINGLE | v0.2.1 | SOURCE_SNAPSHOT | 1 |
| PROMPT-BATCH | v0.2.1 | SOURCE_SNAPSHOT | 2 |
| PROMPT-GROUNDED | v0.1.1 | PROPOSED | 1 |
| MODEL-CHAT | v0.1.1 | SOURCE_SNAPSHOT | 3 |
| CODE-APP | v0.3.1 | SOURCE_SNAPSHOT | 635 |
| EVAL-ENGINEERING | v0.3.1 | SOURCE_SNAPSHOT | 44 |

核验发现历史记录有11项告警：SNAP-002集合摘要不符、1项资产引用未包含于对应快照、7项资产集合摘要口径不符，以及同一组循环关联涉及的2条记录。旧快照与版本原文保持不变，详情和可计算的正确摘要追加到 `integrity_findings`；缺失历史来源不使用当前文件倒填。当前版本均按各自 `source_paths` 独立计算集合摘要，并将关联放入组合以避免代码/规则互相依赖。

`runs` 中既有94/98等测试计数和 PASS 仍属于原实施任务，本轮只核对报告摘要，不重跑、不移接到新组合。`verified_by_this_task` 的历史含义已补注为原执行任务，而非此次自动维护。三份既有运行记录与未发布记录均未改写；新组合没有评测通过或签收记录。

待补项延续材料授权、有效运行模型、固定AI质量评测、人工UAT和可恢复发布资产；另追加历史摘要/关联问题（GAP-09）与Python唯一入口表述和Node备用代码不一致（GAP-10）。本轮只记录该差异，不修改测试入口或治理政策，不执行备用脚本。发布状态仍为 NOT_READY。

后续自动维护应优先读取 `latest_snapshot_id`、`latest_bundle_id` 和 `latest_asset_record_ids`，按累计受控范围比较；局部快照未列出的文件不能当作已删除。同一历史告警或同一内容不可重复创建版本。

## 10. 2026-09-01 18:46 自动维护增量

执行时间：2026-09-01T18:48:54+08:00。台账版本 v0.3.2；新增 `SNAP-20260901-001`、`BUNDLE-20260901-001` 和 `CODE-APP@v0.3.2`。

本轮对最新累计范围逐项复核，前次已覆盖的690个文件没有内容变化或删除。发现受Git跟踪的根种子文件 `yf_boot_exam.sql` 此前未纳入累计资产；当前相对Git基点 `c76f1b383b749dce89f695127fb096f918b28f1e` 显示1行增加、1行删除。本轮仅计算并登记原始字节SHA-256与长度，没有把SQL内容复制到台账、没有判断该行涉及Schema还是种子数据，也没有执行数据库、Baseline、构建或应用测试。

由于该文件是初始化高风险资产，当前代码版本状态记为 `SOURCE_SNAPSHOT_UNREVIEWED_SEED_CHANGE`，组合状态记为 `INVENTORY_ONLY_UNREVIEWED_SEED_CHANGE`。新增 GAP-11，要求在用于环境初始化、候选冻结或发布前由研发/数据库维护者评审变更语义并在隔离环境验证。历史资产、快照、3条运行记录和发布记录均未改写或移接；发布状态仍为 NOT_READY。

## 11. 2026-09-08 ROLELOG 正式业务库迁移证据

负责人确认并完成 `exam_keryo` 的 V7 三角色迁移。迁移前发现的 1 条无效孤儿角色关系已按确认精确清理；密码历史记录保留。随后对正式库执行只读 post-image 核验，结果见 [正式库核验报告](verification/2026-09-08-rolelog-business-db.md)。

本次追加运行记录 `RUN-ROLELOG-20260908-001`，结果仅覆盖授权迁移和数据库 post-image，不更新人工 UAT、业务需求回验、MySQL 七天稳定性或 Portfolio Release 状态。没有新增代码资产、快照或发布记录，也没有将该结果套用到旧的 Baseline 运行记录。

## 12. 2026-09-08 版本台账增量维护

执行时间：2026-09-08T18:10:19+08:00；台账版本 `v0.3.4`；新增 `SNAP-20260908-001`、`BUNDLE-20260908-001`。

本次把前一快照的 691 个受控文件重新计算指纹，并登记 8 个已变化文件和 5 个新增文件。新增范围包括治理文档、启动器/前端配置、V7 业务迁移包装脚本、Java 发现测试和 ROLELOG post-image 报告；`yf_boot_exam.sql` 仍只保存 SHA-256 与字节数，不读取或解释 SQL 内容。

既有 `RUN-ROLELOG-20260908-001` 及其 `docs/verification/2026-09-08-rolelog-business-db.md` 已作为证据关联到 `BUNDLE-20260908-001`；该证据只说明 V7 迁移 post-image 检查范围，不能替代 Manual UAT、Business Requirement Validation、MySQL 七日稳定性观察或 Portfolio Release。本次维护未执行应用测试、数据库、模型调用、部署或发布操作。

本轮新增资产版本：`KNOW-RULES@v0.3.2`、`CODE-APP@v0.3.3`、`EVAL-ENGINEERING@v0.3.2`。Prompt、模型、训练数据、UAT 和 AI 评测集没有新的内容版本；既有发布记录仍为 `NOT_READY`。

## 13. 2026-09-11～09-12 配置与工程证据增量维护

执行时间：2026-09-12T18:09:24+08:00；台账版本 `v0.3.7`；新增 `SNAP-20260911-001`、`BUNDLE-20260911-001`。

本次将前一快照的 696 个受控文件重新计算指纹，登记 13 个已变化文件和 5 个新增文件。新增范围包括 `docs/ARCHITECTURE.md`、`docs/PORTFOLIO_RELEASE.md`、`yf-bev2-vue/.gitignore` 以及两份 2026-09-11 核验报告。

新增资产版本为 `KNOW-RULES@v0.3.3`、`MODEL-CHAT@v0.2.0`、`CODE-APP@v0.3.4`、`EVAL-ENGINEERING@v0.3.3` 和 `EVAL-UAT@v0.1.1`；Prompt、训练数据和 AI 质量评测集没有新的内容版本。`RUN-MODEL-20260911-001` 记录模型配置、工程测试和数据库配置行同步报告，`RUN-NPM-20260911-001` 记录隔离 npm cache 下安装 1158 个包的报告；本次台账维护未重跑这些操作。

DeepSeek 配置证据不代表真实模型调用成功、医学内容正确或供应商能力已固定；Manual UAT、Business Requirement Validation、MySQL 七日稳定性观察和 Portfolio Release 仍未完成，发布状态保持 `NOT_READY`。

## 14. 2026-09-20 WorkBuddy 交接治理文档增量维护

执行时间：2026-09-20T18:02:02+08:00；台账版本 `v0.3.8`；新增 [SNAP-20260920-001](version-ledger/SNAP-20260920-001.files.json)、`BUNDLE-20260920-001` 和 [核验报告](version-ledger/VERIFICATION-20260920-180200.json)。

本轮发现并登记新增文件 `docs/HANDOVER_TO_WORKBUDDY.md`。前一快照的701个受控文件逐项复核无变化，新快照累计702个文件；新增文件仅保存字节摘要，不把交接文档当作应用代码、模型调用、评测结果或发布批准。

新增资产版本为 `KNOW-RULES@v0.3.4`，状态 `SOURCE_SNAPSHOT_NOT_BUSINESS_APPROVAL`，前序为 `KNOW-RULES@v0.3.3`。本轮没有新增 Prompt、模型、代码、评测集或发布记录，也没有新增运行记录；既有运行记录继续绑定原组合，不移接到新组合。

交接文档保留人工 UAT、业务需求回验、MySQL 七日稳定性和 Portfolio Release 的未完成边界。未运行应用、数据库、真实模型、测试服务或部署；发布状态保持 `NOT_READY`。


## 15. 2026-09-21 UAT 前代码修复增量维护

执行时间：2026-09-21T18:00:32+08:00；台账版本 `v0.3.9`；新增 [SNAP-20260921-001](version-ledger/SNAP-20260921-001.files.json)、`BUNDLE-20260921-001` 和 [核验报告](version-ledger/VERIFICATION-20260921-180032.json)。

本轮发现当前 HEAD `65ea046a88a1922633102cc831d95b38fd8b2905` 相对 `SNAP-20260920-001` 的 16 个已登记源码文件变化，并新增 `TrainingFileAccessGuard.java`。变化来自提交 `2f510c3`，新快照累计703个受控文件；工作区当前干净。

新增资产版本为 `CODE-APP@v0.3.5`，状态 `IMPLEMENTATION_REGRESSION_REPORTED_NOT_UAT_VERIFIED`，前序为 `CODE-APP@v0.3.4`。提交说明报告 13 条接口级回归检查，但本轮只核对提交与源码指纹，未重跑测试、未读取外部 UAT 证据，也未关闭任何人工 UAT 问题。

本轮没有新增 Prompt、模型、数据/知识、评测集或发布记录；既有运行记录继续绑定原组合，不移接到新组合。Manual UAT、Business Requirement Validation、MySQL 七日稳定性观察和 Portfolio Release 仍未完成，发布状态保持 `NOT_READY`。


## 16. 2026-09-26 P3-02/P3-03 源码增量维护

执行时间：2026-09-26T18:00:55+08:00；台账版本 `v0.4.0`；新增 [SNAP-20260926-001](version-ledger/SNAP-20260926-001.files.json)、`BUNDLE-20260926-001` 和 [核验报告](version-ledger/VERIFICATION-20260926-180055.json)。

本轮在干净提交 `9ad3e6c` 上盘点 P3-01/P3-02/P3-03 的安全源码、迁移、测试、前端和交接文档增量。快照累计 830 个受控文件，新增 128 个受控路径；构建产物、环境文件、原始材料和台账自身排除。变更的 `yf_boot_exam.sql` 未读取，保留为待补项。

新增资产版本为 `KNOW-RULES@v0.3.5`、`PROMPT-ANSWER@v0.1.2`、`PROMPT-SINGLE@v0.2.2`、`PROMPT-BATCH@v0.2.2`、`MODEL-CHAT@v0.2.1`、`CODE-APP@v0.4.0`、`EVAL-ENGINEERING@v0.4.0` 和 `EVAL-UAT@v0.1.2`。这些都是安全元数据/源码快照；提交说明中的测试/UAT数字未由本轮重跑，不作为独立 PASS，也未创建新的运行记录。

本轮未运行应用、数据库、迁移、服务、真实模型或人工 UAT。Manual UAT、Business Requirement Validation、MySQL 七日稳定性和 Portfolio Release 仍未完成，发布状态保持 `NOT_READY`。


## 17. 2026-09-27 并发源码变更延期

执行时间：2026-09-27T18:02:47+08:00；检测到 SNAP-20260926-001 之后的 P3-10/P3-11/P3-13 提交，同时工作区存在 P3-04 学习建议相关未提交或新增路径：`yf-bev2-api/src/main/java/com/keryo/training/common/security/PortfolioRoles.java`、`yf-bev2-api/src/main/java/com/keryo/training/modules/advice/`、`yf-bev2-api/src/main/java/com/keryo/training/modules/compliance/entity/AiTrace.java`、`yf-bev2-api/src/main/resources/db/migration/V20__study_advice.sql`。

按并发编辑政策，本轮未读取或修改这些源文件，也未冻结不一致组合；仅追加 `GAP-14` 待补项。工作区稳定后再重新盘点，历史快照和发布边界保持不变。


## 19. 2026-09-28 P3-14 并发源码变更延期

执行时间：2026-09-28T18:06:24+08:00；检测到未提交或新增的 P3-14 报告模块路径：`yf-bev2-api/src/main/java/com/keryo/training/modules/compliance/entity/AiTrace.java`、`yf-bev2-api/src/main/java/com/keryo/training/modules/report/`、`yf-bev2-api/src/main/resources/db/migration/V25__report.sql`。

按并发编辑政策，本轮未读取、未修改、未冻结这些文件，也未创建新的资产版本、快照、组合或评测记录；仅追加 `GAP-14` 待补记录，待工作区稳定后重新盘点。


## 20. 2026-09-29 并发源码与未跟踪日志变更延期

执行时间：2026-09-29T18:03:49+08:00；检测到 SNAP-20260926-001 之后的 P3-04～P3-08/P3-14 提交，同时工作区存在未跟踪文件 `yf-bev2-api/logs-p308.txt`。

按并发编辑政策，本轮未读取或修改该日志，也未冻结当前不一致组合；仅追加 `GAP-14` 待补记录，未创建新的资产版本、快照、组合、评测运行或发布事实。工作区稳定后再重新盘点，最新登记组合仍保持 `BUNDLE-20260926-001` / `NOT_READY`。

## 21. 2026-09-30 并发源码与 UAT 材料变更延期

执行时间：2026-09-30T22:04:27+08:00；相对 SNAP-20260926-001 检测到 177 个已提交路径变化，其中 10 个命中既有快照路径、167 个未出现在该清单，另有 1 个删除；工作区还存在未跟踪文件 docs/UAT人工走查清单-三期全量.docx 与 yf-bev2-api/logs-p308.txt。

按并发编辑政策，本轮没有冻结当前组合，也没有读取这两个未跟踪文件；仅追加 GAP-14 与[独立核验报告](version-ledger/VERIFICATION-20260930-220427.json)，未创建新资产版本、快照、组合、评测运行或发布事实。最新登记组合保持 BUNDLE-20260926-001，Manual UAT 仍为 NOT_STARTED，Portfolio Release 仍为 NOT_READY。


## 22. 2026-10-08 新增启动脚本待盘点与路径计数更正

执行时间：2026-10-08T18:02:11+08:00；新增未跟踪路径 scripts/keryo-start.ps1 与 scripts/启动测试环境.bat。既有人工 UAT 清单和日志继续待补。本轮使用未转义 Git 路径比较，177 个提交变更中 11 个命中既有快照、166 个不在清单、1 个删除；此前 10/167 计数保留为历史，本节追加更正。

只追加 GAP-14 四项待补路径及[独立核验报告](version-ledger/VERIFICATION-20261008-180211.json)。未读取、哈希或运行未跟踪文件，未创建资产版本、快照、组合、评测运行或发布事实。最新登记仍为 SNAP-20260926-001 / BUNDLE-20260926-001；Manual UAT 为 NOT_STARTED，Business Requirement Validation 为 NOT_COMPLETED，Portfolio Release 为 NOT_READY。
本轮另发现六个历史快照文件的现有字节摘要与台账登记值不符，已追加 INT-20261008-001，原因和发生时间未知，保留原文件及原登记值待补。最新 SNAP-20260926-001 文件摘要匹配；历史核验不宣称全部通过。


## 23. 2026-10-13 Agent 与站内信并发改动延期

执行时间：2026-10-13T18:50:26+08:00；HEAD 为 08ea32beea514af191e694be6de272fc643e755d。排除台账维护路径后，相对最新快照有 181 个已提交路径变化，其中 12 个命中快照、169 个不在清单，另有 1 个删除。以下路径待盘点：

- yf-bev2-api/logs-p308.txt
- yf-bev2-api/src/main/java/com/keryo/training/modules/agent/service/impl/AgentServiceImpl.java
- yf-bev2-api/src/main/java/com/keryo/training/modules/inbox/entity/InboxMessage.java
- yf-bev2-api/src/main/java/com/keryo/training/modules/inbox/service/impl/InboxServiceImpl.java
- yf-bev2-api/src/main/java/com/keryo/training/modules/inbox/service/InboxService.java
- yf-bev2-api/src/main/resources/db/migration/V27__agent_system_channel.sql
- yf-bev2-api/src/test/java/com/keryo/training/modules/agent/AgentServiceImplTest.java
- yf-bev2-api/src/test/java/com/keryo/training/modules/inbox/InboxServiceImplTest.java

本轮仅追加 GAP-14 和[独立核验报告](version-ledger/VERIFICATION-20261013-185026.json)，未读取未提交文件内容、未运行应用或迁移、未冻结组合，未创建资产版本、快照、组合、评测运行或发布事实。最新登记仍为 SNAP-20260926-001 / BUNDLE-20260926-001；六项历史文件摘要冲突保留待补。Manual UAT 为 NOT_STARTED，Business Requirement Validation 为 NOT_COMPLETED，Portfolio Release 为 NOT_READY。
## 24. 2026-10-14 工作区迁移轮次盘点（补第 22/23 节延期）

执行时间：2026-10-14T01:30:00+08:00；工作区由旧路径（vibe coding/医疗材料树）整体迁移至新项目根，旧目录原样保留。业务负责人确认三项决策：历史包袱两步走、完整验证、旧目录保留。

新增 `SNAP-20261014-001`（1113 文件；相对上一快照 added 284 / modified 198 / deleted 1）、`CODE-APP@v0.4.1`（source_paths 753→1072，纳入第 22/23 节延期的 V27-V29 新增源码与本轮路径修复）、`BUNDLE-20261014-001`、`RUN-RELOC-20261014-001`。验证证据见 [迁移验证报告](verification/2026-10-14-relocation.md)：git fsck/HEAD/17 分支与迁移前基线一致；JDK17 mvn -o package OK；单测 48 类 439 项全过；后端 9091 HTTP 200、前端 5174 监听、生产构建 dist-pro OK。`repository_path_rule` 措辞随目录结构调整同步修订（仅相对仓库根，历史相对路径记录不变，原因记录于 change_log v0.5.6）。

旧目录删除前须先处置两处仍在旧树的项目外资源：`00-原始资料\课件`（batch_upload_courseware.py 引用）与 `04-运营交付包`（NEXT-交接单-P3-08 引用）。本轮为工程迁移验证，不改变 Manual UAT NOT_STARTED、Business Requirement Validation NOT_COMPLETED、Portfolio Release NOT_READY。

## 25. 2026-10-14 瘦身轮次（补提前执行）

执行时间：2026-10-14T09:45:00+08:00；业务负责人确认提前执行原定于人工验收后的瘦身。commit `1f7d50e` 移除 65 个过时跟踪文件（legacy e2e 脚本 7、一次性诊断/补丁 3、build/ 过程产物 39、T 系列评审文档 8、交接单根目录副本 8），git 历史保留全部可找回；`.gitignore` 增 `.workbuddy/`；保留 M1_Acceptance.md、batch_upload_courseware.py 与 archive/ 归档区（业务负责人四选二确认保留）。build/ 未跟踪残留 72 个文件移至 `_migration-baseline/old-build-untracked`（未跟踪，不入清单）。

新增 `SNAP-20261014-002`（1104 文件，deleted 9、modified 0）、`CODE-APP@v0.4.2`（source_paths 1072→1066；yf 两端 src 无任何变更，迁移验证证据仍适用，本轮不重复运行）、`BUNDLE-20261014-002`。最新登记为 SNAP-20261014-002 / BUNDLE-20261014-002 / CODE-APP@v0.4.2；Manual UAT 仍 NOT_STARTED，Portfolio Release 仍 NOT_READY。
