@echo off
REM MediLink - Build Portable (sin emojis y con icono)
cd /d "%~dp0"
setlocal EnableExtensions EnableDelayedExpansion

echo ============================================
echo    MediLink - Crear Portable Limpio
echo ============================================

set PORTABLE=MediLink-Portable

REM Limpiar artefactos previos (conservar %PORTABLE%\MediLink si existe)
if not exist "%PORTABLE%" (
  mkdir "%PORTABLE%"
) else (
  if exist "%PORTABLE%\classes" rmdir /s /q "%PORTABLE%\classes"
  if exist "%PORTABLE%\lib" rmdir /s /q "%PORTABLE%\lib"
  if exist "%PORTABLE%\sql" rmdir /s /q "%PORTABLE%\sql"
  if exist "%PORTABLE%\backups" rmdir /s /q "%PORTABLE%\backups"
  if exist "%PORTABLE%\resources" rmdir /s /q "%PORTABLE%\resources"
  if exist "%PORTABLE%\start.bat" del /q "%PORTABLE%\start.bat"
  if exist "%PORTABLE%\MediLink.vbs" del /q "%PORTABLE%\MediLink.vbs"
  if exist "%PORTABLE%\MediLink.exe" del /q "%PORTABLE%\MediLink.exe"
  if exist "%PORTABLE%\make-exe.ps1" del /q "%PORTABLE%\make-exe.ps1"
  if exist "%PORTABLE%\README.txt" del /q "%PORTABLE%\README.txt"
  if exist "%PORTABLE%\config-example.bat" del /q "%PORTABLE%\config-example.bat"
)
if exist "classes" rmdir /s /q "classes"

REM Crear estructura portable
mkdir "%PORTABLE%\classes"
mkdir "%PORTABLE%\lib" 2>nul
mkdir "%PORTABLE%\backups" 2>nul

REM 1) Compilar directo al portable
echo [1/2] Compilando codigo...
javac -cp . -d "%PORTABLE%\classes" src\app\*.java src\gui\*.java src\gui\views\*.java src\gui\components\*.java src\gui\models\*.java src\gui\util\*.java
if %ERRORLEVEL% neq 0 (
  echo ERROR: Fallo en la compilacion.
  pause & exit /b 1
)

REM Copiar librerias si existen
if exist "lib" xcopy /e /i /h /y "lib" "%PORTABLE%\lib" >nul
if exist "sql" xcopy /e /i /h /y "sql" "%PORTABLE%\sql" >nul
if exist "backups" xcopy /e /i /h /y "backups" "%PORTABLE%\backups" >nul
if exist "resources" xcopy /e /i /h /y "resources" "%PORTABLE%\resources" >nul
if exist "Resources" xcopy /e /i /h /y "Resources" "%PORTABLE%\Resources" >nul
if exist "Instrucciones.md" copy /y "Instrucciones.md" "%PORTABLE%\Instrucciones.md" >nul
if exist "Respaldo-Limpiar-BD.bat" copy /y "Respaldo-Limpiar-BD.bat" "%PORTABLE%\Respaldo-Limpiar-BD.bat" >nul
REM Copiar herramientas de configuracion de BD (siempre la ultima version)
copy /y "tools\configurar-bd.bat" "%PORTABLE%\configurar-bd.bat" >nul
if not exist "%PORTABLE%\db-config.bat" (
  echo @echo off>"%PORTABLE%\db-config.bat"
  echo rem Configuracion de BD por defecto>>"%PORTABLE%\db-config.bat"
  echo set DB_URL=jdbc:mysql://localhost:3306/hospital?useSSL=false^&serverTimezone=UTC>>"%PORTABLE%\db-config.bat"
  echo set DB_USER=root>>"%PORTABLE%\db-config.bat"
  echo set DB_PASS=1234>>"%PORTABLE%\db-config.bat"
)

REM Copiar iconos si existen
if exist "icon.png" copy /y "icon.png" "%PORTABLE%\" >nul
if exist "icon.ico" copy /y "icon.ico" "%PORTABLE%\" >nul
if exist "logo.png" copy /y "logo.png" "%PORTABLE%\" >nul
REM Asegurar Backups vacia
if not exist "%PORTABLE%\Backups" mkdir "%PORTABLE%\Backups"
if not exist "%PORTABLE%\backups" mkdir "%PORTABLE%\backups"

REM 2) Crear launcher unico (prioriza app-image y runtime embebido)
echo [2/2] Creando launcher...
echo @echo off > "%PORTABLE%\start.bat"
echo setlocal EnableExtensions EnableDelayedExpansion >> "%PORTABLE%\start.bat"
echo cd /d "%%~dp0" >> "%PORTABLE%\start.bat"
echo set LOG=portable.log >> "%PORTABLE%\start.bat"
echo echo ==== MediLink portable %%date%% %%time%% ====^>^> "%%LOG%%" >> "%PORTABLE%\start.bat"
echo if exist "config.bat" call config.bat ^>^> "%%LOG%%" 2^>^&1 >> "%PORTABLE%\start.bat"
echo if exist "db-config.bat" call db-config.bat ^>^> "%%LOG%%" 2^>^&1 >> "%PORTABLE%\start.bat"
echo rem 1^) Si existe app-image jpackage, usarlo ^(incluye runtime^) >> "%PORTABLE%\start.bat"
echo if exist "MediLink\MediLink.exe" ^(>> "%PORTABLE%\start.bat"
echo ^  echo Iniciando app-image... ^>^> "%%LOG%%" >> "%PORTABLE%\start.bat"
echo ^  start "MediLink" /b "MediLink\MediLink.exe" >> "%PORTABLE%\start.bat"
echo ^  exit /b >> "%PORTABLE%\start.bat"
echo ^) >> "%PORTABLE%\start.bat"
echo rem 2^) Si existe runtime embebido ^(jlink^), usarlo >> "%PORTABLE%\start.bat"
echo set JAVA_BIN= >> "%PORTABLE%\start.bat"
echo if exist "runtime\bin\java.exe" set "JAVA_BIN=runtime\bin\java.exe" >> "%PORTABLE%\start.bat"
echo if not defined JAVA_BIN set "JAVA_BIN=java" >> "%PORTABLE%\start.bat"
echo echo Using JAVA_BIN=%%JAVA_BIN%% ^>^> "%%LOG%%" >> "%PORTABLE%\start.bat"
echo echo Iniciando MediLink... ^>^> "%%LOG%%" >> "%PORTABLE%\start.bat"
echo "%%JAVA_BIN%%" -cp "classes;lib/*" app.AppLauncher ^>^> "%%LOG%%" 2^>^&1 >> "%PORTABLE%\start.bat"
echo if errorlevel 1 ^( >> "%PORTABLE%\start.bat"
echo ^  echo ERROR: No se pudo iniciar. Revise portable.log. ^>^> "%%LOG%%" >> "%PORTABLE%\start.bat"
echo ^  echo. >> "%PORTABLE%\start.bat"
echo ^  echo ERROR: No se pudo iniciar. Verifique que Java este en PATH o ejecute MediLink\MediLink.exe si existe. >> "%PORTABLE%\start.bat"
echo ^  echo Revise portable.log para mas detalles. >> "%PORTABLE%\start.bat"
echo ^  pause >> "%PORTABLE%\start.bat"
echo ^) >> "%PORTABLE%\start.bat"

REM README
echo MediLink - Portable > "%PORTABLE%\README.txt"
echo. >> "%PORTABLE%\README.txt"
echo Uso: >> "%PORTABLE%\README.txt"
echo - start.bat ^(todo en uno^) o MediLink.exe >> "%PORTABLE%\README.txt"
echo. >> "%PORTABLE%\README.txt"
echo Icono: >> "%PORTABLE%\README.txt"
echo - Coloca tu icono como "icon.png" o "icon.ico" junto a los .bat >> "%PORTABLE%\README.txt"
echo - La ventana usara ese icono automaticamente >> "%PORTABLE%\README.txt"
echo. >> "%PORTABLE%\README.txt"
echo Base de datos: >> "%PORTABLE%\README.txt"
echo - Usa MySQL en jdbc:mysql://localhost:3306/hospital con usuario root y password 1234 (configurable via db-config.bat) >> "%PORTABLE%\README.txt"
echo - Asegurate de tener MySQL iniciado y el esquema creado; el portable incluye el driver (lib\mysql-connector-*.jar) >> "%PORTABLE%\README.txt"
echo - Puedes usar el script SQL: sql\init-mysql.sql ^(ejecutalo en tu servidor MySQL^) >> "%PORTABLE%\README.txt"
echo - Puedes usar el script SQL: sql\init-mysql.sql ^(ejecutalo en tu servidor MySQL^) >> "%PORTABLE%\README.txt"
echo. >> "%PORTABLE%\README.txt"
echo Configuracion rapida para otra BD ^(opcional^): >> "%PORTABLE%\README.txt"
echo - Ejecuta configurar-bd.bat para crear db-config.bat >> "%PORTABLE%\README.txt"
echo - Alternativamente, copia config-example.bat a db-config.bat y edita: DB_URL, DB_USER, DB_PASS >> "%PORTABLE%\README.txt"
echo - Prueba salud: Invoke-RestMethod -Uri "http://localhost:8081/health" -Method GET >> "%PORTABLE%\README.txt"

REM VBScript launcher (sin ventana CMD)
echo Set WshShell = CreateObject("WScript.Shell") > "%PORTABLE%\MediLink.vbs"
echo WshShell.CurrentDirectory = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName) >> "%PORTABLE%\MediLink.vbs"
echo WshShell.Run "cmd /c start.bat", 0, False >> "%PORTABLE%\MediLink.vbs"

REM Generar EXE sin consola (usa icon.ico si existe) via csc.exe
echo [3/3] Construyendo MediLink.exe...
copy /y "tools\make-exe.ps1" "%PORTABLE%\make-exe.ps1" >nul
set "APP_VERSION=%APP_VERSION%"
if "%APP_VERSION%"=="" set "APP_VERSION=1.0.0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%PORTABLE%\make-exe.ps1" -ProductVersion "%APP_VERSION%" -FileVersion "%APP_VERSION%.0" -ProductName "MediLink" -Company "MediLink" -Description "MediLink Portable Launcher" >nul 2>&1

>>"%PORTABLE%\README.txt" echo.
>>"%PORTABLE%\README.txt" echo Lanzador recomendado:
>>"%PORTABLE%\README.txt" echo - Usa MediLink.exe para iniciar sin consolas (toma icon.ico automaticamente)

REM Config de ejemplo
>"%PORTABLE%\config-example.bat" echo @echo off
>>"%PORTABLE%\config-example.bat" echo rem Copia este archivo a config.bat y edita los valores:
>>"%PORTABLE%\config-example.bat" echo rem Ejemplo de URL compatible con MySQL 8 (sin SSL):
>>"%PORTABLE%\config-example.bat" echo set DB_URL=jdbc:mysql://localhost:3306/hospital?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
>>"%PORTABLE%\config-example.bat" echo set DB_USER=root
>>"%PORTABLE%\config-example.bat" echo set DB_PASS=1234
echo ============================================
echo Portable creado en: %PORTABLE%\
echo - Ejecuta start.bat ^(o MediLink.exe^) 
echo - Nota: La carpeta "%PORTABLE%\MediLink" la genera el empaquetado con jpackage ^(tools\package-win.ps1^)
echo ============================================
endlocal
