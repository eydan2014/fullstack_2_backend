@echo off
setlocal EnableExtensions DisableDelayedExpansion
set "NUTRIVIDA_MAVEN_VERSION=3.9.14"
set "NUTRIVIDA_MAVEN_CACHE=%~dp0.maven"
set "NUTRIVIDA_MAVEN_HOME=%NUTRIVIDA_MAVEN_CACHE%\apache-maven-%NUTRIVIDA_MAVEN_VERSION%"

if not exist "%NUTRIVIDA_MAVEN_HOME%\bin\mvn.cmd" (
    call :descargar
    if errorlevel 1 exit /b 1
)
if not exist "%NUTRIVIDA_MAVEN_HOME%\bin\mvn.cmd" exit /b 1

if "%~1"=="" (
    echo Maven %NUTRIVIDA_MAVEN_VERSION% listo en "%NUTRIVIDA_MAVEN_HOME%".
    exit /b 0
)

pushd "%~dp0" || exit /b 1
call "%NUTRIVIDA_MAVEN_HOME%\bin\mvn.cmd" %*
set "NUTRIVIDA_MAVEN_RESULTADO=%errorlevel%"
popd
exit /b %NUTRIVIDA_MAVEN_RESULTADO%

:descargar
powershell.exe -NoLogo -NoProfile -NonInteractive -Command ^
    "$ErrorActionPreference = 'Stop';" ^
    "$ProgressPreference = 'SilentlyContinue';" ^
    "$version = $env:NUTRIVIDA_MAVEN_VERSION;" ^
    "$nombre = 'apache-maven-' + $version;" ^
    "$temporal = Join-Path $env:NUTRIVIDA_MAVEN_CACHE ('descarga-' + [guid]::NewGuid().ToString('N'));" ^
    "try {" ^
    "    New-Item -ItemType Directory -Path $temporal -Force | Out-Null;" ^
    "    $zip = Join-Path $temporal ($nombre + '-bin.zip');" ^
    "    $url = 'https://maven-central.storage-download.googleapis.com/maven2/org/apache/maven/apache-maven/' + $version + '/' + $nombre + '-bin.zip';" ^
    "    Write-Host ('Descargando Maven ' + $version + '...');" ^
    "    Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $zip -TimeoutSec 120;" ^
    "    if ((Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash -ne '55fadd669532a3205d5db95f490bf13971d8b0843526f407f29db0e61f074ab3') { throw 'El archivo de Maven no coincide con el SHA-256 esperado.' };" ^
    "    Expand-Archive -LiteralPath $zip -DestinationPath $temporal;" ^
    "    [IO.Directory]::Move((Join-Path $temporal $nombre), $env:NUTRIVIDA_MAVEN_HOME);" ^
    "} finally {" ^
    "    if (Test-Path -LiteralPath $temporal) { Remove-Item -LiteralPath $temporal -Recurse -Force }" ^
    "}"
exit /b %errorlevel%
