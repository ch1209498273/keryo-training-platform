# 数据库迁移执行手册（手工版）

> 本项目**没有接 Flyway**。`db/migration/V*.sql` 是**版本化手册**，不是自动执行脚本：
> 谁改谁按本文流程对 `exam_keryo` 库手工执行，并在 `docs/VERSION_LEDGER.md` 留痕。
>
> 起因：UAT-2026-10-20 发现 BUG-UAT-062 —— V38 只改了 `el_sys_menu.meta_title`，
> 接口返回的还是旧菜单名。查下来不是改错了表，而是**手工 SQL 绕过了 `@CacheEvict`**，
> Redis 里的 `sys:menu::routes-<角色>` / `sys:menu::permissions-<角色>` 仍是旧值。

## 一、执行前

1. **备份**（涉及菜单/权限/答卷等核心表时必做）

   ```bash
   MYSQL_PWD=*** "C:/Program Files/MySQL/MySQL Server 8.4/bin/mysqldump.exe" \
     -uroot --single-transaction --default-character-set=utf8mb4 \
     exam_keryo > scripts/uat/backup_<TAG>_$(date +%Y%m%d).sql
   ```

2. **确认库连接**：密码在 `HKCU\Environment\DB_PASSWORD`（`reg.exe` 被沙箱禁用，用 Python `winreg` 读）。
   中文路径 `source` 会乱码，**先拷到 ASCII 临时路径再执行**。

## 二、执行

```bash
mysql -uroot exam_keryo --default-character-set=utf8mb4 < /tmp/V39__xxx.sql
```

执行中用 `SELECT ROW_COUNT();` 或执行后 `SELECT ... WHERE ...` 核验影响行数，
不要只看"没有报错"。迁移脚本一律**幂等**（`IF NOT EXISTS` / 条件更新），便于重跑。

## 三、执行后（最容易被漏掉的一步）

### 3.1 菜单/权限类变更 → 必须刷新菜单缓存

改了 `el_sys_menu`（名称、路径、显隐、权限标识）或 `el_sys_role_menu` 之后：

- **正常路径**：登录管理端 → 系统管理 → 菜单管理 → 点 **「刷新菜单缓存」**
  （对应接口 `POST /api/sys/menu/refresh-cache`，仅 admin）。
- 按钮点击后当前登录用户**需要重新登录**，前端 `userInfo` / `roleRouters` 才会重新拉取。

**禁止**用 `redis-cli FLUSHDB`。会话键 `yf:exam:name:*` 与菜单缓存同库，
FLUSHDB 会把所有在线用户踢下线。真要手工删，只删这两类键：

```bash
redis-cli --scan --pattern 'sys:menu::routes-*'      | xargs -r redis-cli del
redis-cli --scan --pattern 'sys:menu::permissions-*' | xargs -r redis-cli del
```

### 3.2 数据/结构类变更 → 重启后端

改表结构（加列、改索引）后必须重启后端（走 `/tmp/keryo_watchdog.sh` 守护，
构建新 jar 到 `yf-bev2-api/target/` 后 kill 进程，看门狗 ≤70s 自动拉起），
否则 MyBatis 结果映射仍按旧列工作。

### 3.3 前端静态包

`9091` 端口的正式入口用的是后端内置静态包，前端改动必须
`bash scripts/rebuild_frontend.sh` 重新构建（`vite build --mode base`，**不能**用 dev 模式）。

## 四、回归

| 变更类型 | 最低回归 |
|---|---|
| 菜单/权限 | 目标角色重新登录后菜单名与按钮正确；`docs/UAT` 里的 6 角色遍历脚本 |
| 表结构 | `mvn test` 全绿 + 相关模块 API 冒烟 |
| 业务数据 | `scripts/uat/uat_round_2026_10_19.py --phase post` |

## 五、留痕

在 `docs/VERSION_LEDGER.md` 追加一行：版本号、日期、变更摘要、执行人、
是否已刷新菜单缓存、回归结论。**"执行了但没刷缓存"是最常见的返工原因，这一栏必须写。**
