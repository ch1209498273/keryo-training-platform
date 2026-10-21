# 系统架构与技术方案（ARCHITECTURE）

> 交接文档 · 基于实际代码整理 · 2026-08-14
> 本文描述**当前真实运行的架构**。历史文档（WBS V3.1、技术方案 V1.4）中的设计差异已在 CHANGELOG CR-01~CR-08 记录。

---

## 1. 系统概述

**血透中心医护培训平台**：面向血透中心医护人员的在线培训 + 考试平台。核心业务闭环：

```
登录 → 学（培训内容/培训计划）→ 考（在线考试）→ 出分（成绩公示/人工阅卷/补考）→ 统计（看板）
```

上游二开基底：**云帆在线考试系统 V2.0 开源版**（SpringBoot3+Vue3，原包名 `com.yf.*` 保留）。定制业务放在 `com.keryo.training.*` 扩展包，通过 `@ComponentScan("com.keryo.training")` 挂载（`com.yf.config.keryo.KeryoComponentConfig`）。

## 2. 技术栈

| 层 | 选型 | 版本 |
|---|---|---|
| 后端 | Java + Spring Boot | JDK 17（Spring Boot 3.2.1）|
| ORM | MyBatis-Plus | 3.5.11 |
| 安全 | Apache Shiro + JWT | Shiro 2.0.2（jakarta）/ shiro-redis 3.3.1 |
| 数据库 | MySQL | 8.4 LTS，库名 `exam_keryo` |
| 缓存 | Redis（dev 降级为内存实现 `MemoryRedisServiceImpl`）| — |
| 前端 | Vue 3 + TypeScript + Element Plus + Vite | Vite 4 / Element Plus |
| AI | DeepSeek API（OpenAI 兼容协议）| — |
| 文档预览 | Office Online Viewer（PPTX/DOCX）+ PDF.js（PDF）| — |
| 构建 | Maven（后端）/ npm（前端）| Maven 3.9.16 |

## 3. 进程与端口

| 服务 | 端口 | 说明 |
|---|---|---|
| MySQL | 3306 | Windows 原生安装，datadir=`D:/MySQL/data` |
| 后端 | **9091**（必须显式 `--server.port=9091`）| `java -jar target/yf-bev2-api.jar --server.port=9091` |
| 前端 | 8000 | `npm run dev`（vite，代理后端 `/api` → 9091）|

> 配置文件 `server.port` 默认 8080 但**实际不生效**：后端启动必须带 `--server.port=9091`，否则绑随机端口。见 KNOWN_ISSUES。

## 4. 目录结构

```
workspace/keryo-training/
├── yf-bev2-api/                  # 后端（SpringBoot）
│   ├── pom.xml
│   ├── src/main/java/
│   │   ├── com.yf.*              # 上游基底（exam/repo/paper/user/menu/dict/depart/...）
│   │   └── com.keryo.training.*  # 定制业务扩展包
│   │       ├── modules/aiconfig  # AI 配置（T-17）
│   │       ├── modules/audit     # 审计日志 AOP + 管理端只读查看
│   │       ├── modules/auth      # 登录鉴权相关
│   │       ├── modules/stat      # 统计看板
│   │       ├── modules/training  # 培训内容（原课件，T-13 更名）
│   │       ├── modules/trainingplan # 培训计划 + 学员参与
│   │       └── modules/user      # 忘记密码申请记录（T-18）
│   ├── src/main/resources/
│   │   ├── application.yml / application-dev.yml / application-local.yml
│   │   ├── mapper/               # MyBatis XML
│   │   ├── db/                   # 迁移脚本
│   │   └── static/               # 预编译前端（未启用，实际走分离部署）
│   └── src/test/java/            # 12 个后端回归测试类（当前工作区）
├── yf-bev2-vue/                  # 前端（Vue3+TS）
│   └── src/
│       ├── views/                # Dashboard/Exam/Login/Plan/Stat/System/Training
│       ├── api/                  # axios 封装（@/config/axios）
│       ├── store/modules/        # pinia
│       └── router/ permission.ts # 动态路由 + 守卫
├── scripts/
│   ├── start_all.sh              # 一键启动 MySQL+后端+前端（已固化环境）
│   ├── run_unit_tests.sh         # 单测运行（离线 JUnitCore）
│   ├── backup_db.sh / restore_db.sh  # 备份/恢复
├── 启动服务器.bat                # 用户双击启动入口
├── yf_boot_exam.sql              # 种子 SQL（含基础表+初始数据，品牌已替换为中性平台名）
├── docs/                         # 项目文档（验收/操作/权限模型/等保等）
└── test_*.py / e2e_*.js          # 验收自动化脚本（Python urllib / Playwright）
```

## 5. 后端模块关系（com.keryo.training 扩展包）

```
KeryoComponentConfig (com.yf.config.keryo) → @ComponentScan("com.keryo.training")
  ├── aiconfig   → 管理端 AI 设置（sys:ai-config:view/edit，仅 admin）
  ├── audit      → @AuditLog 切面，写 el_keryo_audit_log（PII 脱敏）
  ├── stat       → 统计看板（keryo:stat:overview，按 data_scope）
  ├── training   → 培训内容 CRUD/上传/在线预览（原课件）
  ├── trainingplan → 培训计划 CRUD/审批/关联培训/分配学员；学员端 my 过滤
  └── user       → 忘记密码申请记录（el_keryo_forget_pass）
```

上游模块（com.yf.modules.exam）：`exam`（考试配置/组卷）、`paper`（考试会话/答题/判分/阅卷）、`repo`（题库/题目/审核/AI 出题）、`cheat`（防切屏/违规日志）。

## 6. 核心数据流

### 6.1 考试全链路
```
el_repo(题库) → el_repo_qu(题目, APPROVED) 
  → el_exam(考试, score_visible) + el_exam_rule(组卷规则: 按题库+题型随机抽 APPROVED 题)
  → 学员进入 → el_paper(考试会话) + el_paper_qu(逐题, 含 answer_text/actual_score)
  → 交卷自动判分(单选/多选/判断/填空) → el_exam_record(成绩汇总)
  → ESSAY 人工阅卷(管理端 isAdmin=1) → 重算总分同步 record
  → 成绩公示(score_visible=0 时学员端掩码为"待公布") → 补考(record/retry 重置 tryCount)
```

### 6.2 培训闭环
```
el_keryo_training(培训内容) ← 培训计划 el_keryo_training_plan.courseware_id
el_keryo_training_plan(状态机 DRAFT→PENDING→ACTIVE→ENDED→ARCHIVED)
  └─ el_keryo_plan_user(学员参与)
  └─ exam_id（计划关联考试；学员端考试列表 = 参与计划的 ACTIVE/ENDED 计划所关联考试）
```

### 6.3 学员端可见性规则
- 培训列表 `/api/keryo/training/my`：仅返回当前学员参与的 ACTIVE/ENDED 计划所关联培训。
- 考试列表 `/api/exam/exam/exam/client-paging`：按参与计划关联考试的 examIds 过滤（2026-08-13 修复，见 CHANGELOG）。

## 7. 主要 API（关键接口）

| 接口 | 说明 | 认证 |
|---|---|---|
| `POST /api/sys/user/login` | 登录（userName 非 username）| anon |
| `POST /api/sys/user/reg` | 注册（userName/realName/password 必填）| anon |
| `POST /api/sys/user/forget-check` | 忘记密码申请（账号+姓名+验证码→写申请记录）| anon |
| `POST /api/keryo/forget-pass/paging\|handle` | 申请记录管理端列表/处理（sys:user:forget-*）| JWT |
| `POST /api/exam/exam/exam/client-paging` | 学员端考试列表（按计划过滤）| JWT |
| `POST /api/exam/paper/qu/review-essay` | ESSAY 人工阅卷 | JWT |
| `POST /api/exam/exam/record/retry` | 补考（exam:record:retry）| JWT |
| `POST /api/exam/exam/score-visible` | 成绩公示开关（exam:exam:edit）| JWT |
| `POST /api/keryo/training/my` | 学员端我的培训 | JWT |
| `GET  /api/common/captcha/gen` | 图形验证码生成；`/debug` 仅显式配置开启，默认 404 | anon |
| `POST /api/exam/exam/exam/detail-for-exam` | 考试详情；服务层要求 ACTIVE/ENDED 计划参与关系 | JWT |
| `POST /api/keryo/training/{id}/preview` / `GET .../{id}/file` | 培训预览/下载；服务层要求管理角色或计划参与关系 | JWT |
| `POST /api/keryo/ai-config/view\|save\|test` | AI 配置（sys:ai-config:view/edit）| JWT |
| 菜单路由 `POST /api/sys/menu/routes` | 动态菜单（权限标签→前端路由）| JWT |

## 8. 数据库（库名 exam_keryo）

基础表（来自 yf_boot_exam.sql 种子）：
`el_sys_user / el_sys_user_role / el_sys_role / el_sys_menu / el_sys_role_menu / el_sys_depart / el_sys_dic / el_sys_dic_value / el_cfg_base / el_cfg_switch / el_exam / el_exam_rule / el_exam_record / el_exam_cheat_log / el_paper / el_paper_qu / el_paper_qu_answer / el_repo / el_repo_qu / el_repo_qu_answer / pl_plugin_data / pl_plugin_group / pl_plugin_schema / qrtz_*`

定制扩展表：
| 表 | 说明 |
|---|---|
| `el_keryo_training` | 培训内容（原课件）|
| `el_keryo_training_plan` | 培训计划（courseware_id 关联培训、exam_id 关联考试、dept_code）|
| `el_keryo_plan_user` | 计划-学员参与 |
| `el_keryo_ai_config` | AI 配置（provider/base_url/model/api_key/enabled）|
| `el_keryo_audit_log` | 审计日志（AOP 写入，管理端只读按账号/时间检索）|
| `el_keryo_forget_pass` | 忘记密码申请记录（T-18）|

> 已删除表（T-16/T-13）：`el_keryo_credit`（学分）、`el_practice_*`（练习）。
> 数据库初始化：`yf_boot_exam.sql`（含基础表 + 初始数据）。增量表结构变更记录在 `yf-bev2-api/src/main/resources/db/`。

## 9. 配置与环境

- 数据源、Redis 假值：`application-local.yml` / `application-dev.yml`；数据库连接使用 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 环境变量。
- **AI Key 已迁入数据库** `el_keryo_ai_config`，yml 不再含 Key（T-17）；yml 仅留 base-url 兜底。
- 上传文件物理存储：`D:/upload/<yyyy/MM/dd>/`（pl_plugin_data upload-local 插件 localDir）。
- 日志：`logs/`（logback-spring.xml），后端启动日志见 `keryo-training/logs/backend-YYYYMMDD.log`。

## 10. 外部依赖

| 依赖 | 用途 | 备注 |
|---|---|---|
| DeepSeek API | AI 出题 | base_url https://api.deepseek.com/chat/completions，默认模型 deepseek-v4-flash，Key 存 DB |
| Office Online Viewer | PPTX/DOCX 在线预览 | iframe embed，需公网可访问文件 URL（内网可能失效）|
| CDN `be2.yfhl.net` / `cdn.yfhl.net` | 历史图片资源 | 前端 logo/登录图，用户已部分替换 |

## 11. 测试与验收

- 后端单元测试：3 个（`scripts/run_unit_tests.sh`，离线 JUnitCore 方式）。
- 验收自动化：`test_t*.py`（Python urllib 全链路断言）、`e2e_t*.js`（Playwright）。
- 权限矩阵历史测试：`archive/tests/legacy-python/test_perm_matrix.py`（7 角色 × 关键接口，已归档，不纳入 baseline）。
- 文档：`docs/验收手册.md`（v2.1）、`docs/操作手册.md`、`docs/权限模型梳理与设计.md`。

