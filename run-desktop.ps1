# CyberShield Swing desktop launcher
# Original run.ps1 wrote sources.txt with PowerShell's default UTF-16 BOM,
# which javac cannot read as an argument file. This copy uses ASCII encoding.

$ProjectRoot = $PSScriptRoot
$SrcDir      = Join-Path $ProjectRoot "src"
$OutDir      = Join-Path $ProjectRoot "out"
$DataDir     = Join-Path $ProjectRoot "data"

$JavaHome = $env:JAVA_HOME
if (-not $JavaHome) {
    $candidates = @(
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\Java"
    )
    foreach ($base in $candidates) {
        if (Test-Path $base) {
            $found = Get-ChildItem $base -Directory -ErrorAction SilentlyContinue |
                     Where-Object { $_.Name -like "*jdk*" } |
                     Sort-Object Name -Descending |
                     Select-Object -First 1
            if ($found) {
                $JavaHome = $found.FullName
                break
            }
        }
    }
}

if (-not $JavaHome) {
    Write-Host "ERROR: Java JDK not found." -ForegroundColor Red
    exit 1
}

$Javac = Join-Path $JavaHome "bin\javac.exe"
$Java  = Join-Path $JavaHome "bin\java.exe"

if (-not (Test-Path $OutDir))  { New-Item -ItemType Directory -Path $OutDir  | Out-Null }
if (-not (Test-Path $DataDir)) { New-Item -ItemType Directory -Path $DataDir | Out-Null }

$SourceFiles = Get-ChildItem -Path $SrcDir -Recurse -Filter "*.java" |
               Where-Object { $_.FullName -notlike "*\test\*" } |
               ForEach-Object { $_.FullName }

$SourceListFile = Join-Path $ProjectRoot "sources.txt"
$SourceFiles | Set-Content -Path $SourceListFile -Encoding ascii

Write-Host "Compiling Swing application..."
& $Javac -d $OutDir -sourcepath $SrcDir "@$SourceListFile"
Remove-Item $SourceListFile -ErrorAction SilentlyContinue

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Set-Location $ProjectRoot
& $Java -cp $OutDir Main
