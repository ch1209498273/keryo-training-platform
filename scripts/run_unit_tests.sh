#!/usr/bin/env bash
# Windows/Git Bash compatibility entry; the PowerShell runner owns discovery.
# Compile current sources first with JDK17: mvn package -DskipTests
set -euo pipefail
SCRIPT_DIR="$(cd -- "${BASH_SOURCE[0]%/*}" && pwd)"
ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
if command -v cygpath >/dev/null 2>&1; then ROOT="$(cygpath -m "$ROOT")"; fi
export BASELINE_BACKEND_TARGET="${BASELINE_BACKEND_TARGET:-$ROOT/yf-bev2-api/target}"
if [ -z "${BASELINE_JAVA_HOME:-}" ] && [ -n "${JAVA_HOME:-}" ]; then
  export BASELINE_JAVA_HOME="$JAVA_HOME"
fi
if [ -z "${BASELINE_MAVEN_REPO:-}" ] && [ -n "${MAVEN_REPO_LOCAL:-}" ]; then
  export BASELINE_MAVEN_REPO="$MAVEN_REPO_LOCAL"
fi
exec "${BASELINE_POWERSHELL:-powershell.exe}" -NoProfile -ExecutionPolicy Bypass \
  -File "$ROOT/scripts/baseline/run_java_tests.ps1" "$@"

