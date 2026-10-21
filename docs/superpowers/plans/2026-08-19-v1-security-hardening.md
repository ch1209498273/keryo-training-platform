# Plan: V1.0 安全与业务缺陷闭环

**Goal:** 在保持现有架构和数据库结构不变的前提下，修复交接审计中的 P0/P1 鉴权、权限、考试资格、统计和安全边界问题，并用本轮测试证据更新 UAT 台账。

**Approach:** 每个批次先增加能复现问题的负向测试，再做最小范围实现；保留工作区现有改动，不使用 reset/checkout，不删除历史备份或业务数据。

**Verification:** 后端使用 JDK17 的 Maven/既有 Java 测试入口，前端只运行当前依赖可用的测试和构建检查；任何无法运行的验证记录为环境阻塞，不伪造通过。

## Task 1: 建立可追溯基线与测试骨架

**Files:** `docs/UAT_BUG_LEDGER.md`, `yf-bev2-api/src/test/java/com/yf/...`, `scripts/run_unit_tests.sh`

- [x] 保存本轮目标文件的 `git diff --name-only` 和当前测试入口输出，避免覆盖既有修改。
- [x] 为 BUG-UAT-001～010 增加与现有 JUnit 版本兼容的测试类/测试方法；测试先断言当前缺陷或安全边界。
- [x] 保持脚本只编译和运行测试，不连接生产数据库、不写入业务数据。

## Task 2: 修复登录输入与 refresh 令牌校验

**Files:** `yf-bev2-api/src/main/java/com/yf/system/modules/user/service/impl/SysUserServiceImpl.java`, `yf-bev2-api/src/main/java/com/keryo/training/modules/auth/controller/AuthController.java`

- [x] 登录入口和 `checkAndLogin` 都拒绝空用户名/空密码；失败路径统一记录暴力破解计数。
- [x] 保留每次登录验证码校验，并覆盖空验证码、错误验证码和空密码的负向测试。
- [x] refresh 令牌验证类型、签名、过期时间、黑名单、用户存在性和用户状态；只有通过全部检查后才撤销旧令牌并换发新令牌。
- [x] 忘记密码申请落库异常向调用方返回失败，不能吞掉异常后记录成功。

## Task 3: 外置 JWT 密钥并强制签名验证

**Files:** `yf-bev2-api/src/main/java/com/yf/ability/shiro/jwt/JwtUtils.java`, `yf-bev2-api/src/main/java/com/yf/ability/shiro/MyShiroRealm.java`, `yf-bev2-api/src/main/resources/application*.yml`, relevant tests

- [x] 增加 `keryo.security.jwt-secret` 配置解析；生产/非 dev profile 缺失时快速失败，Java 源码不再保存硬编码密钥。
- [x] 让签发、验证和过期判断使用同一外置密钥；验证先完成签名和标准 claims 校验，再读取用户名。
- [x] 在 Shiro Realm 中拒绝篡改签名、缺少 subject/username、过期和不存在/禁用用户的令牌。
- [x] 明确旧令牌在密钥变更后的重新登录行为并在台账记录。

## Task 4: 收紧匿名链并修复 CORS/Origin/验证码调试

**Files:** `yf-bev2-api/src/main/java/com/yf/config/ShiroConfig.java`, `yf-bev2-api/src/main/java/com/yf/ability/shiro/aop/JwtFilter.java`, `yf-bev2-api/src/main/java/com/yf/config/CorsConfig.java`, `yf-bev2-api/src/main/java/com/keryo/training/common/security/SecurityHeadersFilter.java`, `yf-bev2-api/src/main/java/com/yf/ability/captcha/controller/CaptchaController.java`

- [x] 匿名路径改为精确匹配；移除不属于公开 API 的宽泛放行项，保留登录、验证码、refresh 和明确静态资源。
- [x] OPTIONS 响应按请求 Origin 返回允许列表，禁止凭据模式下的 `*`。
- [x] Origin 校验使用规范化后的精确 origin 比较，拒绝 `evil.example.com` 等包含式绕过。
- [x] 验证码调试接口改为显式配置门控且默认关闭。

## Task 5: 补齐管理权限与服务层资源边界

**Files:** `TrainingController.java`, `TrainingPlanController.java`, `UserImportController.java`, `UploadController.java`, related service implementations and tests

- [x] 为培训资料、培训计划、批量导入、上传及其它管理写操作补 `@RequiresPermissions`；读取接口至少要求认证。
- [x] 在服务层复核角色、部门数据范围、资源归属和本人范围，确保直接调用 service/ID 不能绕过控制器注解。
- [x] 为普通用户调用管理写接口、跨部门资源 ID 和未授权下载增加 403/失败断言。

## Task 6: 在考试服务层落实培训计划资格

**Files:** `yf-bev2-api/src/main/java/com/yf/modules/exam/paper/service/impl/PaperServiceImpl.java`, `yf-bev2-api/src/main/java/com/keryo/training/modules/trainingplan/service/impl/TrainingPlanServiceImpl.java`, `ExamController.java`, related mappers/tests

- [x] 将计划参与关系、计划状态、时间窗口和考试次数检查集中到预检/创建/进入考试共用路径。
- [x] 对非参与者直接提交 paper/exam ID 返回明确失败，不能只依赖列表过滤。
- [x] 保持现有状态机和数据库结构，不新增表或修改生产数据。

## Task 7: 修复仪表盘、计划自动结束和密码/角色边界

**Files:** `DashboardServiceImpl.java`, `TrainingPlanServiceImpl.java`, related DTOs/tests, password services

- [x] 修正通过率数值计算和 Boolean/Integer 混用；未完成统计仅面向当前计划参与者和对应考试。
- [x] 为自动结束增加实际调度触发或现有调度入口的可靠调用，并测试边界时间。
- [x] 复核角色映射、所有权、数据范围为空时的行为；修复管理员改密/忘记密码异常吞错。

## Task 8: 统一上传类型和运行边界

**Files:** `FileSecurityValidator.java`, `LocalUpServiceImpl.java`, upload tests, `start_all.py`/scripts only where required

- [x] 统一允许的扩展名、MIME 和 magic bytes；伪造后缀或文件头必须失败。
- [x] 不扩大文件类型白名单，不把调试或本地启动修复冒充生产能力。

## Task 9: 回归验证与证据闭环（进行中）

**Files:** `docs/UAT_BUG_LEDGER.md`, `docs/KNOWN_ISSUES.md`, `docs/ROADMAP.md`, `docs/PORTFOLIO_RELEASE.md`, `docs/CASE_STUDY_LOG.md`

- [x] 使用 JDK17 运行既有后端测试、目标新增测试和可用的脚本检查；运行前端现有测试/构建，记录环境阻塞。（后端 `test-compile` + JUnitCore 36 项 PASS；前端/人工 UAT 尚未在本轮执行。）
- [x] 每个修复项从 `OPEN → IN PROGRESS → FIXED → RETEST`，只有复测证据成立才允许 `CLOSED`。（001～010 当前为 FIXED，等待负责人 RETEST。）
- [x] 将生产数据库迁移、真实人工 UAT、CI/Docker、灾备和备份介质清理保留为明确开放项，不将历史 PASS 当作本轮证据。
- [x] 复核最终 diff，确认没有凭据、Token、生产数据或无关架构变更。

