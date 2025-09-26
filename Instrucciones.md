# MediLink - Instrucciones de uso

Gracias por instalar o usar MediLink.

Este paquete incluye:

- MediLink.exe (lanzador sin consola)
- start.bat (lanzador con consola y logs en portable.log)
- db-config.bat (credenciales de la base de datos)
- configurar-bd.bat (asistente para crear/editar db-config.bat)
- Respaldo-Limpiar-BD.bat (utilidades de respaldo de la BD)
- resources/ y Resources/ (imágenes y recursos estáticos)
- sql/ (scripts SQL de inicialización / migración)
- Backups/ y backups/ (carpetas vacías para respaldos)

## 1) Configurar la Base de Datos

1. Asegúrate de tener MySQL en otra máquina o localmente y con el esquema `hospital`.
2. Ejecuta `configurar-bd.bat` y completa URL, usuario y contraseña.
   - Ejemplo de URL: `jdbc:mysql://localhost:3306/hospital?useSSL=false&serverTimezone=UTC`
3. Se creará/actualizará `db-config.bat` en la carpeta actual y (si existe) dentro de `MediLink/`.

## 2) Iniciar la aplicación

- Si existe la carpeta `MediLink/` (app-image), ejecuta `MediLink/MediLink.exe`.
- Alternativamente, usa `MediLink.exe` o `start.bat` en la raíz del portable.

## 3) Probar salud del backend (opcional)

- Con la app abierta, abre un navegador en `http://localhost:8081/health`.
- Debe indicar `ok:true` y `db:"connected"` si todo está bien.

## 4) Respaldo y limpieza

- Ejecuta `Respaldo-Limpiar-BD.bat` para crear un respaldo con `mysqldump` en `backups/AAAAmmdd-HHMMSS/`.
- Requiere tener el cliente `mysqldump` y `mysql` en el PATH.

## 5) Instalador (Windows)

El instalador `.exe` copia estos archivos al directorio de instalación, crea accesos directos de menú e incluye:

- `configurar-bd.bat`, `db-config.bat`, `Instrucciones.md`, `Respaldo-Limpiar-BD.bat`
- Carpetas `resources/`, `Resources/`, `sql/`, `Backups/` y `backups/` vacías

Ante cualquier problema, revisa `portable.log` (si iniciaste con `start.bat`).
