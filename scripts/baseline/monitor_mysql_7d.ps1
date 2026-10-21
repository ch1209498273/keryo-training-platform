[CmdletBinding()]
param(
    [int]$DurationHours = 168,
    [int]$IntervalSeconds = 60,
    [int]$Port = 3306,
    [string]$ServiceName = 'MySQL84',
    [string]$OutputPath = ''
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $OutputPath = Join-Path (Join-Path $PSScriptRoot '..\..\baseline-report') "mysql-stability-$stamp.csv"
}

$mysqlCandidates = @()
if (-not [string]::IsNullOrWhiteSpace($env:BASELINE_MYSQL_CLIENT)) {
    $mysqlCandidates += $env:BASELINE_MYSQL_CLIENT
}
if (-not [string]::IsNullOrWhiteSpace($env:BASELINE_MYSQL_HOME)) {
    $mysqlCandidates += Join-Path $env:BASELINE_MYSQL_HOME 'bin\mysql.exe'
}
if (-not [string]::IsNullOrWhiteSpace($env:MYSQL_HOME)) {
    $mysqlCandidates += Join-Path $env:MYSQL_HOME 'bin\mysql.exe'
}
$pathMysql = Get-Command mysql.exe -ErrorAction SilentlyContinue
if ($pathMysql) {
    $mysqlCandidates += $pathMysql.Source
}
$mysqlCandidates += @(
    'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe',
    'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
)
$mysql = $mysqlCandidates | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } | Select-Object -First 1
$configCandidates = @(
    (Join-Path $PSScriptRoot '..\..\yf-bev2-api\src\main\resources\application-local.yml'),
    (Join-Path $PSScriptRoot '..\..\yf-bev2-api\src\main\resources\application-dev.yml'),
    (Join-Path $PSScriptRoot '..\..\yf-bev2-api\src\main\resources\application-baseline.yml')
)

if (-not (Test-Path -LiteralPath $mysql -PathType Leaf)) {
    throw 'mysql.exe not found. Set BASELINE_MYSQL_CLIENT, BASELINE_MYSQL_HOME, MYSQL_HOME, or add mysql.exe to PATH.'
}

$dbPassword = if (-not [string]::IsNullOrWhiteSpace($env:BASELINE_DB_PASSWORD)) {
    $env:BASELINE_DB_PASSWORD
} elseif (-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $env:DB_PASSWORD
} else {
    $null
}
foreach ($config in $configCandidates) {
    if (-not $dbPassword -and (Test-Path -LiteralPath $config -PathType Leaf)) {
        $match = Select-String -LiteralPath $config -Pattern '^\s*password\s*:\s*[''\"]?([^''\"\s#]+)' | Select-Object -First 1
        if ($match) {
            $candidate = $match.Matches[0].Groups[1].Value
            if ($candidate -notmatch '^\$\{[^}]+\}$') {
                $dbPassword = $candidate
            }
        }
    }
}
if (-not $dbPassword) {
    throw 'No database password available. Set BASELINE_DB_PASSWORD or DB_PASSWORD before starting the monitor.'
}

$parent = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Path $parent -Force | Out-Null

$columns = @('timestamp_utc','service_status','service_pid','listener_pid','sql_ok','uptime_seconds','error')
if (-not (Test-Path -LiteralPath $OutputPath)) {
    ($columns -join ',') | Set-Content -LiteralPath $OutputPath -Encoding utf8
}

function Get-ListenerPid {
    $line = netstat.exe -ano | Select-String -Pattern (":$Port\s+.*LISTENING\s+(\d+)$") | Select-Object -First 1
    if (-not $line) { return '' }
    return ([regex]::Match($line.ToString(), '(\d+)$')).Groups[1].Value
}

function Get-ServicePid {
    $lines = sc.exe queryex $ServiceName 2>$null
    $match = $lines | Select-String -Pattern 'PID\s*:\s*(\d+)' | Select-Object -First 1
    if (-not $match) { return '' }
    return $match.Matches[0].Groups[1].Value
}

$endAt = (Get-Date).ToUniversalTime().AddHours($DurationHours)
try {
    while ((Get-Date).ToUniversalTime() -lt $endAt) {
        $timestamp = (Get-Date).ToUniversalTime().ToString('o')
        $serviceStatus = 'UNKNOWN'
        $servicePid = ''
        $listenerPid = ''
        $sqlOk = 'FAIL'
        $uptime = ''
        $errorText = ''

        try {
            $serviceStatus = (Get-Service -Name $ServiceName).Status.ToString()
            $servicePid = Get-ServicePid
            $listenerPid = Get-ListenerPid
            $env:MYSQL_PWD = $dbPassword
            $sqlOutput = & $mysql --protocol=TCP --host=127.0.0.1 --port=$Port --user=root --batch --skip-column-names -e "SHOW GLOBAL STATUS LIKE 'Uptime'" 2>&1
            $sqlExit = $LASTEXITCODE
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
            if ($sqlExit -eq 0 -and $sqlOutput) {
                $sqlOk = 'PASS'
                $uptime = (($sqlOutput | Select-Object -Last 1).ToString().Trim() -split '\s+')[-1]
            } else {
                $errorText = (($sqlOutput | Out-String).Trim() -replace '[\r\n]+', ' ')
            }
        }
        catch {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
            $errorText = $_.Exception.Message -replace '[\r\n]+', ' '
        }

        $row = [pscustomobject]@{
            timestamp_utc = $timestamp
            service_status = $serviceStatus
            service_pid = $servicePid
            listener_pid = $listenerPid
            sql_ok = $sqlOk
            uptime_seconds = $uptime
            error = $errorText
        }
        ($row | ConvertTo-Csv -NoTypeInformation | Select-Object -Skip 1) | Add-Content -LiteralPath $OutputPath -Encoding utf8

        Start-Sleep -Seconds $IntervalSeconds
    }
}
finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Output "MySQL stability monitor finished: $OutputPath"

