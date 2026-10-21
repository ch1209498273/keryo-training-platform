# Repeatable baseline

Run from the repository root in PowerShell:

```powershell
$env:BASELINE_JAVA_HOME = '<JDK17_HOME>'
$env:BASELINE_MAVEN_HOME = '<MAVEN_HOME>'
$env:BASELINE_DB_PORT = '3307'
$env:BASELINE_BACKEND_PORT = '9092'
$env:BASELINE_FRONTEND_PORT = '8001'
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\baseline\run_baseline.ps1
```

The script creates a fresh MySQL data directory under `%TEMP%`, uses the database name `exam_keryo_baseline`, imports `bootstrap.sql`, `schema.sql`, `seed.sql`, and `demo_questions.sql`, and removes only the processes bound to the selected baseline ports. It writes logs and a timestamped report under `%TEMP%` and `baseline-report/`; secrets, tokens, and SQL dumps are not written to the report. The public fixture intentionally contains synthetic demo records and does not require the excluded local `yf_boot_exam.sql` dump.

Before API Smoke, provide the local fixture credentials as `BASELINE_ADMIN_PASSWORD`, `BASELINE_MANAGER_PASSWORD`, and `BASELINE_STUDENT_PASSWORD`. Their values are intentionally kept outside the public repository; do not replace them with committed literals.

The public `seed.sql` contains no account rows, password hashes, or password history. The runner derives temporary legacy-compatible hashes in memory, imports them from the isolated runtime directory, and removes the generated SQL afterward; a successful login migrates the fixture password to bcrypt through the existing compatibility path.

Useful environment overrides:

- `BASELINE_MYSQL_HOME`, `BASELINE_MYSQL_CLIENT`, `BASELINE_MYSQLD`
- `BASELINE_JAVA`, `BASELINE_MAVEN`, `BASELINE_NPM`, `BASELINE_NODE`, `BASELINE_PYTHON`

`BASELINE_PYTHON` may point to a specific interpreter. If it is omitted, the
runner checks `python.exe` on `PATH`, then the standard user Conda locations
`%USERPROFILE%\.conda\envs\py\python.exe`, `anaconda3\python.exe`, and
`miniconda3\python.exe`. This is needed when the desktop app starts PowerShell
without the interactive Anaconda initialization hook.
- `BASELINE_DB_NAME` (must start with `exam_keryo_baseline`)
- `BASELINE_RUNTIME_DIR`, `BASELINE_MAVEN_REPO`, `BASELINE_COMMAND_TIMEOUT_MS`

On Windows, set `BASELINE_RUNTIME_DIR` to a local temporary path containing only ASCII characters and no spaces (for example `%TEMP%\keryo-baseline`); MySQL 8.4 may truncate non-ASCII datadir paths when launched from PowerShell.

`-SkipNpmInstall` is only for rerunning checks with an already-installed dependency tree. A clean acceptance run must omit it; the runner copies the frontend source to the runtime directory first so `npm ci` cannot disturb a user's running frontend. The install uses `--legacy-peer-deps` because the locked Vue 3.3.4 and Element Plus 2.14.x peer ranges are not accepted by npm 11, while the existing production build remains compatible. Python is required: missing Python now fails before database initialization because both active local unit tests and the Python API smoke must execute. Archived legacy suites are intentionally not required.

The old root-level legacy `test_*.py` scripts are formally archived under `archive/tests/legacy-python/` and are not executed by acceptance. The only API acceptance entrypoint is `scripts/baseline/smoke.py`, invoked by `run_baseline.ps1`, against the isolated baseline database. The current `tests/test_*.py` files are active, database-free tests of local startup/test tooling, not reactivated legacy API scripts. Archived legacy suites are reported as `ARCHIVED` and do not make the baseline fail.

## Active regression inventory (OPT-02)

| Suite | Baseline step | Standalone command | Boundary |
|---|---|---|---|
| Java `src/test/java/**/*Test.java` | `java-unit-tests` | PowerShell runner below | Automatically discovered; empty inventory, uncompiled class, nonzero Java exit or zero JUnit tests fails |
| Frontend `tests/*.test.ts` | `frontend-unit-tests` | In `yf-bev2-vue`: `npm test` | Node built-in test runner with TypeScript stripping; current verification uses Node 24.18.0 |
| Python `tests/test_*.py` | `python-unit-tests` | `& $env:BASELINE_PYTHON -B scripts/baseline/run_python_tests.py` | No MySQL/service startup; empty or skipped suites and failed tests return nonzero |
| Core API smoke | `api-smoke-python` | Use the full baseline runner | Isolated database only; does not certify medical content or manual UAT |
| Legacy Python/Playwright | `ARCHIVED` | Do not execute as acceptance | Historical assets, neither PASS nor missing active tests |

For a database-free Java regression, configure `BASELINE_JAVA_HOME`, `BASELINE_MAVEN_HOME`, and optionally `BASELINE_MAVEN_REPO` for this machine. From the repository root:

```powershell
$env:JAVA_HOME = $env:BASELINE_JAVA_HOME
$env:BASELINE_BACKEND_TARGET = Join-Path (Get-Location) '.baseline\regression-target'
$env:BASELINE_RUNTIME_DIR = Join-Path $env:BASELINE_BACKEND_TARGET 'runtime'
# If needed, set MAVEN_OPTS to the local user.home and Maven repository;
# BASELINE_MAVEN_REPO must identify that same repository for test-only jars.
& "$env:BASELINE_MAVEN_HOME\bin\mvn.cmd" -f yf-bev2-api/pom.xml "-Dproject.buildDir=$env:BASELINE_BACKEND_TARGET" -o package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Backend build failed' }
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/baseline/run_java_tests.ps1
if ($LASTEXITCODE -ne 0) { throw 'Java regression failed' }
```

`package -DskipTests` compiles application/test sources and packages dependencies; **it does not run tests**. Only the following runner result with `JAVA_TEST_RESULT classes=… tests=…` is test execution evidence. `-ListOnly` prints the discovered class inventory without building or running it. Keep new JUnit4 source classes under `src/test/java`, named `*Test.java`; other test frameworks/conventions require explicit runner support, not a silent PASS.

`bash scripts/run_unit_tests.sh` is now a Windows/Git Bash compatibility wrapper for the same PowerShell runner, not a second class list. It uses `BASELINE_BACKEND_TARGET` or `yf-bev2-api/target` and requires a fresh build as above; it no longer invokes a personal WorkBuddy Python installation. These unit-only commands do not substitute for the clean database/build/start/API baseline or manual UAT.

The legacy frontend `engines.node` lower bound describes the application, not support for these native TypeScript tests. Use the verified Node 24.18.0 runtime or a compatible runtime supporting `--experimental-strip-types` and the test glob. `node-test-runtime` checks the flag before database initialization; unsupported Node fails immediately. No Node/framework upgrade is performed automatically.

