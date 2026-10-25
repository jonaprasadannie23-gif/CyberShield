param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = "Stop"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$WrapperDir = Join-Path $ProjectDir ".mvn\wrapper"
$PropertiesFile = Join-Path $WrapperDir "maven-wrapper.properties"

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
}

$Java = Join-Path $env:JAVA_HOME "bin\java.exe"
if (-not (Test-Path $Java)) {
    Write-Error "JAVA_HOME is not valid: $env:JAVA_HOME"
}

$distributionUrl = (Get-Content $PropertiesFile | Where-Object { $_ -like "distributionUrl=*" }) -replace "distributionUrl=", ""
$wrapperUrl = (Get-Content $PropertiesFile | Where-Object { $_ -like "wrapperUrl=*" }) -replace "wrapperUrl=", ""
$wrapperJar = Join-Path $WrapperDir "maven-wrapper.jar"

if (-not (Test-Path $wrapperJar)) {
    Write-Host "Downloading Maven Wrapper..."
    New-Item -ItemType Directory -Force -Path $WrapperDir | Out-Null
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar
}

Set-Location $ProjectDir
& $Java -classpath $wrapperJar "-Dmaven.multiModuleProjectDirectory=$ProjectDir" org.apache.maven.wrapper.MavenWrapperMain @MavenArgs
exit $LASTEXITCODE
