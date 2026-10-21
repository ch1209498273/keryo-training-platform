#Requires -RunAsAdministrator

[CmdletBinding()]
param(
    [string]$ServiceName = 'MySQL84',
    [string]$MySqlHome = '',
    [string]$ConfigPath = '',
    [string]$DataPath = '',
    [int]$WaitSeconds = 60
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($MySqlHome)) {
    $MySqlHome = if ($env:MYSQL_HOME) { $env:MYSQL_HOME } else { 'C:\Program Files\MySQL\MySQL Server 8.4' }
}
if ([string]::IsNullOrWhiteSpace($ConfigPath)) {
    $ConfigPath = if ($env:MYSQL_DEFAULTS_FILE) { $env:MYSQL_DEFAULTS_FILE } else { Join-Path $MySqlHome 'my.ini' }
}
if ([string]::IsNullOrWhiteSpace($DataPath)) {
    $DataPath = if ($env:MYSQL_DATA_DIR) { $env:MYSQL_DATA_DIR } else { Join-Path (Split-Path -Parent $ConfigPath) 'data' }
}

$mysqld = Join-Path $MySqlHome 'bin\mysqld.exe'
$serviceKey = "HKLM:\SYSTEM\CurrentControlSet\Services\$ServiceName"
$expectedImagePath = '"{0}" --defaults-file={1} {2}' -f $mysqld, $ConfigPath, $ServiceName

if (-not (Test-Path -LiteralPath $mysqld -PathType Leaf)) {
    throw "mysqld.exe not found: $mysqld"
}
if (-not (Test-Path -LiteralPath $ConfigPath -PathType Leaf)) {
    throw "MySQL config not found: $ConfigPath"
}
if (-not (Test-Path -LiteralPath $DataPath -PathType Container)) {
    throw "MySQL data directory not found: $DataPath"
}

$parsedDefaults = & $mysqld "--defaults-file=$ConfigPath" '--print-defaults' 2>&1 | Out-String
if ($LASTEXITCODE -ne 0) {
    throw "MySQL config cannot be parsed: $parsedDefaults"
}
if ($parsedDefaults -match 'skip[-_]name[-_]resolve') {
    throw 'The active repair config must not enable skip-name-resolve while accounts use localhost.'
}

$service = Get-Service -Name $ServiceName
$oldImagePath = (Get-ItemProperty -LiteralPath $serviceKey -Name ImagePath).ImagePath
$changed = $false
$recoveryActions = 'restart/60000/restart/60000/""/0'

try {
    if ($oldImagePath -ne $expectedImagePath) {
        Write-Output "Updating $ServiceName ImagePath to use $ConfigPath"
        Set-ItemProperty -LiteralPath $serviceKey -Name ImagePath -Value $expectedImagePath
        $changed = $true
    } else {
        Write-Output "$ServiceName already uses $ConfigPath"
    }

    # Automatic start only covers Windows boot. Configure recovery for an
    # unexpected process/service failure so the local runtime can self-heal.
    $failureOutput = & sc.exe @('failure', $ServiceName, 'reset=', '86400', 'actions=', $recoveryActions) 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to configure $ServiceName failure recovery: $($failureOutput -join ' ')"
    }

    if ($service.Status -eq 'Running') {
        Stop-Service -Name $ServiceName -Force
    }
    Start-Service -Name $ServiceName

    $deadline = (Get-Date).AddSeconds($WaitSeconds)
    do {
        Start-Sleep -Seconds 1
        $service = Get-Service -Name $ServiceName
        if ($service.Status -eq 'Running') {
            break
        }
    } while ((Get-Date) -lt $deadline)

    if ((Get-Service -Name $ServiceName).Status -ne 'Running') {
        throw "$ServiceName did not reach Running state within $WaitSeconds seconds"
    }

    $listenerLine = netstat.exe -ano | Select-String -Pattern ':3306\s+.*LISTENING\s+(\d+)$' | Select-Object -First 1
    if (-not $listenerLine) {
        throw 'MySQL service is running but port 3306 is not listening'
    }

    $listenerPid = ([regex]::Match($listenerLine.ToString(), '(\d+)$')).Groups[1].Value
    Write-Output "MySQL runtime repair passed; listener PID=$listenerPid"
    Write-Output "ImagePath=$((Get-ItemProperty -LiteralPath $serviceKey -Name ImagePath).ImagePath)"
}
catch {
    if ($changed) {
        Write-Warning 'Repair failed; restoring the original service ImagePath.'
        Set-ItemProperty -LiteralPath $serviceKey -Name ImagePath -Value $oldImagePath
        try { Start-Service -Name $ServiceName -ErrorAction Stop } catch { }
    }
    throw
}

