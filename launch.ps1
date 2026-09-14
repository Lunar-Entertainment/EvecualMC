<#
.SYNOPSIS
    Launcher script for EvecualMC Fabric 1.20.1 mod.
.DESCRIPTION
    Runs Minecraft 1.20.1 with the mod loaded via Gradle runClient, or builds and installs into %APPDATA%/.minecraft/mods.
.PARAMETER Install
    Build and copy mod jar into %APPDATA%/.minecraft/mods
.PARAMETER BuildOnly
    Only build the mod jar without launching
#>
[CmdletBinding()]
param(
    [switch]$Install,
    [switch]$BuildOnly
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "          EvecualMC Fabric 1.20.1 Mod Launcher         " -ForegroundColor Yellow
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java was not found in your PATH. Please install Java 17 or 21."
    exit 1
}

if ($Install) {
    Write-Host "Building mod JAR..." -ForegroundColor Green
    & .\gradlew.bat build
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Build failed with exit code $LASTEXITCODE"
        exit $LASTEXITCODE
    }

    $modsDir = Join-Path $env:APPDATA ".minecraft\mods"
    if (-not (Test-Path $modsDir)) {
        New-Item -ItemType Directory -Path $modsDir -Force | Out-Null
    }

    $jarFiles = Get-ChildItem -Path "build\libs" -Filter "evecualmc-*.jar" | Where-Object { $_.Name -notmatch "sources" }
    foreach ($jar in $jarFiles) {
        Copy-Item -Path $jar.FullName -Destination $modsDir -Force
        Write-Host "Installed $($jar.Name) to $modsDir" -ForegroundColor Green
    }

    $shaderDir = Join-Path $env:APPDATA ".minecraft\shaderpacks"
    if (-not (Test-Path $shaderDir)) {
        New-Item -ItemType Directory -Path $shaderDir -Force | Out-Null
    }
    if (Test-Path "shader/EvecualTechShader.zip") {
        Copy-Item -Path "shader/EvecualTechShader.zip" -Destination (Join-Path $shaderDir "EvecualTechShader.zip") -Force
        Write-Host "Installed EvecualTechShader.zip to $shaderDir" -ForegroundColor Green
    }

    Write-Host "`nMod and Shader installed successfully into .minecraft!" -ForegroundColor Cyan
    exit 0
}

if ($BuildOnly) {
    Write-Host "Building mod JAR..." -ForegroundColor Green
    & .\gradlew.bat build
    exit $LASTEXITCODE
}

# Ensure shaderpack in run/shaderpacks is always up to date with the latest code
function Package-ShaderZip($sourceDir, $zipPath) {
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    if (Test-Path $zipPath) {
        Remove-Item $zipPath -Force
    }
    $destDir = Split-Path -Parent $zipPath
    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }
    $zipStream = [System.IO.File]::Open($zipPath, [System.IO.FileMode]::CreateNew)
    $archive = New-Object System.IO.Compression.ZipArchive($zipStream, [System.IO.Compression.ZipArchiveMode]::Create)
    $null = $archive.CreateEntry("shaders/", [System.IO.Compression.CompressionLevel]::Optimal)
    $shaderFiles = Get-ChildItem -Path (Join-Path $sourceDir "shaders") -File
    foreach ($file in $shaderFiles) {
        $entry = $archive.CreateEntry("shaders/" + $file.Name, [System.IO.Compression.CompressionLevel]::Optimal)
        $entryStream = $entry.Open()
        $fileStream = [System.IO.File]::OpenRead($file.FullName)
        $fileStream.CopyTo($entryStream)
        $fileStream.Dispose()
        $entryStream.Dispose()
    }
    $archive.Dispose()
    $zipStream.Dispose()
}

$shaderSource = "shader/EvecualTechShader"
if (Test-Path $shaderSource) {
    Package-ShaderZip $shaderSource "shader/EvecualTechShader.zip"
    $runShaderDir = "run/shaderpacks"
    if (-not (Test-Path $runShaderDir)) {
        New-Item -ItemType Directory -Path $runShaderDir -Force | Out-Null
    }
    Copy-Item -Path "shader/EvecualTechShader.zip" -Destination (Join-Path $runShaderDir "EvecualTechShader.zip") -Force
    Write-Host "Synchronized and verified EvecualTechShader.zip to $runShaderDir" -ForegroundColor Green
}

Write-Host "Launching Minecraft 1.20.1 with EvecualMC mod..." -ForegroundColor Green
& .\gradlew.bat runClient
exit $LASTEXITCODE
