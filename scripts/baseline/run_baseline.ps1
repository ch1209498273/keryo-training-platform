[CmdletBinding()]
param([switch]$KeepRuntime, [switch]$SkipNpmInstall, [switch]$SkipLegacyTests)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$apiDir = Join-Path $repo 'yf-bev2-api'
$webDir = Join-Path $repo 'yf-bev2-vue'
$frontendDir = $webDir
$runtime = if ($env:BASELINE_RUNTIME_DIR) { $env:BASELINE_RUNTIME_DIR } else { Join-Path $env:TEMP 'keryo-baseline' }
$reportDir = Join-Path $repo 'baseline-report'
$dbName = if ($env:BASELINE_DB_NAME) { $env:BASELINE_DB_NAME } else { 'exam_keryo_baseline' }
$dbPort = if ($env:BASELINE_DB_PORT) { [int]$env:BASELINE_DB_PORT } else { 3307 }
$backendPort = if ($env:BASELINE_BACKEND_PORT) { [int]$env:BASELINE_BACKEND_PORT } else { 9092 }
$frontendPort = if ($env:BASELINE_FRONTEND_PORT) { [int]$env:BASELINE_FRONTEND_PORT } else { 8001 }
$dbUser = if ($env:BASELINE_DB_USER) { $env:BASELINE_DB_USER } else { 'root' }
$dbPassword = if ($null -ne $env:BASELINE_DB_PASSWORD) { $env:BASELINE_DB_PASSWORD } else { '' }
$started = [System.Collections.Generic.List[System.Diagnostics.Process]]::new()
$results = [System.Collections.Generic.List[object]]::new()
$startedAt = Get-Date

New-Item -ItemType Directory -Force -Path $runtime, $reportDir | Out-Null
$logDir = Join-Path $runtime 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

function Add-Result([string]$Name, [string]$Status, [string]$Detail = '') {
  $results.Add([pscustomobject]@{ Name = $Name; Status = $Status; Detail = $Detail })
  Write-Host "[$Status] $Name $Detail"
}

function Find-Tool([string]$Name, [string]$EnvName, [string[]]$Candidates = @()) {
  $configured = [Environment]::GetEnvironmentVariable($EnvName)
  if ($configured) { return (Resolve-Path $configured).Path }
  foreach ($candidate in $Candidates) {
    if ($candidate -and (Test-Path $candidate)) { return (Resolve-Path $candidate).Path }
  }
  $command = Get-Command $Name -ErrorAction SilentlyContinue
  if ($command) { return $command.Source }
  throw "Tool not found: $Name. Set $EnvName."
}

function Test-PythonExecutable([string]$Candidate) {
  if (-not $Candidate -or -not (Test-Path -LiteralPath $Candidate)) { return $false }
  try {
    $null = & $Candidate '--version' 1>$null 2>$null
    return ($LASTEXITCODE -eq 0)
  } catch {
    return $false
  }
}

function Find-Python {
  if ($env:BASELINE_PYTHON) {
    if (-not (Test-Path -LiteralPath $env:BASELINE_PYTHON)) {
      throw "Configured BASELINE_PYTHON not found: $env:BASELINE_PYTHON"
    }
    $configuredPython = (Resolve-Path -LiteralPath $env:BASELINE_PYTHON).Path
    if (-not (Test-PythonExecutable $configuredPython)) {
      throw "Configured BASELINE_PYTHON is not executable: $env:BASELINE_PYTHON"
    }
    return $configuredPython
  }

  $command = Get-Command python.exe -ErrorAction SilentlyContinue
  $candidates = @()
  if ($command) { $candidates += $command.Source }

  # PowerShell launched by the desktop app may not inherit the user's
  # Anaconda PATH. Keep the fallback narrow and deterministic.
  $candidates += @(
    if ($env:CONDA_PREFIX) { Join-Path $env:CONDA_PREFIX 'python.exe' }
    (Join-Path $env:USERPROFILE '.conda\envs\py\python.exe'),
    (Join-Path $env:USERPROFILE 'anaconda3\python.exe'),
    (Join-Path $env:USERPROFILE 'miniconda3\python.exe'),
    'C:\ProgramData\Anaconda3\python.exe',
    'C:\ProgramData\miniconda3\python.exe'
  )
  foreach ($candidate in $candidates) {
    if (Test-PythonExecutable $candidate) { return (Resolve-Path -LiteralPath $candidate).Path }
  }
  return $null
}

function Invoke-Checked([string]$Name, [string]$File, [string[]]$Arguments, [string]$WorkDir) {
  $sw = [Diagnostics.Stopwatch]::StartNew()
  $logFile = Join-Path $logDir ($Name + '.log')
  $errorFile = Join-Path $logDir ($Name + '.err.log')
  $argumentList = @($Arguments | ForEach-Object {
    if ([string]$_ -match '\s') { '"' + ([string]$_).Replace('"', '\"') + '"' } else { [string]$_ }
  })
  $psi = [Diagnostics.ProcessStartInfo]::new()
  $psi.FileName = if ($File -match '(?i)\.cmd$') { $env:COMSPEC } else { $File }
  $psi.Arguments = if ($File -match '(?i)\.cmd$') {
    "/d /c call `"$File`" $($argumentList -join ' ')"
  } else { $argumentList -join ' ' }
  $psi.WorkingDirectory = $WorkDir
  $psi.UseShellExecute = $false
  $psi.CreateNoWindow = $true
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError = $true
  $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
  $psi.StandardErrorEncoding = [Text.Encoding]::UTF8
  $process = [Diagnostics.Process]::new()
  $process.StartInfo = $psi
  $process.Start() | Out-Null
  $stdout = $process.StandardOutput.ReadToEndAsync()
  $stderr = $process.StandardError.ReadToEndAsync()
  $timeoutMs = if ($env:BASELINE_COMMAND_TIMEOUT_MS) { [int]$env:BASELINE_COMMAND_TIMEOUT_MS } else { 600000 }
  if (-not $process.WaitForExit($timeoutMs)) {
    try { & taskkill.exe /PID $process.Id /T /F 1>$null 2>$null } catch { }
    Add-Result $Name 'FAIL' "timeout=${timeoutMs}ms"
    throw "$Name timed out"
  }
  $process.Refresh()
  $code = $process.ExitCode
  $sw.Stop()
  $stdout.Result | Set-Content -LiteralPath $logFile -Encoding UTF8
  $stderr.Result | Set-Content -LiteralPath $errorFile -Encoding UTF8
  if (Test-Path $logFile) { Get-Content -LiteralPath $logFile -Tail 20 | ForEach-Object { Write-Host $_ } }
  if (Test-Path $errorFile) { Get-Content -LiteralPath $errorFile -Tail 20 | ForEach-Object { Write-Host $_ } }
  if ($code -ne 0) { Add-Result $Name 'FAIL' "exit=$code, log=$logFile"; throw "$Name failed" }
  Add-Result $Name 'PASS' ("{0:n1}s" -f $sw.Elapsed.TotalSeconds)
}

function Start-Child([string]$Name, [string]$File, [string[]]$Arguments, [string]$WorkDir, [hashtable]$Environment = @{}) {
  $outFile = Join-Path $logDir ($Name + '.out.log')
  $errFile = Join-Path $logDir ($Name + '.err.log')
  $argumentList = @($Arguments | ForEach-Object {
    if ([string]$_ -match '\s') { '"' + ([string]$_).Replace('"', '\"') + '"' } else { [string]$_ }
  })
  $psi = [Diagnostics.ProcessStartInfo]::new()
  $psi.FileName = if ($File -match '(?i)\.cmd$') { $env:COMSPEC } else { $File }
  $psi.Arguments = if ($File -match '(?i)\.cmd$') {
    "/d /c call `"$File`" $($argumentList -join ' ')"
  } else { $argumentList -join ' ' }
  $psi.WorkingDirectory = $WorkDir
  $psi.UseShellExecute = $false
  $psi.CreateNoWindow = $true
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError = $true
  $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
  $psi.StandardErrorEncoding = [Text.Encoding]::UTF8
  $process = [Diagnostics.Process]::new()
  $process.StartInfo = $psi
  $process.Start() | Out-Null
  # Drain redirected pipes while long-running children are alive. Waiting
  # until Exited lets verbose Spring/MySQL output fill the pipe and block the
  # child process, which can make an otherwise healthy API appear hung.
  $stdoutTask = $process.StandardOutput.ReadToEndAsync()
  $stderrTask = $process.StandardError.ReadToEndAsync()
  $process.add_Exited({
    try { $stdoutTask.GetAwaiter().GetResult() | Set-Content -LiteralPath $outFile -Encoding UTF8 } catch { }
    try { $stderrTask.GetAwaiter().GetResult() | Set-Content -LiteralPath $errFile -Encoding UTF8 } catch { }
  })
  $started.Add($process)
  return $process
}

function Invoke-Sql([string]$Name, [string]$SqlFile, [switch]$WithDatabase, [string]$InitCommand = '') {
  $safeFile = Join-Path $runtime ($Name + '.sql')
  Copy-Item -LiteralPath $SqlFile -Destination $safeFile -Force
  $args = @('--protocol=TCP', '--host=127.0.0.1', "--port=$dbPort", "--user=$dbUser", '--batch')
  if ($dbPassword) { $args += "--password=$dbPassword" }
  if ($InitCommand) { $args += "--init-command=$InitCommand" }
  if ($WithDatabase) { $args += $dbName }
  $args += @('--execute', "source $safeFile")
  Invoke-Checked $Name $mysql $args $runtime
}

function Get-RequiredBaselineCredential([string]$Name) {
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) {
    throw "Missing required baseline credential: $Name"
  }
  return $value
}

function Get-Md5Hex([string]$Value) {
  $md5 = [Security.Cryptography.MD5]::Create()
  try {
    $bytes = [Text.Encoding]::UTF8.GetBytes($Value)
    $hash = $md5.ComputeHash($bytes)
    return (($hash | ForEach-Object { $_.ToString('x2') }) -join '')
  } finally {
    $md5.Dispose()
  }
}

function Initialize-BaselineCredentials {
  # The public SQL fixture deliberately contains no authentication material.
  # Generate legacy-compatible hashes only in the isolated runtime directory;
  # the application migrates them to bcrypt after a successful login.
  $adminPassword = Get-RequiredBaselineCredential 'BASELINE_ADMIN_PASSWORD'
  $managerPassword = Get-RequiredBaselineCredential 'BASELINE_MANAGER_PASSWORD'
  $studentPassword = Get-RequiredBaselineCredential 'BASELINE_STUDENT_PASSWORD'
  $adminHash = Get-Md5Hex ((Get-Md5Hex $adminPassword) + 'BaseA1')
  $managerHash = Get-Md5Hex ((Get-Md5Hex $managerPassword) + 'BaseM1')
  $studentHash = Get-Md5Hex ((Get-Md5Hex $studentPassword) + 'BaseS1')

  $generatedFile = Join-Path $runtime 'generated-baseline-credentials.sql'
  $sql = @"
SET NAMES utf8mb4;

INSERT INTO el_sys_role
  (id, role_name, data_scope, role_level, remark, create_time, create_by, update_time, update_by)
VALUES
  ('keryo_director', '培训管理员', 4, 100, 'baseline manager', NOW(), 'baseline', NOW(), 'baseline'),
  ('keryo_nurse', '学员', 1, 40, 'baseline learner', NOW(), 'baseline', NOW(), 'baseline'),
  ('keryo_team_leader', '小组长', 1, 30, 'baseline team leader', NOW(), 'baseline', NOW(), 'baseline')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), data_scope=VALUES(data_scope), role_level=VALUES(role_level);

INSERT INTO el_sys_depart
  (id, dept_type, parent_id, dept_name, dept_code, area_code, dept_level, sort, create_time, update_time, create_by, update_by)
VALUES
  ('baseline_dept', 1, '0', 'Baseline Center', 'BASELINE_DEPT', 'BASELINE_AREA', 1, 1, NOW(), NOW(), 'baseline', 'baseline')
ON DUPLICATE KEY UPDATE dept_name=VALUES(dept_name), area_code=VALUES(area_code);

INSERT INTO el_sys_user
  (id, user_name, real_name, avatar, password, salt, state, id_card, mobile, email,
   dept_code, area_code, last_password_time, create_time, create_by, update_time, update_by)
VALUES
  ('1000000000000000001', 'admin', 'Baseline Administrator', '', '$adminHash', 'BaseA1', 0, '', '', NULL,
   'BASELINE_DEPT', 'BASELINE_AREA', NOW(), NOW(), 'baseline', NOW(), 'baseline'),
  ('1914226980618383361', 'student', 'Baseline Learner', '', '$studentHash', 'BaseS1', 0, '', '', NULL,
   NULL, NULL, NOW(), NOW(), 'baseline', NOW(), 'baseline'),
  ('baseline_manager', 'nurse_head', 'Baseline Training Manager', '', '$managerHash', 'BaseM1', 0, '', '', NULL,
   'BASELINE_DEPT', 'BASELINE_AREA', NOW(), NOW(), 'baseline', NOW(), 'baseline'),
  ('baseline_team_leader', 'team_lead1', 'Baseline Team Leader', '', '$studentHash', 'BaseS1', 0, '', '', NULL,
   'BASELINE_DEPT', 'BASELINE_AREA', NOW(), NOW(), 'baseline', NOW(), 'baseline')
ON DUPLICATE KEY UPDATE
  real_name=VALUES(real_name), avatar=VALUES(avatar), password=VALUES(password), salt=VALUES(salt),
  state=VALUES(state), dept_code=VALUES(dept_code), area_code=VALUES(area_code),
  last_password_time=VALUES(last_password_time), update_time=NOW(), update_by='baseline';

DELETE ur FROM el_sys_user_role ur
JOIN el_sys_user u ON u.id=ur.user_id
WHERE u.user_name IN ('admin','nurse_head','student','team_lead1');

INSERT INTO el_sys_user_role (id, user_id, role_id)
VALUES
  ('baseline-admin-role', '1000000000000000001', 'admin'),
  ('baseline-manager-role', 'baseline_manager', 'keryo_director'),
  ('baseline-student-role', '1914226980618383361', 'keryo_nurse'),
  ('baseline-team-leader-role', 'baseline_team_leader', 'keryo_team_leader')
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), role_id=VALUES(role_id);

INSERT INTO el_sys_user_password_history (id, user_id, password, create_time)
SELECT MD5(CONCAT('baseline-password-', u.id, '-', u.password)), u.id, u.password, u.last_password_time
FROM el_sys_user u
WHERE u.user_name IN ('admin', 'nurse_head', 'student', 'team_lead1')
  AND NOT EXISTS (
    SELECT 1 FROM el_sys_user_password_history h
    WHERE h.user_id = u.id AND h.password = u.password
  );
"@
  try {
    $sql | Set-Content -LiteralPath $generatedFile -Encoding UTF8
    Invoke-Sql 'baseline-credentials' $generatedFile -WithDatabase
  } finally {
    Remove-Item -LiteralPath $generatedFile -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $runtime 'baseline-credentials.sql') -Force -ErrorAction SilentlyContinue
  }
}

function Wait-Database {
  for ($i = 0; $i -lt 60; $i++) {
    & $mysql '--protocol=TCP' '--host=127.0.0.1' "--port=$dbPort" "--user=$dbUser" '--execute=SELECT 1' 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { return }
    Start-Sleep -Milliseconds 500
  }
  throw 'MySQL did not become ready'
}

function Wait-Http([string]$Url, [string]$Name) {
  for ($i = 0; $i -lt 60; $i++) {
    try {
      $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 3
      if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) { Add-Result $Name 'PASS' $Url; return }
    } catch { }
    Start-Sleep -Milliseconds 500
  }
  throw "$Name did not become ready: $Url"
}

function Get-ListeningLines([int]$Port) {
  return @(netstat -ano -p tcp | Select-String (":$Port\s") | Where-Object { $_.ToString() -match '\sLISTENING\s' })
}

function Get-ListeningPid([int]$Port) {
  foreach ($line in (Get-ListeningLines $Port)) {
    if ($line.ToString() -match '\s(?<pid>\d+)\s*$') { return [int]$Matches['pid'] }
  }
  return $null
}

function Assert-PortFree([int]$Port) {
  $listeners = Get-ListeningLines $Port
  if ($listeners.Count -gt 0) {
    $detail = ($listeners | ForEach-Object { $_.ToString().Trim() }) -join '; '
    throw "Baseline port $Port is already in use: $detail"
  }
}

function Save-FailureDiagnostics {
  $diagnosticDir = Join-Path $logDir 'failure-diagnostics'
  New-Item -ItemType Directory -Force -Path $diagnosticDir | Out-Null
  try { netstat -ano -p tcp | Set-Content -LiteralPath (Join-Path $diagnosticDir 'netstat.txt') -Encoding UTF8 } catch { }

  try {
    $mysqlArgs = @('--protocol=TCP', '--host=127.0.0.1', "--port=$dbPort", "--user=$dbUser", '--batch', '--raw')
    if ($dbPassword) { $mysqlArgs += "--password=$dbPassword" }
    $mysqlArgs += @('--execute', 'SHOW FULL PROCESSLIST')
    & $mysql @mysqlArgs 1> (Join-Path $diagnosticDir 'mysql-processlist.txt') 2>&1
  } catch { }

  $backendPid = Get-ListeningPid $backendPort
  if ($backendPid) {
    $jstack = Join-Path (Split-Path $java -Parent) 'jstack.exe'
    if (Test-Path -LiteralPath $jstack) {
      try { & $jstack $backendPid 1> (Join-Path $diagnosticDir 'backend-jstack.txt') 2>&1 } catch { }
    }
    try { & tasklist.exe /FI "PID eq $backendPid" 1> (Join-Path $diagnosticDir 'backend-process.txt') 2>&1 } catch { }
  }
}

$mysqlHome = $env:BASELINE_MYSQL_HOME
$mysqlCandidates = @('C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe', 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe')
$mysqldCandidates = @('C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe', 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe')
if ($mysqlHome) {
  $mysqlCandidates = @(Join-Path $mysqlHome 'bin\mysql.exe') + $mysqlCandidates
  $mysqldCandidates = @(Join-Path $mysqlHome 'bin\mysqld.exe') + $mysqldCandidates
}
$mysql = Find-Tool 'mysql.exe' 'BASELINE_MYSQL_CLIENT' $mysqlCandidates
$mysqld = Find-Tool 'mysqld.exe' 'BASELINE_MYSQLD' $mysqldCandidates
$javaCandidates = @()
$mvnCandidates = @()
if ($env:BASELINE_JAVA_HOME) { $javaCandidates = @(Join-Path $env:BASELINE_JAVA_HOME 'bin\java.exe') }
if ($env:BASELINE_MAVEN_HOME) { $mvnCandidates = @(Join-Path $env:BASELINE_MAVEN_HOME 'bin\mvn.cmd') }
$java = Find-Tool 'java.exe' 'BASELINE_JAVA' $javaCandidates
$mvn = Find-Tool 'mvn.cmd' 'BASELINE_MAVEN' $mvnCandidates
$npm = Find-Tool 'npm.cmd' 'BASELINE_NPM'
$node = Find-Tool 'node.exe' 'BASELINE_NODE'
$python = Find-Python

try {
  $javaVersionFile = Join-Path $runtime 'java-version.txt'
  & cmd.exe /c "`"$java`" -version > `"$javaVersionFile`" 2>&1"
  $javaVersion = (Get-Content -LiteralPath $javaVersionFile | Select-Object -First 1).Trim()
  if ($javaVersion -notmatch '17') { throw "JDK 17 required, got $javaVersion" }
  Add-Result 'environment' 'PASS' $javaVersion
  Assert-PortFree $dbPort
  Assert-PortFree $backendPort
  Assert-PortFree $frontendPort
  Add-Result 'ports' 'PASS' "$dbPort/$backendPort/$frontendPort are free"

  if (-not $python) { throw 'Python is required for active launcher/runner tests and API smoke. Set BASELINE_PYTHON.' }
  Invoke-Checked 'node-test-runtime' $node @('--experimental-strip-types', '--eval', 'console.log(process.version)') $repo
  Invoke-Checked 'python-unit-tests' $python @('-B', (Join-Path $repo 'scripts\baseline\run_python_tests.py')) $repo

  $dataDir = Join-Path $runtime 'data'
  if (-not (Test-Path $dataDir)) {
    $mysqlInitLog = Join-Path $logDir 'mysql-init.log'
    & cmd.exe /c "`"$mysqld`" --initialize-insecure --datadir=`"$dataDir`" > `"$mysqlInitLog`" 2>&1"
    if ($LASTEXITCODE -ne 0) { throw 'MySQL initialization failed' }
  }
  # Pass the datadir as a separate argument. MySQL on Windows can truncate an
  # equals-form argument when the path contains spaces or non-ASCII characters,
  # even though initialization accepts the same spelling.
  Start-Child 'mysql' $mysqld @('--no-defaults', '--console', '--datadir', $dataDir, "--port=$dbPort", '--bind-address=127.0.0.1', '--mysqlx=OFF') $runtime | Out-Null
  Wait-Database
  Add-Result 'mysql_start' 'PASS' "127.0.0.1:$dbPort"

  if ($dbName -notmatch '^exam_keryo_baseline([_a-zA-Z0-9-]*)$') { throw 'Refusing non-baseline database name' }
  $resetSql = "DROP DATABASE IF EXISTS $dbName; CREATE DATABASE $dbName CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
  & $mysql '--protocol=TCP' '--host=127.0.0.1' "--port=$dbPort" "--user=$dbUser" '--execute' $resetSql 2>&1 | Tee-Object (Join-Path $logDir 'database-reset.log')
  if ($LASTEXITCODE -ne 0) { throw 'Database reset failed' }
  Invoke-Sql 'bootstrap-schema' (Join-Path $repo 'scripts\baseline\bootstrap.sql') -WithDatabase
  Invoke-Sql 'baseline-schema' (Join-Path $repo 'scripts\baseline\schema.sql') -WithDatabase
  Initialize-BaselineCredentials
  Invoke-Sql 'baseline-seed' (Join-Path $repo 'scripts\baseline\seed.sql') -WithDatabase
  Invoke-Sql 'baseline-question-clean' (Join-Path $repo 'scripts\baseline\clean_demo_questions.sql') -WithDatabase
  Invoke-Sql 'baseline-questions' (Join-Path $repo 'scripts\baseline\demo_questions.sql') -WithDatabase -InitCommand "SET collation_connection='utf8mb4_general_ci'"
  Invoke-Sql 'baseline-verify' (Join-Path $repo 'scripts\baseline\verify.sql') -WithDatabase

  $targetRoot = Join-Path $repo '.baseline'
  New-Item -ItemType Directory -Force -Path $targetRoot | Out-Null
  $target = Join-Path $targetRoot ('backend-target-' + (Get-Date -Format 'yyyyMMdd-HHmmssfff'))
  if ($env:BASELINE_JAVA_HOME) {
    $env:JAVA_HOME = (Resolve-Path $env:BASELINE_JAVA_HOME).Path
    $env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
  }
  if (-not $env:MAVEN_OPTS) {
    $mavenRepo = if ($env:BASELINE_MAVEN_REPO) { $env:BASELINE_MAVEN_REPO } else { Join-Path $env:USERPROFILE '.m2\repository' }
    $env:MAVEN_OPTS = "-Duser.home=$env:USERPROFILE -Dmaven.repo.local=$mavenRepo"
  }
  Invoke-Checked 'backend-package' $mvn @("-Dproject.buildDir=$target", '-Dmaven.compiler.fork=true', '-Dmaven.compiler.useIncrementalCompilation=false', '-o', 'package', '-DskipTests') $apiDir
  if (-not $SkipNpmInstall) {
    # Keep a clean npm install isolated from a user's running frontend. The
    # working tree node_modules may contain locked esbuild binaries, while
    # the baseline must be repeatable without stopping unrelated services.
    $frontendDir = Join-Path $runtime 'frontend'
    New-Item -ItemType Directory -Force -Path $frontendDir | Out-Null
    & robocopy.exe $webDir $frontendDir /E /XD node_modules dist dist-pro .vite /NFL /NDL /NJH /NJS /NP | Out-Null
    $copyExit = $LASTEXITCODE
    if ($copyExit -gt 7) { throw "frontend source copy failed with robocopy exit=$copyExit" }
    Add-Result 'frontend-copy' 'PASS' $frontendDir
    Invoke-Checked 'frontend-install' $npm @('ci', '--ignore-scripts', '--legacy-peer-deps') $frontendDir
  }
  Invoke-Checked 'frontend-typecheck' $npm @('run', 'ts:check') $frontendDir
  Invoke-Checked 'frontend-unit-tests' $node @('--experimental-strip-types', '--test', 'tests/*.test.ts') $frontendDir
  Invoke-Checked 'frontend-build' $npm @('run', 'build:pro') $frontendDir

  $jar = Join-Path $target 'yf-bev2-api.jar'
  $env:BASELINE_BACKEND_TARGET = $target
  $powershell = Join-Path $PSHOME 'powershell.exe'
  Invoke-Checked 'java-unit-tests' $powershell @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $repo 'scripts\baseline\run_java_tests.ps1')) $repo
  $env:BASELINE_DB_PORT = [string]$dbPort
  $env:BASELINE_DB_NAME = $dbName
  $env:BASELINE_BACKEND_PORT = [string]$backendPort
  $env:BASELINE_LOG_DIR = $logDir
  $env:BASELINE_API_URL = "http://127.0.0.1:$backendPort"
  $env:BASELINE_DB_HOST = '127.0.0.1'
  $env:BASELINE_DB_PORT = [string]$dbPort
  $env:BASELINE_DB_NAME = $dbName
  $env:BASELINE_DB_USER = $dbUser
  $env:BASELINE_DB_PASSWORD = $dbPassword
  Start-Child 'backend' $java @(
    '-Dspring.profiles.active=dev,baseline',
    "-Dspring.datasource.url=jdbc:mysql://127.0.0.1:$dbPort/$dbName",
    "-Dspring.datasource.username=$dbUser",
    "-Dspring.datasource.password=$dbPassword",
    '-jar', $jar,
    "--server.port=$backendPort",
    '--logging.level.root=WARN'
  ) $repo | Out-Null
  Wait-Http "http://127.0.0.1:$backendPort/api/common/captcha/gen?key=baseline-health" 'backend_health'
  Start-Child 'frontend' $npm @('run', 'dev', '--', '--host', '127.0.0.1', '--port', $frontendPort) $frontendDir | Out-Null
  Wait-Http "http://127.0.0.1:$frontendPort/index.html" 'frontend_health'
  Invoke-Checked 'baseline-db-health' $mysql @('--protocol=TCP', '--host=127.0.0.1', "--port=$dbPort", "--user=$dbUser", '--execute=SELECT 1') $runtime

  if ($python) {
    $pythonVersionLine = (& $python --version 2>&1 | Select-Object -First 1)
    $pythonVersion = if ($null -eq $pythonVersionLine) { 'unknown' } else { $pythonVersionLine.ToString().Trim() }
    Add-Result 'python-environment' 'PASS' "$pythonVersion ($python)"
    Invoke-Checked 'api-smoke-python' $python @((Join-Path $repo 'scripts\baseline\smoke.py')) $repo
  }
  else {
    if (-not $node) { throw 'Neither Python nor Node was found for API smoke' }
    Invoke-Checked 'api-smoke-node' $node @((Join-Path $repo 'scripts\baseline\smoke.js')) $repo
  }
  if (-not $python) { Add-Result 'python-test-entrypoint' 'SKIP' 'Python interpreter was not found; Node smoke fallback was executed' }
  Add-Result 'legacy-tests' 'ARCHIVED' 'Legacy Python and Playwright suites are archived; baseline smoke is the only acceptance test entrypoint'
} catch {
  Save-FailureDiagnostics
  $diagnostic = @(
    (($_ | Out-String).Trim()),
    ("Position: " + $_.InvocationInfo.PositionMessage),
    ("Stack: " + $_.ScriptStackTrace)
  )
  Set-Content -LiteralPath (Join-Path $reportDir 'last-baseline-error.txt') -Value $diagnostic -Encoding UTF8
  $diagnostic | ForEach-Object { Write-Host $_ }
  Add-Result 'baseline' 'FAIL' $_.Exception.Message
} finally {
  foreach ($process in $started) {
    try {
      # The .NET process object can report Exited before a child tree has
      # released its listener. Always target only the PID started by this
      # run, then use an exact-PID fallback if taskkill did not finish it.
      $processId = $process.Id
      try { & taskkill.exe /PID $processId /T /F 1>$null 2>$null } catch { }
      Start-Sleep -Milliseconds 200
      if (Get-Process -Id $processId -ErrorAction SilentlyContinue) {
        try { Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue } catch { }
      }
      try { $process.WaitForExit(5000) | Out-Null } catch { }
    } catch { }
  }
  foreach ($port in @($dbPort, $backendPort, $frontendPort)) {
    # The launcher may leave a service child behind. Only clean a listener
    # that appeared after this run started and has the expected executable;
    # this avoids killing an unrelated process that reused the port.
    $remaining = Get-ListeningPid $port
    if ($remaining) {
      $candidate = Get-Process -Id $remaining -ErrorAction SilentlyContinue
      $expectedNames = if ($port -eq $dbPort) { @('mysqld') } elseif ($port -eq $backendPort) { @('java', 'javaw') } else { @('node') }
      $candidateStartedDuringRun = $false
      if ($candidate) {
        try { $candidateStartedDuringRun = $candidate.StartTime -ge $startedAt } catch { }
      }
      if ($candidate -and ($expectedNames -contains $candidate.ProcessName) -and $candidateStartedDuringRun) {
        try { & taskkill.exe /PID $remaining /T /F 1>$null 2>$null } catch { }
        try { Stop-Process -Id $remaining -Force -ErrorAction SilentlyContinue } catch { }
        for ($attempt = 0; $attempt -lt 10; $attempt++) {
          Start-Sleep -Milliseconds 300
          if (-not (Get-Process -Id $remaining -ErrorAction SilentlyContinue)) { break }
          try { Stop-Process -Id $remaining -Force -ErrorAction SilentlyContinue } catch { }
        }
      }
      $remaining = Get-ListeningPid $port
      if ($remaining) {
        Write-Host "WARNING: baseline listener remains on port $port (PID $remaining)"
        Add-Result 'cleanup' 'FAIL' "listener remains on port $port (PID $remaining)"
      }
    }
  }
  $duration = (Get-Date) - $startedAt
  $status = if ($results.Status -contains 'FAIL' -or $results.Status -contains 'SKIP') { 'FAIL' } else { 'PASS' }
  $report = Join-Path $reportDir ('baseline-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.md')
  $lines = @("# Baseline $status", '', "- Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')", "- Database: $dbName @ 127.0.0.1:$dbPort", "- Duration: $([math]::Round($duration.TotalSeconds, 1)) seconds", '', '| Item | Status | Detail |', '|---|---|---|')
  foreach ($result in $results) { $lines += "| $($result.Name) | $($result.Status) | $($result.Detail -replace '\|','/') |" }
  $lines += '', "Logs: $logDir"
  Set-Content -LiteralPath $report -Value $lines -Encoding UTF8
  Write-Host "BASELINE_$status"
  Write-Host "Report: $report"
  if ($status -ne 'PASS') { exit 1 }
}

