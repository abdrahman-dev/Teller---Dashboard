@echo off
:: setup.bat — Build script for Account Management System (Windows cmd)
::
:: This script:
::   1. Ensures lib/ exists
::   2. Downloads missing jar dependencies from Maven Central
::   3. Compiles the Java sources into out/
::   4. Prints the command to run the app
::
:: Run with: setup.bat

setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0"
set "LIB_DIR=%PROJECT_DIR%lib"
set "SRC_DIR=%PROJECT_DIR%src"
set "OUT_DIR=%PROJECT_DIR%out"

set "SQLITE_JAR=%LIB_DIR%\sqlite-jdbc-3.53.4.0.jar"
set "FLATLAF_JAR=%LIB_DIR%\flatlaf-3.7.2.jar"

set "SQLITE_URL=https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.53.4.0/sqlite-jdbc-3.53.4.0.jar"
set "FLATLAF_URL=https://repo1.maven.org/maven2/com/formdev/flatlaf/3.7.2/flatlaf-3.7.2.jar"

echo ================================================
echo   Account Management System — Setup Script
echo ================================================
echo.

:: --- Step 1: Ensure lib/ directory exists ---
if not exist "%LIB_DIR%\" (
    echo [1/3] Creating lib\ directory...
    mkdir "%LIB_DIR%"
) else (
    echo [1/3] lib\ directory already exists.
)

:: --- Step 2: Download missing jars from Maven Central ---
echo.
echo [2/3] Checking jar dependencies...

if exist "%SQLITE_JAR%" (
    echo   sqlite-jdbc-3.53.4.0.jar already present — skipping download.
) else (
    echo   Downloading sqlite-jdbc-3.53.4.0.jar from Maven Central...
    powershell -Command "Invoke-WebRequest -Uri '%SQLITE_URL%' -OutFile '%SQLITE_JAR%'"
    echo   Saved sqlite-jdbc-3.53.4.0.jar to lib\
)

if exist "%FLATLAF_JAR%" (
    echo   flatlaf-3.7.2.jar already present — skipping download.
) else (
    echo   Downloading flatlaf-3.7.2.jar from Maven Central...
    powershell -Command "Invoke-WebRequest -Uri '%FLATLAF_URL%' -OutFile '%FLATLAF_JAR%'"
    echo   Saved flatlaf-3.7.2.jar to lib\
)

:: --- Step 3: Compile Java sources ---
echo.
echo [3/3] Compiling Java sources...
if not exist "%OUT_DIR%\" (
    mkdir "%OUT_DIR%"
)

javac -cp "%SQLITE_JAR%;%FLATLAF_JAR%" -d "%OUT_DIR%" "%SRC_DIR%\*.java"

echo.
echo ================================================
echo   Setup complete!
echo ================================================
echo.
echo To run the application, execute:
echo.
echo    java -cp "%OUT_DIR%;%SQLITE_JAR%;%FLATLAF_JAR%" Main
echo.
echo Default login:  username = admin   password = admin123
echo.
endlocal
