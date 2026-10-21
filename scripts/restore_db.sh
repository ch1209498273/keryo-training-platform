#!/usr/bin/env bash
# ============================================================
# 血透中心医护培训平台 · MySQL 恢复脚本（T-08.1）
# 流程：openssl 解密 → gzip 解压 → mysql 导入
# 用法：bash restore_db.sh <备份文件.enc>
# ============================================================
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"

if [ $# -lt 1 ]; then
  echo "用法：bash restore_db.sh <备份文件.sql.gz.enc>" >&2
  exit 1
fi
ENC_FILE="$1"

DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-${DB_PASS:-}}"
DB_NAME="${DB_NAME:-exam_keryo}"
BACKUP_PASS="${BACKUP_PASS:-}"
MYSQL="${MYSQL:-mysql}"
GZIP="${GZIP:-gzip}"
OPENSSL="${OPENSSL:-openssl}"

if [ -z "$BACKUP_PASS" ]; then
  read -rsp "输入备份加密口令: " BACKUP_PASS
  echo ""
fi

if [ -z "$DB_PASSWORD" ]; then
  echo "错误：请设置 DB_PASSWORD（旧变量 DB_PASS 仍兼容）" >&2
  exit 1
fi

TMP_GZ="${ENC_FILE%.enc}"
TMP_SQL="${TMP_GZ%.gz}"

echo "[1/3] AES-256-CBC 解密 ..."
"$OPENSSL" enc -d -aes-256-cbc -pbkdf2 -iter 10000 \
  -in "$ENC_FILE" -out "$TMP_GZ" -k "$BACKUP_PASS"

echo "[2/3] gzip 解压 ..."
"$GZIP" -d -f "$TMP_GZ"

echo "[3/3] 导入 ${DB_NAME} ..."
"$MYSQL" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$TMP_SQL"

echo "✅ 恢复完成：$TMP_SQL"

