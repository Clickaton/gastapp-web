@REM Maven Wrapper - ejecuta Maven sin tenerlo instalado globalmente
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROP=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_PROP%" (
  echo No se encuentra maven-wrapper.properties
  exit /b 1
)

for /f "tokens=1,2 delims==" %%a in ('type "%WRAPPER_PROP%"') do (
  if "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
  if "%%a"=="distributionUrl" set DISTRIBUTION_URL=%%b
)

if not exist "%WRAPPER_JAR%" (
  echo Descargando Maven Wrapper...
  powershell -NoProfile -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%' -UseBasicParsing}"
  if errorlevel 1 (
    echo Fallo la descarga. Instala Maven o ejecuta desde una IDE.
    exit /b 1
  )
)

set "MAVEN_OPTS=%MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%"
java %MAVEN_OPTS% -jar "%WRAPPER_JAR%" %*
endlocal
