@echo off
setlocal EnableExtensions
chcp 65001 >nul 2>&1
title Keryo Training Launcher

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "PYTHON="
set "PYTHONUTF8=1"

if not exist "%ROOT%\start_all.py" (
    echo [ERROR] start_all.py was not found next to this file.
    pause
    exit /b 1
)

where py.exe >nul 2>&1
if not errorlevel 1 (
    py -3 -c "import sys; raise SystemExit(0 if sys.version_info >= (3, 8) else 1)" >nul 2>&1
    if not errorlevel 1 set "PYTHON=py -3"
)

if not defined PYTHON (
    for %%P in (
        "%USERPROFILE%\.conda\envs\py\python.exe"
        "%USERPROFILE%\anaconda3\python.exe"
        "%USERPROFILE%\miniconda3\python.exe"
        "C:\ProgramData\Anaconda3\python.exe"
        "C:\ProgramData\miniconda3\python.exe"
    ) do (
        if not defined PYTHON if exist %%~P (
            "%%~P" --version >nul 2>&1
            if not errorlevel 1 set "PYTHON=%%~P"
        )
    )
)

if not defined PYTHON (
    where python.exe >nul 2>&1
    if not errorlevel 1 (
        python.exe --version >nul 2>&1
        if not errorlevel 1 set "PYTHON=python.exe"
    )
)

if not defined PYTHON (
    echo [ERROR] Python 3.8 or newer was not found.
    echo Install or activate Python/Anaconda, then run this file again.
    pause
    exit /b 1
)

echo Starting Keryo Training services...
echo If DB_PASSWORD is not set, the launcher will ask for it securely.
echo The window will stay open and show the exact failure if startup cannot continue.
echo.

pushd "%ROOT%"
%PYTHON% "%ROOT%\start_all.py"
set "EXIT_CODE=%ERRORLEVEL%"
popd

if not "%EXIT_CODE%"=="0" echo [ERROR] Startup failed. Review the message and logs under logs\.
echo.
pause
exit /b %EXIT_CODE%

