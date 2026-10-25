# CyberShield website launcher (Spring Boot + Vite)
# The previous script compiled the Swing app and wrote javac's source list with
# UTF-16 (PowerShell Set-Content default), which javac cannot parse.
# Use .\run-desktop.ps1 for the original Swing application.

$ErrorActionPreference = "Stop"
$Root = $PSScriptRoot
$JavaHome = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
if (Test-Path (Join-Path $JavaHome "bin\java.exe")) {
    $env:JAVA_HOME = $JavaHome
}

Write-Host "Starting CyberShield backend on http://localhost:8080 ..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$Root\backend'; `$env:JAVA_HOME = '$env:JAVA_HOME'; .\mvnw.cmd spring-boot:run"
)

Write-Host "Starting CyberShield frontend on http://localhost:5173 ..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$Root\frontend'; if (-not (Test-Path node_modules)) { npm install }; npm run dev"
)

Write-Host ""
Write-Host "Open http://localhost:5173 in your browser once both windows report they are ready."
Write-Host "Leave both PowerShell windows open while you use the site."
