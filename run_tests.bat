@echo off
echo ===================================
echo  CyberShield - Test Suite
echo ===================================

if not exist out mkdir out

echo Compiling all sources including tests...
javac -d out -sourcepath src src\test\CyberShieldTest.java
if %ERRORLEVEL% NEQ 0 (
    echo COMPILATION FAILED.
    pause
    exit /b 1
)

echo Running tests...
java -cp out test.CyberShieldTest
echo.
echo Tests complete.
pause
