# Builds dist/HexGrid/HexGrid.exe with a private runtime beside it, then zips that folder.
# The exe is a launcher. It does not run without runtime/ and app/ from the same zip.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Resolve-JdkTool([string]$Name) {
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\$Name.exe"
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }
    $cmd = Get-Command "$Name.exe" -ErrorAction SilentlyContinue
    if (-not $cmd) {
        $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    }
    if ($cmd) {
        return $cmd.Source
    }
    return $null
}

function Get-ToolFeature([string]$ToolPath) {
    $output = & $ToolPath --version 2>&1 | Out-String
    if ($output -match '(\d+)') {
        return [int]$Matches[1]
    }
    throw "Could not read a JDK feature version from $ToolPath --version: $output"
}

$jlink = Resolve-JdkTool 'jlink'
$jpackage = Resolve-JdkTool 'jpackage'
$jdeps = Resolve-JdkTool 'jdeps'
if (-not $jlink -or -not $jpackage -or -not $jdeps) {
    throw 'jlink, jpackage, and jdeps must come from a JDK 27 or newer. Set JAVA_HOME to that JDK.'
}

$feature = Get-ToolFeature $jlink
if ($feature -lt 27) {
    throw "JDK $feature is older than 27. Set JAVA_HOME to JDK 27 or newer."
}

$env:JAVA_HOME = Split-Path (Split-Path $jlink -Parent) -Parent

$jarName = 'hexgrid-1.0.jar'
$builtJar = Join-Path $PSScriptRoot "target\$jarName"
& mvn -q package
if ($LASTEXITCODE -ne 0) {
    throw "mvn package failed ($LASTEXITCODE)"
}
if (-not (Test-Path -LiteralPath $builtJar)) {
    throw "Expected shaded jar at $builtJar"
}

$dist = Join-Path $PSScriptRoot 'dist'
$inputDir = Join-Path $dist 'input'
$runtime = Join-Path $dist 'runtime'
$appImage = Join-Path $dist 'HexGrid'
$zipPath = Join-Path $dist 'HexGrid-windows.zip'

foreach ($path in @($inputDir, $runtime, $appImage, $zipPath)) {
    if (Test-Path -LiteralPath $path) {
        Remove-Item -LiteralPath $path -Recurse -Force
    }
}
New-Item -ItemType Directory -Path $inputDir | Out-Null
Copy-Item -LiteralPath $builtJar -Destination (Join-Path $inputDir $jarName)

$depsText = & $jdeps --multi-release 27 --ignore-missing-deps --print-module-deps $builtJar 2>&1 | Out-String
if ($LASTEXITCODE -ne 0) {
    throw "jdeps failed ($LASTEXITCODE): $depsText"
}
$depsLine = $null
foreach ($line in ($depsText -split '\r?\n')) {
    $trimmed = $line.Trim()
    if ($trimmed -match '^[\w.]+(,[\w.]+)*$') {
        $depsLine = $trimmed
    }
}
$found = @()
if ($depsLine) {
    $found = $depsLine -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ }
}
$required = @('java.desktop', 'java.xml', 'java.logging', 'java.management')
$modules = @($required + $found | Select-Object -Unique)
$moduleArg = $modules -join ','

& $jlink --add-modules $moduleArg --no-header-files --no-man-pages --output $runtime
if ($LASTEXITCODE -ne 0) {
    throw "jlink failed ($LASTEXITCODE)"
}

& $jpackage `
    --type app-image `
    --name HexGrid `
    --app-version 1.0 `
    --dest $dist `
    --input $inputDir `
    --main-jar $jarName `
    --main-class com.how_hard_can_it_be.hexgrid.HexGridDriver `
    --runtime-image $runtime
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed ($LASTEXITCODE)"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($appImage, $zipPath)

Write-Output "Wrote $zipPath"
Write-Output "Unzip it and run HexGrid.exe. The exe needs the runtime and app folders beside it."
