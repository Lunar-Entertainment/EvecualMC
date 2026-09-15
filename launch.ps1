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

# Ensure shaderpack is always packaged and up to date with the latest code
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
    
    $shadersRoot = (Get-Item (Join-Path $sourceDir "shaders")).FullName
    $allFiles = Get-ChildItem -Path $shadersRoot -Recurse -File
    $createdDirs = [System.Collections.Generic.HashSet[string]]::new()
    $null = $createdDirs.Add("shaders/")

    foreach ($file in $allFiles) {
        $relPath = $file.FullName.Substring($shadersRoot.Length).TrimStart('\', '/').Replace('\', '/')
        $entryName = "shaders/" + $relPath
        
        $parentDir = [System.IO.Path]::GetDirectoryName($entryName).Replace('\', '/')
        if ($parentDir -and -not $createdDirs.Contains($parentDir + "/")) {
            $null = $archive.CreateEntry($parentDir + "/", [System.IO.Compression.CompressionLevel]::Optimal)
            $null = $createdDirs.Add($parentDir + "/")
        }

        $entry = $archive.CreateEntry($entryName, [System.IO.Compression.CompressionLevel]::Optimal)
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
    
    $releaseShaderDir = "release"
    if (Test-Path $releaseShaderDir) {
        Copy-Item -Path "shader/EvecualTechShader.zip" -Destination (Join-Path $releaseShaderDir "EvecualTechShader.zip") -Force
    }

    $mcShaderDir = Join-Path $env:APPDATA ".minecraft\shaderpacks"
    if (Test-Path $mcShaderDir) {
        Copy-Item -Path "shader/EvecualTechShader.zip" -Destination (Join-Path $mcShaderDir "EvecualTechShader.zip") -Force
    }

    Write-Host "Synchronized and verified EvecualTechShader.zip to shader/, run/shaderpacks/, release/, and .minecraft/shaderpacks" -ForegroundColor Green
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

        $releaseDir = "release"
        if (Test-Path $releaseDir) {
            Get-ChildItem -Path $releaseDir -Filter "evecualmc-*.jar" | Remove-Item -Force
            Copy-Item -Path $jar.FullName -Destination (Join-Path $releaseDir $jar.Name) -Force
            Write-Host "Updated release jar in $releaseDir/$($jar.Name)" -ForegroundColor Green
        }
    }

    Write-Host "`nMod and Shader installed successfully into .minecraft!" -ForegroundColor Cyan
    exit 0
}

if ($BuildOnly) {
    Write-Host "Building mod JAR..." -ForegroundColor Green
    & .\gradlew.bat build
    if ($LASTEXITCODE -eq 0) {
        $releaseDir = "release"
        if (Test-Path $releaseDir) {
            $jarFiles = Get-ChildItem -Path "build\libs" -Filter "evecualmc-*.jar" | Where-Object { $_.Name -notmatch "sources" }
            foreach ($jar in $jarFiles) {
                Get-ChildItem -Path $releaseDir -Filter "evecualmc-*.jar" | Remove-Item -Force
                Copy-Item -Path $jar.FullName -Destination (Join-Path $releaseDir $jar.Name) -Force
                Write-Host "Updated release jar in $releaseDir/$($jar.Name)" -ForegroundColor Green
            }
        }
    }
    exit $LASTEXITCODE
}

Write-Host "Launching Minecraft 1.20.1 with EvecualMC mod..." -ForegroundColor Green
& .\gradlew.bat runClient
exit $LASTEXITCODE
