@echo off
setlocal ENABLEDELAYEDEXPANSION
cd /d "%~dp0"

echo ============================================================
echo               MediLink - Configuracion de BD
echo ============================================================
echo.
echo Este script te ayuda a configurar la conexion a tu base de datos MySQL.
echo.

REM Determinar base y candidatos de destino
set "BASE=%~dp0"
set "CAND1=%BASE%db-config.bat"
set "CAND2=%BASE%app\db-config.bat"
set "CAND3=%BASE%..\db-config.bat"
set "CAND4=%BASE%..\app\db-config.bat"
set "CAND5=%BASE%MediLink\db-config.bat"
set "CAND6=%BASE%MediLink\app\db-config.bat"

REM Directorios especiales para instalaciones (sin permisos de escritura en Program Files)
set "PDIR=%ProgramData%\MediLink"
set "ADIR=%APPDATA%\MediLink"

REM Detectar si estamos en la raiz del app-image (donde esta MediLink.exe o app\MediLink.cfg)
set "IS_APP_IMAGE=0"
if exist "%BASE%MediLink.exe" set "IS_APP_IMAGE=1"
if exist "%BASE%app\MediLink.cfg" set "IS_APP_IMAGE=1"

REM Detectar si la carpeta BASE es escribible
set "CAN_WRITE_BASE=1"
>"%BASE%.__wtest" echo test >nul 2>&1
if errorlevel 1 set "CAN_WRITE_BASE=0"
if not exist "%BASE%.__wtest" set "CAN_WRITE_BASE=0"
if exist "%BASE%.__wtest" del /q "%BASE%.__wtest" >nul 2>&1

REM Elegir archivo de config preferido para leer/mostrar (el primero que exista en orden)
set "CONFIG_FILE="
for %%F in ("%CAND1%" "%CAND2%" "%CAND3%" "%CAND4%" "%CAND5%" "%CAND6%") do (
    if defined CONFIG_FILE ( rem ya elegido ) else (
        if exist %%~F set "CONFIG_FILE=%%~F"
    )
)
if not defined CONFIG_FILE set "CONFIG_FILE=%CAND1%"
set "CURRENT_URL=jdbc:mysql://localhost:3306/hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set CURRENT_USER=root
set CURRENT_PASS=1234

echo Archivo de configuracion detectado: "%CONFIG_FILE%"
if exist "%CONFIG_FILE%" (
    echo Configuracion actual encontrada:
    call "%CONFIG_FILE%"
    set CURRENT_URL=!DB_URL!
    set CURRENT_USER=!DB_USER!
    set CURRENT_PASS=!DB_PASS!
    echo   URL: "!CURRENT_URL!"
    echo   Usuario: !CURRENT_USER!
    echo   Password: [oculto]
    echo.
) else (
    echo No hay configuracion previa. Usando valores por defecto.
    echo Tambien puedes revisar:
    echo   - %ProgramData%\MediLink\db-config.bat
    echo   - %APPDATA%\MediLink\db-config.bat
    echo.
)

echo Ingresa los nuevos valores (Enter para mantener actual):
echo.

set /p NEW_HOST="Host MySQL [localhost]: "
if "!NEW_HOST!"=="" set NEW_HOST=localhost

set /p NEW_PORT="Puerto [3306]: "
if "!NEW_PORT!"=="" set NEW_PORT=3306

set /p NEW_DB="Nombre de BD [hospital]: "
if "!NEW_DB!"=="" set NEW_DB=hospital

set /p NEW_USER="Usuario [%CURRENT_USER%]: "
if "!NEW_USER!"=="" set NEW_USER=%CURRENT_USER%

set /p NEW_PASS="Password [mantener actual]: "
if "!NEW_PASS!"=="" set NEW_PASS=%CURRENT_PASS%

set "NEW_URL=jdbc:mysql://!NEW_HOST!:!NEW_PORT!/!NEW_DB!?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

echo.
echo Nueva configuracion:
echo   URL: "!NEW_URL!"
echo   Usuario: !NEW_USER!
echo   Password: [configurado]
echo.

set /p CONFIRM="Guardar esta configuracion? (S/n): "
if /i "!CONFIRM!"=="n" (
    echo Configuracion cancelada.
    pause
    exit /b 0
)

REM Escribir en una o varias ubicaciones segun contexto
set "WROTE=0"
if "%IS_APP_IMAGE%"=="1" (
    REM Siempre guardar en ubicaciones comunes y, si se puede, tambien junto al exe
    if not exist "%PDIR%" mkdir "%PDIR%" >nul 2>&1
    call :write_cfg "%PDIR%\db-config.bat" ^& set "WROTE=1"
    if not exist "%ADIR%" mkdir "%ADIR%" >nul 2>&1
    call :write_cfg "%ADIR%\db-config.bat"
    if "%CAN_WRITE_BASE%"=="1" (
        for %%T in ("%CAND1%" "%CAND2%") do (
            for %%D in ("%%~dpT") do (
                if exist %%~D call :write_cfg "%%~T"
            )
        )
    )
) else (
    REM Siempre guardar en ubicaciones comunes y, si se puede, tambien junto al exe
    if not exist "%PDIR%" mkdir "%PDIR%" >nul 2>&1
    call :write_cfg "%PDIR%\db-config.bat" ^& set "WROTE=1"
    if not exist "%ADIR%" mkdir "%ADIR%" >nul 2>&1
    call :write_cfg "%ADIR%\db-config.bat"
    if "%CAN_WRITE_BASE%"=="1" (
        for %%T in ("%CAND1%" "%CAND5%" "%CAND6%") do (
            for %%D in ("%%~dpT") do (
                if not exist %%~D mkdir "%%~D"
                call :write_cfg "%%~T"
            )
        )
    )
)
if "%WROTE%"=="0" (
    for %%T in ("%CONFIG_FILE%") do (
        for %%D in ("%%~dpT") do (
            if not exist %%~D mkdir "%%~D"
            call :write_cfg "%%~T"
        )
    )
)

echo.
echo Listo. La configuracion se ha guardado en ubicaciones comunes:
echo   - %ProgramData%\MediLink\db-config.bat
echo   - %APPDATA%\MediLink\db-config.bat
echo Si la carpeta de instalacion es escribible, tambien se ha guardado una copia junto al .exe.
echo MediLink detecta primero el db-config.bat junto al .exe y, si no existe, busca en estas ubicaciones.
echo.
echo IMPORTANTE: Reinicia MediLink para aplicar los cambios. Si usas app-image, cierra y vuelve a abrir.
echo.
echo Nota: No es necesario ejecutar este asistente como administrador. ProgramData/AppData son las
echo ubicaciones recomendadas para la configuracion.
echo.
pause
endlocal
exit /b 0

:write_cfg
REM Subrutina para escribir el archivo de configuracion con los valores actuales
set "TARGET=%~1"
REM Escapar caracteres especiales para que no se interpreten (principalmente & y ^)
set "ESC_URL=%NEW_URL%"
set "ESC_URL=%ESC_URL:^=^^%"
set "ESC_URL=%ESC_URL:&=^&%"
set "ESC_USER=%NEW_USER%"
set "ESC_USER=%ESC_USER:^=^^%"
set "ESC_USER=%ESC_USER:&=^&%"
set "ESC_PASS=%NEW_PASS%"
set "ESC_PASS=%ESC_PASS:^=^^%"
set "ESC_PASS=%ESC_PASS:&=^&%"
>"%TARGET%" echo @echo off
>>"%TARGET%" echo rem Configuracion de BD para MediLink
>>"%TARGET%" echo set DB_URL=%ESC_URL%
>>"%TARGET%" echo set DB_USER=%ESC_USER%
>>"%TARGET%" echo set DB_PASS=%ESC_PASS%
goto :eof
