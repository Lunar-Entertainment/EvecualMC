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
    Write-Host "`nMod installed successfully into .minecraft/mods!" -ForegroundColor Cyan
    exit 0
}

if ($BuildOnly) {
    Write-Host "Building mod JAR..." -ForegroundColor Green
    & .\gradlew.bat build
    exit $LASTEXITCODE
}

Write-Host "Launching Minecraft 1.20.1 with EvecualMC mod..." -ForegroundColor Green
& .\gradlew.bat runClient
exit $LASTEXITCODE
