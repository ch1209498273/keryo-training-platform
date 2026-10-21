#!/usr/bin/env bash
# ============================================================
# 血透中心医护培训平台 · MySQL 备份脚本（T-08.1 M4 备份加密）
# 流程：mysqldump → gzip → openssl aes-256-cbc 加密 → 保留 7 天
# 用法：
#   bash backup_db.sh                  # 手动备份（读环境变量或默认值）
#   BACKUP_PASS=xxx bash backup_db.sh  # 指定加密口令
# 计划任务（Windows 任务计划程序）：
#   每天 03:00 执行：bash scripts/backup_db.sh
# ============================================================
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"

# ---------- 配置（可被环境变量覆盖） ----------
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-${DB_PASS:-}}"
DB_NAME="${DB_NAME:-exam_keryo}"
BACKUP_PASS="${BACKUP_PASS:-}"          # 为空则提示输入
BACKUP_DIR="${BACKUP_DIR:-$ROOT/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

MYSQLDUMP="${MYSQLDUMP:-mysqldump}"
GZIP="${GZIP:-gzip}"
OPENSSL="${OPENSSL:-openssl}"

# ---------- 加密口令 ----------
if [ -z "$BACKUP_PASS" ]; then
  read -rsp "输入备份加密口令: " BACKUP_PASS
  echo ""
fi

if [ -z "$DB_PASSWORD" ]; then
  echo "错误：请设置 DB_PASSWORD（旧变量 DB_PASS 仍兼容）" >&2
  exit 1
fi
if [ ${#BACKUP_PASS} -lt 8 ]; then
  echo "错误：加密口令至少 8 位" >&2
  exit 1
fi

# ---------- 备份 ----------
mkdir -p "$BACKUP_DIR"
STAMP="$(date +%Y%m%d_%H%M%S)"
DUMP_FILE="${BACKUP_DIR}/${DB_NAME}_${STAMP}.sql"
ENC_FILE="${DUMP_FILE}.gz.enc"

echo "[1/4] mysqldump ${DB_NAME} ..."
"$MYSQLDUMP" -u"$DB_USER" -p"$DB_PASSWORD" --single-transaction --routines --triggers \
  --default-character-set=utf8mb4 "$DB_NAME" > "$DUMP_FILE"

echo "[2/4] gzip 压缩 ..."
"$GZIP" -f "$DUMP_FILE"

echo "[3/4] AES-256-CBC 加密 ..."
"$OPENSSL" enc -aes-256-cbc -salt -pbkdf2 -iter 10000 \
  -in "${DUMP_FILE}.gz" -out "$ENC_FILE" -k "$BACKUP_PASS"

echo "[4/4] 清理（保留 ${RETENTION_DAYS} 天）..."
rm -f "${DUMP_FILE}.gz" 2>/dev/null || true
# 过期备份清理（trash 方式不可用时降级为直接删除，由 find 处理）
find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz.enc" -mtime +"$RETENTION_DAYS" -delete 2>/dev/null || true

SIZE=$(du -h "$ENC_FILE" | cut -f1)
echo "✅ 备份完成：$ENC_FILE (${SIZE})"
echo "   恢复命令：bash restore_db.sh $ENC_FILE"

