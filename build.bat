@echo off
echo ===================================
echo  CyberShield - Build and Run
echo ===================================

if not exist out mkdir out

echo Compiling...
javac -d out -sourcepath src src\Main.java
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED.
    pause
    exit /b 1
)

echo Compilation successful. Launching CyberShield...
java -cp out Main
