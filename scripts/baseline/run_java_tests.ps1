[CmdletBinding()]
param([switch]$ListOnly, [string]$TestSourceRoot)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
if (-not $TestSourceRoot) { $TestSourceRoot = Join-Path $repo 'yf-bev2-api\src\test\java' }
# Discover source classes, not stale compiled files or a second hand-maintained list.
$testSources = @{}
$testClasses = @(Get-ChildItem -LiteralPath $TestSourceRoot -Recurse -File -Filter '*Test.java' | ForEach-Object {
  $source = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
  if ($source -notmatch '(?m)^\s*package\s+([\w.]+)\s*;') {
    throw "Missing Java package declaration: $($_.Name)"
  }
  $testClass = $Matches[1] + '.' + $_.BaseName
  $testSources[$testClass] = $_.LastWriteTimeUtc
  $testClass
} | Sort-Object -Unique)
if ($testClasses.Count -eq 0) { throw 'No Java test classes found; refusing an empty test run' }
if ($ListOnly) { $testClasses; exit 0 }
$target = if ($env:BASELINE_BACKEND_TARGET) { $env:BASELINE_BACKEND_TARGET } else { Join-Path $repo '.baseline\backend-target' }
foreach ($testClass in $testClasses) {
  $compiledClass = Join-Path $target ('test-classes\' + $testClass.Replace('.', '\') + '.class')
  if (-not (Test-Path -LiteralPath $compiledClass)) { throw "Test class not compiled: $testClass. Run Maven test-compile/package first." }
  if ((Get-Item -LiteralPath $compiledClass).LastWriteTimeUtc -lt $testSources[$testClass]) {
    throw "Test class is older than its source: $testClass. Rebuild before running tests."
  }
}
$jar = Join-Path $target 'yf-bev2-api.jar'
$runtime = if ($env:BASELINE_RUNTIME_DIR) { $env:BASELINE_RUNTIME_DIR } else { Join-Path $env:TEMP 'keryo-baseline-tests' }
$libDir = Join-Path $runtime 'test-libs'
New-Item -ItemType Directory -Force -Path $libDir | Out-Null

if (-not (Test-Path $jar)) { throw "Backend jar not found: $jar" }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($jar)
$runtimeJars = @()
try {
  foreach ($entry in $archive.Entries) {
    if ($entry.FullName.StartsWith('BOOT-INF/lib/') -and $entry.FullName.EndsWith('.jar')) {
      $destination = Join-Path $libDir ([IO.Path]::GetFileName($entry.FullName))
      $runtimeJars += $destination
      $input = $entry.Open()
      try { $output = [IO.File]::Create($destination); try { $input.CopyTo($output) } finally { $output.Dispose() } } finally { $input.Dispose() }
    }
  }
} finally { $archive.Dispose() }

$m2 = if ($env:BASELINE_MAVEN_REPO) { $env:BASELINE_MAVEN_REPO } else { Join-Path $env:USERPROFILE '.m2\repository' }
$extra = @(
  (Join-Path $m2 'junit\junit\4.13.2\junit-4.13.2.jar'),
  (Join-Path $m2 'org\hamcrest\hamcrest\2.2\hamcrest-2.2.jar'),
  (Join-Path $m2 'org\springframework\spring-test\6.1.2\spring-test-6.1.2.jar'),
  (Join-Path $m2 'org\mockito\mockito-core\5.7.0\mockito-core-5.7.0.jar'),
  (Join-Path $m2 'net\bytebuddy\byte-buddy\1.14.10\byte-buddy-1.14.10.jar'),
  (Join-Path $m2 'net\bytebuddy\byte-buddy-agent\1.14.10\byte-buddy-agent-1.14.10.jar'),
  (Join-Path $m2 'org\objenesis\objenesis\3.3\objenesis-3.3.jar')
)
$missing = $extra | Where-Object { -not (Test-Path $_) }
if ($missing) { throw "Missing Java test dependencies: $($missing -join ', ')" }
$classpathItems = @((Join-Path $target 'classes'), (Join-Path $target 'test-classes'))
$classpathItems += $runtimeJars
$classpathItems += $extra
$classpath = $classpathItems -join ';'
$java = if ($env:BASELINE_JAVA) { $env:BASELINE_JAVA } elseif ($env:BASELINE_JAVA_HOME) { Join-Path $env:BASELINE_JAVA_HOME 'bin\java.exe' } else { (Get-Command java.exe).Source }
Write-Output "JAVA_TEST_CLASSES count=$($testClasses.Count)"
$testClasses | ForEach-Object { Write-Output "  $_" }
$testOutput = @(& $java '-cp' $classpath 'org.junit.runner.JUnitCore' @testClasses)
$testExitCode = $LASTEXITCODE
$testOutput | Write-Output
if ($testExitCode -ne 0) { exit $testExitCode }
if (($testOutput -join "`n") -notmatch 'OK \(([1-9][0-9]*) tests?\)') {
  throw 'No positive JUnit test count reported; refusing a false PASS'
}
Write-Output "JAVA_TEST_RESULT classes=$($testClasses.Count) tests=$($Matches[1])"
exit 0

