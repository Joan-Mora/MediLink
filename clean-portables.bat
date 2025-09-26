@echo off
cd /d "%~dp0"
setlocal EnableExtensions

echo ============================================
echo   Limpiando portables y residuos previos
echo ============================================

for %%D in ("MediLink-Portable" "MediLink-Pro" "MediLink-Simple" "classes") do (
  if exist "%%~D" (
    echo Eliminando %%~D
    rmdir /s /q "%%~D"
  )
)

echo Limpieza completada.
endlocal
