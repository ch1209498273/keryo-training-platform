# 功能状态盘点（FEATURE_STATUS）

> 交接文档 · 2026-08-14 · 基于实际代码 + 验收记录
> 分类：A=已完成可用 / B=已实现但有问题 / C=部分实现 / D=未实现 / E=已废弃

---

## A. 已完成且基本可用

| # | 功能 | 前端 | 后端 | 数据库表 | 备注 |
|---|---|---|---|---|---|
| A1 | 登录（账号密码+图形验证码）| views/Login | SysUserController.login | el_sys_user | 每次登录强制验证码；debug 明文接口仅显式配置开启，默认 404 |
| A2 | 注册 | RegisterForm.vue | SysUserController.reg | el_sys_user | 默认角色 keryo_nurse，强制姓名/账号 |
| A3 | 忘记密码申请（管理员审核重置）| ForgetPasswordForm.vue / System/User/ForgetPass.vue | forget-check / keryo/forget-pass | el_keryo_forget_pass | T-18，2026-08-13 新增 |
| A4 | RBAC 权限（7 角色 + data_scope）| 菜单随角色动态 | Shiro + el_sys_role_menu | el_sys_role/menu/user_role | 权限矩阵测试 14/14 |
| A5 | 用户/角色/菜单/部门/字典/个性配置/插件管理 | views/System/* | SysUser/SysRole/SysMenu/... | el_sys_* | 仅 admin |
| A6 | 题库管理（建题/两级审核/Excel/Word 导入）| Exam/Repo/* | modules.exam.repo | el_repo/el_repo_qu | 审核状态机完整 |
| A7 | AI 出题（DeepSeek，人工审核入库）| Repo/Qu 出题入口 | AiQuService | el_keryo_ai_config | AI 配置可换供应商 |
| A8 | 在线考试（组卷/答题/计时/自动判分）| Exam/Exam/Client/* | modules.exam.paper | el_exam/rule/paper/qu | 主链路完整 |
| A9 | ESSAY 人工阅卷 | Exam.vue(isAdmin=1) | PaperQu.review-essay | el_paper_qu.actual_score | 阅卷后重算总分 |
| A10 | 成绩公示（T-15）| Exam.vue 公布按钮 | score-visible / detail 掩码 | el_exam.score_visible | 学员端"待公布" |
| A11 | 补考（T-14）| Record.vue 补考按钮 | exam/record/retry | el_exam_record.try_count | 仅总监/超管 |
| A12 | 成绩查看/导出/修正 | Exam/Exam/Record.vue | ExamRecord | el_exam_record | 修正仅总监 |
| A13 | 培训内容管理 + 在线预览 | Training/ | modules.training | el_keryo_training | 管理端角色约束；学员详情/预览/下载重复校验计划参与关系 |
| A14 | 培训计划（CRUD/审批/关联培训/分配学员）| Plan/ | modules.trainingplan | el_keryo_training_plan/plan_user | 状态机、数据范围和参与关系均在服务层复核 |
| A15 | 学员端培训/考试列表按计划过滤 | ClientTraining / Client/List.vue | training/my + client-paging | — | 列表过滤 + 直达详情/考试接口二次校验 |
| A16 | 统计看板 | Stat/Dashboard.vue | modules.stat | 聚合查询 | 通过率、未完成资格和空 scope 已收敛；权限 `keryo:stat:overview` |
| A17 | 防切屏 + 作弊日志 | Client 计时组件 | modules.exam.cheat | el_exam_cheat_log | IP 变更/快速答题检测 |
| A18 | 登录失败锁定 | — | LoginProtectionService | Redis/内存 | 失败次数阈值 |
| A19 | 审计日志 AOP + 管理端查看 | AuditLog.vue | modules.audit | el_keryo_audit_log | PII 脱敏；只读按账号/时间检索 |
| A20 | 个人资料/改密/所属中心选择 | System/User/Client/* | SysUserController.update/pass | el_sys_user.dept_code | 中心须在 el_sys_depart |
| A21 | 忘记密码申请管理端列表 | ForgetPass.vue | keryo/forget-pass | el_keryo_forget_pass | admin+医护长 |

## B. 已实现但存在问题

| # | 功能 | 问题 | 涉及位置 | 建议 |
|---|---|---|---|---|
| B1 | 演示题库 | 历史上混入非血透脏题；已按 BK-03 删除 16 道目标脏题及 62 条答案记录，历史试卷快照保留 | el_repo_qu (repo_id=1910524655864012801) | 已处理，人工 UAT 继续复核抽题内容 |
| B2 | 演示培训内容 | 历史上存在占位 PDF；已按 BK-04 使用真实血液透析培训 PPTX 并补齐部署文件 | el_keryo_training / D:/upload/ | 已处理，人工 UAT 继续复核文件打开体验 |
| B3 | 演示培训计划 | 历史上 `exam_id=NULL`；已按 BK-05 将 `t13_demo_plan` 关联真实培训和演示考试 | t13_demo_plan | 已处理，人工 UAT 继续复核学员可见性 |
| B4 | `reset-pass` 接口 | 已下线；忘记密码改为账号+姓名+验证码申请，管理员审核后处理 | SysUserController / ForgetPassController | 已完成 |
| B5 | 登录验证码策略 | 每次登录始终要求验证码，前端展示与后端校验已统一 | LoginForm.vue / SysUserServiceImpl.login | 已完成 |
| B6 | 演示题库分类 | 已归到「其他」，但题目本身与血透无关（数据层）| el_repo.cat_id | 数据清理 |

## C. 部分实现/半成品

| # | 功能 | 完成度 | 说明 |
|---|---|---|---|
| C1 | 防切屏策略 | 有基础实现，无"标黄/标红"前端展示闭环 | 后端记录 cheat_log，但考试结果页是否展示违规分析【待确认】 |
| C2 | AI 出题质量 | 可出题，但演示题库脏题源于 AI/导入数据 | 需要人工审核把关（已有两级审核） |
| C3 | 统计看板 | 当前已实现参训/未完成基础名单；多计划业务口径和展示体验仍需人工 UAT 复核 | 不将自动回归当作业务报表验收 |
| C4 | 前端 E2E 脚本 | 有 e2e_t*.js（Playwright），但本环境 Playwright 不可用，仅历史留档 | 不可在本机重跑 |

## D. 尚未实现

| # | 功能 | 来源 | 备注 |
|---|---|---|---|
| D1 | M2：公网部署 + HTTPS + ICP 备案 | WBS M2 | 备案移交信息技术部 |
| D2 | M2：完整渗透测试 / ZAP 全量扫描 | WBS M2 / T-11 深度版 | M1 只做基础版 |
| D3 | M2：POI 5.x 升级 | 已完成（2026-08-15） | 当前依赖为 POI 5.2.5；后续只需按 Portfolio Release 范围做回归 |
| D4 | M2：正式 DPIA 文档 / 等保三级合规 | M2 | 已有基础版定级 |
| D5 | M2：审计日志查看页 | 已完成（2026-08-15） | 只读分页接口、管理端页面和菜单授权脚本已完成 |
| D6 | Future Production：CSP 强制模式 | 暂缓 | 安全头已注入，当前保持 Report-Only；正式生产/试点时再确认强制切换 |
| D7 | 强制改密 / 密码历史 / 密码 90 天过期 | 已完成（2026-08-15） | 旧账号回填豁免；独立临时改密令牌；最近5次密码不可复用；90天过期和7天提醒 |
| D8 | 年度参训统计报表 | 部分已实现 | 看板已按年度计划-学员参与关系计数；完整报表维度仍需业务确认 |
| D9 | 完整数据大屏（总监视角）| P2 二期 | 未排期 |
| D10 | 小程序端 | P2 二期 | 未排期 |
| D11 | 摄像头监考 / 人脸识别 | P2 二期 | 未排期 |
| D12 | 小组长参与管理培训能力 | 已完成（2026-08-15） | 小组长可在本部门范围管理计划参与人员；仍需按 UAT 复核体验 |

## E. 已废弃 / 已删除 / 无效

| # | 内容 | 处理 |
|---|---|---|
| E1 | 随机练习 / 错题本 / 练习统计（T-04.4~4.6）| 已删除：el_practice_* 表 + 练习中心菜单 + 前端 |
| E2 | 学分系统（T-16）| 已删除：el_keryo_credit 表 + CreditController + 前端 + 菜单；备份在 backups/ |
| E3 | 课件概念（T-13 更名）| 全链路更名「培训」；历史 API /courseware 迁移 /api/keryo/training |
| E4 | `user` 角色 | 停用（保留数据，不授权），注册默认改 keryo_nurse |
| E5 | LibreOffice 转 PDF 方案（CR-03）| 废弃，改 Office Online Viewer |
| E6 | Docker/WSL2 本地开发（CR-02）| 废弃，Windows 原生 MySQL + 内存 Redis |
| E7 | `KeryoApplication` 双启动类方案（CR-05）| 改 KeryoComponentConfig |
| E8 | yml 里的 AI Key（T-17）| 环境变量优先，数据库值兼容回退 |
| E9 | 「演示」题库分类 | 已删（分类精简为三），题库归「其他」 |

## 完成度评估

- **核心业务闭环（登录→培训→考试→出分→公示→看板）**：✅ 完整可用
- **管理后台（RBAC/用户/菜单/字典/配置/部门）**：✅ 完整可用
- **安全基线（bcrypt/防注入/防XSS/防切屏/审计/锁定）**：✅ 主体完成，M2 遗留项见 D 节
- **AI 能力**：✅ 基础可用（可换供应商、可测试、可降级）
- **二期规划（大屏/小程序/监考/深度合规）**：⬜ 未启动

