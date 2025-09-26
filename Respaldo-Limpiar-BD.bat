@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

REM Utilidad simple de respaldo de BD usando mysqldump
if not exist "db-config.bat" (
  echo No se encontro db-config.bat en %cd%.
  echo Ejecute configurar-bd.bat primero.
  exit /b 1
)
call db-config.bat

if not defined DB_URL (
  echo DB_URL no definida en db-config.bat
  exit /b 1
)
if not defined DB_USER (
  echo DB_USER no definida en db-config.bat
  exit /b 1
)

REM Extraer host y base de datos desde DB_URL (formato jdbc:mysql://host:puerto/base?...)
set "DB_HOST=localhost"
set "DB_NAME=hospital"
for /f "tokens=1-5 delims=/?:=&" %%A in ("%DB_URL%") do (
  REM %%A=jdbc:mysql:  %%B=  %%C=  %%D=host:puerto  %%E=base
  set "DB_NAME=%%E"
  for /f "tokens=1 delims=?" %%X in ("%%D") do set "DB_HOST=%%X"
)

set "STAMP=%DATE:~-4%%DATE:~3,2%%DATE:~0,2%-%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%"
set "STAMP=%STAMP: =0%"
set "OUTDIR=backups\%STAMP%"
if not exist "backups" mkdir "backups"
mkdir "%OUTDIR%" 2>nul

echo Creando respaldo en %OUTDIR% ...
if defined DB_PASS (
  set "MYSQL_PWD=%DB_PASS%"
)
where mysqldump >nul 2>&1
if errorlevel 1 (
  echo No se encontro mysqldump en el PATH. Instale MySQL Client Tools.
  exit /b 1
)

mysqldump -h %DB_HOST% -u %DB_USER% --databases %DB_NAME% > "%OUTDIR%\%DB_NAME%.sql"
if errorlevel 1 (
  echo Error al ejecutar mysqldump.
  exit /b 1
)
echo Respaldo completado: %OUTDIR%\%DB_NAME%.sql
exit /b 0
