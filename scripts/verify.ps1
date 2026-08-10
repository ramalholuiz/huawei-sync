[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $RemainingArguments
)

$ErrorActionPreference = 'Stop'

if ($RemainingArguments.Count -ne 0) {
    Write-Error 'BLOCKED: scripts/verify.ps1 does not accept arguments.'
    exit 2
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$wrapper = Join-Path $repoRoot 'gradlew.bat'

if (-not (Test-Path -LiteralPath $wrapper -PathType Leaf)) {
    Write-Error 'BLOCKED: gradlew.bat not found. Create the Android project before full verification.'
    exit 2
}

Push-Location $repoRoot
try {
    & $wrapper clean test lint assembleDebug
    $gradleExitCode = $LASTEXITCODE
} finally {
    Pop-Location
}

if ($gradleExitCode -ne 0) {
    Write-Error "BLOCKED: Gradle verification failed with exit code $gradleExitCode."
    exit $gradleExitCode
}

Write-Output 'PASS: clean test lint assembleDebug'
exit 0
