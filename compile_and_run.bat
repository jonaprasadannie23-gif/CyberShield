@echo off
setlocal enabledelayedexpansion

echo ===================================
echo  CyberShield - Compile and Run
echo ===================================
echo.

:: ---- Find Java ----
set "JAVA_EXE="
set "JAVAC_EXE="

:: Check JAVA_HOME first
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
        echo Using JAVA_HOME: %JAVA_HOME%
        goto :found_java
    )
)

:: Search Eclipse Adoptium (Temurin)
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVAC_EXE=%%D\bin\javac.exe"
        set "JAVA_EXE=%%D\bin\java.exe"
        echo Found JDK: %%D
        goto :found_java
    )
)

:: Search standard Java location
for /d %%D in ("C:\Program Files\Java\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVAC_EXE=%%D\bin\javac.exe"
        set "JAVA_EXE=%%D\bin\java.exe"
        echo Found JDK: %%D
        goto :found_java
    )
)

:: Search Microsoft JDK
for /d %%D in ("C:\Program Files\Microsoft\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVAC_EXE=%%D\bin\javac.exe"
        set "JAVA_EXE=%%D\bin\java.exe"
        echo Found JDK: %%D
        goto :found_java
    )
)

:: Try PATH
where javac >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set "JAVAC_EXE=javac"
    set "JAVA_EXE=java"
    goto :found_java
)

echo ERROR: Java JDK not found.
echo Please install Eclipse Temurin JDK 21: https://adoptium.net
echo Or run in PowerShell: winget install EclipseAdoptium.Temurin.21.JDK
pause
exit /b 1

:found_java
echo.
"%JAVA_EXE%" -version 2>&1
echo.

:: ---- Setup directories ----
if not exist "out"  mkdir out
if not exist "data" mkdir data

:: ---- Collect source files ----
echo Collecting source files...
dir /s /b "src\*.java" | findstr /v "\\test\\" > sources.txt
echo.

:: ---- Compile ----
echo Compiling...
"%JAVAC_EXE%" -d out -sourcepath src @sources.txt
del sources.txt 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo COMPILATION FAILED. See errors above.
    pause
    exit /b 1
)

echo Compilation successful!
echo.

:: ---- Run ----
echo Launching CyberShield...
"%JAVA_EXE%" -cp out Main

pause
