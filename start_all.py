# -*- coding: utf-8 -*-
"""
医护培训平台 · Windows 本地一键启动脚本

启动 MySQL、后端和前端中当前未运行的服务，并在启动后进行端口和 HTTP 检查。
数据库口令只从 DB_PASSWORD 环境变量读取；未设置时在交互式终端安全提示，
不会把口令写入仓库或启动命令行。
"""

from __future__ import annotations

import getpass
import json
import os
import shutil
import socket
import subprocess
import sys
import time
from pathlib import Path
from typing import Iterable, Optional
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


ROOT = Path(__file__).resolve().parent
API_DIR = ROOT / "yf-bev2-api"
WEB_DIR = ROOT / "yf-bev2-vue"
LOG_DIR = ROOT / "logs"
BACKEND_JAR = API_DIR / "target" / "yf-bev2-api.jar"

MYSQL_PORT = int(os.environ.get("KERYO_MYSQL_PORT", "3306"))
BACKEND_PORT = int(os.environ.get("KERYO_BACKEND_PORT", "9091"))
FRONTEND_PORT = int(os.environ.get("KERYO_FRONTEND_PORT", "8000"))
FRONTEND_STARTUP_TIMEOUT = int(os.environ.get("KERYO_FRONTEND_STARTUP_TIMEOUT", "180"))


def unique_paths(paths: Iterable[Optional[Path]]) -> list[Path]:
    result: list[Path] = []
    seen: set[str] = set()
    for path in paths:
        if not path:
            continue
        key = str(path).lower()
        if key not in seen:
            seen.add(key)
            result.append(path)
    return result


def configured_path(*names: str) -> Optional[Path]:
    for name in names:
        value = os.environ.get(name)
        if value:
            return Path(value)
    return None


def find_executable(env_names: tuple[str, ...], names: tuple[str, ...], candidates: Iterable[Path]) -> Path:
    configured = configured_path(*env_names)
    if configured:
        if configured.is_file():
            return configured
        raise RuntimeError(f"配置的工具不存在：{configured}")

    for candidate in unique_paths(candidates):
        if candidate.is_file():
            return candidate

    for name in names:
        found = shutil.which(name)
        if found:
            return Path(found)
    raise RuntimeError(f"找不到工具 {', '.join(names)}；可通过 {', '.join(env_names)} 指定路径")


def java_is_17(java: Path) -> bool:
    try:
        completed = subprocess.run(
            [str(java), "-version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=10,
            check=False,
        )
    except (OSError, subprocess.SubprocessError):
        return False
    return completed.returncode == 0 and 'version "17.' in completed.stdout


def find_java17() -> Path:
    java_home = os.environ.get("JAVA_HOME")
    candidates = [
        configured_path("BASELINE_JAVA", "KERYO_JAVA"),
        Path(java_home) / "bin" / "java.exe" if java_home else None,
        Path("C:/Program Files/Java/jdk-17/bin/java.exe"),
        *Path("C:/Program Files/Java").glob("jdk-17*/bin/java.exe"),
    ]
    path_java = shutil.which("java.exe") or shutil.which("java")
    candidates.append(Path(path_java) if path_java else None)
    for candidate in unique_paths(candidates):
        if candidate.is_file() and java_is_17(candidate):
            return candidate
    raise RuntimeError("找不到 JDK 17。请设置 JAVA_HOME 或 BASELINE_JAVA，不能使用当前 PATH 中的其他 Java 版本。")


def find_npm() -> Path:
    node_home = os.environ.get("NODE_HOME")
    candidates = [
        configured_path("BASELINE_NPM", "KERYO_NPM"),
        Path(node_home) / "npm.cmd" if node_home else None,
        Path("C:/Program Files/nodejs/npm.cmd"),
    ]
    return find_executable(("BASELINE_NPM", "KERYO_NPM"), ("npm.cmd", "npm"), candidates)


def find_mysqld() -> Path:
    mysql_home = os.environ.get("MYSQL_HOME")
    candidates = [
        configured_path("BASELINE_MYSQLD", "KERYO_MYSQLD"),
        Path(mysql_home) / "bin/mysqld.exe" if mysql_home else None,
        Path("C:/Program Files/MySQL/MySQL Server 8.4/bin/mysqld.exe"),
        Path("C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqld.exe"),
    ]
    return find_executable(("BASELINE_MYSQLD", "KERYO_MYSQLD"), ("mysqld.exe", "mysqld"), candidates)


def mysql_working_directory(defaults_file: Optional[Path]) -> Path:
    """从数据目录启动 mysqld，避免配置中的相对 binlog 路径解析到错误目录。"""
    configured = configured_path("MYSQL_DATA_DIR", "KERYO_MYSQL_DATA_DIR")
    if configured:
        return configured

    if defaults_file:
        sibling_data = defaults_file.parent / "data"
        if sibling_data.is_dir():
            return sibling_data

    return ROOT


def port_open(port: int, host: str = "127.0.0.1", timeout: float = 1.5) -> bool:
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except OSError:
        return False


def wait_port(port: int, seconds: int = 60) -> bool:
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        if port_open(port):
            return True
        time.sleep(1)
    return port_open(port)


def http_ready(url: str, seconds: int = 60) -> bool:
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        try:
            with urlopen(url, timeout=3):
                return True
        except HTTPError:
            # A 4xx/5xx response still proves that the HTTP server is alive.
            return True
        except (OSError, URLError):
            time.sleep(1)
    return False


def backend_api_ready(seconds: int = 60) -> bool:
    """确认后端不仅监听端口，而且已经能访问数据库并返回公开配置。"""
    url = f"http://127.0.0.1:{BACKEND_PORT}/api/sys/config/detail"
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        request = Request(
            url,
            data=b"{}",
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        try:
            with urlopen(request, timeout=3) as response:
                payload = json.loads(response.read().decode("utf-8"))
                if response.status == 200 and payload.get("code") == 0:
                    return True
        except (HTTPError, OSError, URLError, ValueError, json.JSONDecodeError):
            pass
        time.sleep(1)
    return False


def log_path(name: str) -> Path:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    return LOG_DIR / name


def launch(command: list[str], cwd: Path, log_file: Path) -> subprocess.Popen[bytes]:
    with log_file.open("ab") as output:
        return subprocess.Popen(
            command,
            cwd=str(cwd),
            stdout=output,
            stderr=subprocess.STDOUT,
            env=os.environ.copy(),
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )


def get_db_password() -> None:
    if os.environ.get("DB_PASSWORD"):
        return
    if not sys.stdin.isatty():
        raise RuntimeError("未设置 DB_PASSWORD，且当前不是可输入密码的终端。请先设置 DB_PASSWORD 后再启动。")
    password = getpass.getpass("请输入本机 MySQL 密码（输入不可见）：")
    if not password:
        raise RuntimeError("未提供 MySQL 密码，启动已取消。")
    os.environ["DB_PASSWORD"] = password


def start_mysql() -> Optional[subprocess.Popen[bytes]]:
    if port_open(MYSQL_PORT):
        print(f"[MySQL] 已在运行（{MYSQL_PORT}）")
        return None

    service = os.environ.get("KERYO_MYSQL_SERVICE", "MySQL84")
    print(f"[MySQL] 端口 {MYSQL_PORT} 未监听，尝试启动 Windows 服务 {service} …")
    try:
        subprocess.run(
            ["sc.exe", "start", service],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            timeout=15,
            check=False,
        )
    except (OSError, subprocess.SubprocessError):
        pass
    if wait_port(MYSQL_PORT, 20):
        print(f"[MySQL] 已就绪（{MYSQL_PORT}）")
        return None

    mysqld = find_mysqld()
    defaults_file = configured_path("MYSQL_DEFAULTS_FILE", "KERYO_MYSQL_DEFAULTS_FILE")
    command = [str(mysqld)]
    if defaults_file:
        command.append(f"--defaults-file={defaults_file}")
    command.append("--console")
    print(f"[MySQL] Windows 服务未就绪，启动 mysqld；日志：{log_path('mysql-startup.log')}")
    process = launch(command, mysql_working_directory(defaults_file), log_path("mysql-startup.log"))
    if not wait_port(MYSQL_PORT, 30):
        raise RuntimeError(f"MySQL 启动失败，请查看 {log_path('mysql-startup.log')}")
    print(f"[MySQL] 已就绪（{MYSQL_PORT}）")
    return process


def start_backend(java: Path) -> Optional[subprocess.Popen[bytes]]:
    if port_open(BACKEND_PORT):
        if backend_api_ready(5):
            print(f"[后端] 已在运行且数据库检查通过（{BACKEND_PORT}）")
            return None
        raise RuntimeError(
            f"后端端口 {BACKEND_PORT} 已被占用，但数据库检查未通过；请先停止旧后端进程，再使用本次数据库口令启动。"
        )
    if not BACKEND_JAR.is_file():
        raise RuntimeError(f"未找到后端 JAR：{BACKEND_JAR}；请先执行 Maven 打包。")

    log_file = log_path("backend-startup.log")
    command = [str(java), "-jar", str(BACKEND_JAR), f"--server.port={BACKEND_PORT}"]
    print(f"[后端] 启动中（{BACKEND_PORT}）；日志：{log_file}")
    process = launch(command, API_DIR, log_file)
    if not wait_port(BACKEND_PORT, 60) or not backend_api_ready(60):
        raise RuntimeError(f"后端启动失败，请查看 {log_file}")
    print(f"[后端] 已就绪（{BACKEND_PORT}）")
    return process


def start_frontend(npm: Path) -> Optional[subprocess.Popen[bytes]]:
    if port_open(FRONTEND_PORT):
        print(f"[前端] 已在运行（{FRONTEND_PORT}）")
        return None
    if not (WEB_DIR / "package.json").is_file():
        raise RuntimeError(f"未找到前端目录：{WEB_DIR}")

    log_file = log_path("frontend-startup.log")
    if npm.suffix.lower() in {".cmd", ".bat"}:
        command = [os.environ.get("ComSpec", "cmd.exe"), "/d", "/c", "call", str(npm), "run", "dev"]
    else:
        command = [str(npm), "run", "dev"]
    print(f"[前端] 启动中（{FRONTEND_PORT}）；日志：{log_file}")
    process = launch(command, WEB_DIR, log_file)
    if not wait_port(FRONTEND_PORT, FRONTEND_STARTUP_TIMEOUT) or not http_ready(
        f"http://127.0.0.1:{FRONTEND_PORT}/index.html", 10
    ):
        raise RuntimeError(f"前端启动失败，请查看 {log_file}")
    print(f"[前端] 已就绪（{FRONTEND_PORT}）")
    return process


def main() -> int:
    print("=== 医护培训平台一键启动 ===")
    print(f"项目目录：{ROOT}")
    try:
        get_db_password()
        java = find_java17()
        npm = find_npm()
        print(f"[环境] JDK17：{java}")
        print(f"[环境] npm：{npm}")
        start_mysql()
        start_backend(java)
        start_frontend(npm)
    except KeyboardInterrupt:
        print("[启动取消] 未输入数据库口令，服务未启动。", file=sys.stderr)
        return 130
    except (OSError, RuntimeError, ValueError) as exc:
        print(f"[启动失败] {exc}", file=sys.stderr)
        return 1

    print("=== 全部检查完成 ===")
    print(f"前端：http://localhost:{FRONTEND_PORT}")
    print(f"后端：http://localhost:{BACKEND_PORT}/doc.html")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

