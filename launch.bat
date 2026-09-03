@echo off
setlocal enabledelayedexpansion
title EvecualMC - Minecraft 1.20.1 Launcher

echo =======================================================
echo          EvecualMC Fabric 1.20.1 Mod Launcher         
echo =======================================================
echo.

cd /d "%~dp0"

REM Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH!
    echo Please install Java 17 or Java 21 to run Minecraft 1.20.1 Fabric.
    pause
    exit /b 1
)

if "%1"=="--install" goto install_mods
if "%1"=="--build" goto build_only
if "%1"=="--help" goto show_help

echo Launching Minecraft 1.20.1 with EvecualMC mod...
echo (First launch may take a minute while assets are prepared)
echo.

call gradlew.bat runClient
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Minecraft client failed to start or exited with an error.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Minecraft session ended successfully.
goto end

:install_mods
echo Building release mod jar...
call gradlew.bat build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed!
    pause
    exit /b %ERRORLEVEL%
)

set "MODS_DIR=%APPDATA%\.minecraft\mods"
if not exist "%MODS_DIR%" (
    echo Creating mods directory at %MODS_DIR%...
    mkdir "%MODS_DIR%"
)

echo Copying mod jar to %MODS_DIR%...
for %%f in (build\libs\evecualmc-*.jar) do (
    echo %%~nxf | findstr /i "sources" >nul
    if errorlevel 1 (
        copy /y "%%f" "%MODS_DIR%\"
        echo Installed %%~nxf into %MODS_DIR%
    )
)
echo.
echo Mod installed successfully to your standard .minecraft/mods folder!
echo You can now launch Minecraft 1.20.1 with Fabric Loader from the official Minecraft Launcher.
goto end

:build_only
echo Building mod jar...
call gradlew.bat build
goto end

:show_help
echo Usage:
echo   launch.bat                 Launch Minecraft 1.20.1 with EvecualMC mod in dev environment
echo   launch.bat --install       Build and copy the mod JAR into your %%APPDATA%%\.minecraft\mods folder
echo   launch.bat --build         Build the mod JAR only (outputs to build/libs/)
echo   launch.bat --help          Show this help message
echo.
goto end

:end
if "%~1"=="" pause
exit /b 0
