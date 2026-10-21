#!/usr/bin/env bash
# ============================================================
# 血透中心医护培训平台 · 一键启动脚本（M1 准入：docker-compose 降级）
# 本机无 Docker → 直接启动 MySQL + 后端 + 前端三个进程
# 用法：bash scripts/start_all.sh
# ============================================================
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_CMD="${JAVA_CMD:-java}"
MYSQLD="${MYSQLD:-mysqld}"
NODE="${NODE:-node}"
MYSQL_DATA_DIR="${MYSQL_DATA_DIR:-}"

# 数据库连接由环境变量提供，避免把口令写入配置文件或启动命令。
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-3306}"
export DB_NAME="${DB_NAME:-exam_keryo}"
export DB_USER="${DB_USER:-root}"

echo "== [1/3] 启动 MySQL (3306) =="
if ! netstat -ano | grep -q ":3306.*LISTENING"; then
  MYSQL_ARGS=(--port=3306 --console)
  if [ -n "$MYSQL_DATA_DIR" ]; then MYSQL_ARGS+=(--datadir="$MYSQL_DATA_DIR"); fi
  "$MYSQLD" "${MYSQL_ARGS[@]}" &
  sleep 8
  echo "  MySQL 已启动"
else
  echo "  MySQL 已在运行"
fi

echo "== [2/3] 启动后端 (9091) =="
if ! netstat -ano | grep -q ":9091.*LISTENING"; then
  if [ -z "${DB_PASSWORD:-}" ]; then
    echo "[错误] 未设置 DB_PASSWORD，后端不会启动。请先在当前 shell 设置数据库口令。" >&2
    exit 1
  fi
  cd "$ROOT/yf-bev2-api"
  "$JAVA_CMD" -jar target/yf-bev2-api.jar --server.port=9091 &
  sleep 15
  echo "  后端已启动 (http://localhost:9091)"
else
  echo "  后端已在运行"
fi

echo "== [3/3] 启动前端 (8000) =="
if ! netstat -ano | grep -q ":8000.*LISTENING"; then
  cd "$ROOT/yf-bev2-vue"
  npm run dev &
  sleep 20
  echo "  前端已启动 (http://localhost:8000)"
else
  echo "  前端已在运行"
fi

echo ""
echo "=========================================="
echo "  ✅ 全部就绪"
echo "  前端:  http://localhost:8000"
echo "  后端:  http://localhost:9091"
echo "=========================================="

