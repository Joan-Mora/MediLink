# Desarrollo y Empaquetado

## Requisitos

- JDK 17+ (recomendado).

- MySQL o servidor compatible.

- Conector JDBC de MySQL disponible en `lib/`.

## Compilación y ejecución

- Punto de entrada recomendado (proceso unificado): `app.AppLauncher`.

- Alternativa: ejecutar `app.Main` (solo servidor) y `gui.views.LoginFrame` (GUI) por separado.

## Formato de código

- Tarea disponible en VS Code: "Format Java: 1 blank line" que ejecuta `tools/format-java.ps1`.

## Base de datos

- Ejecutar `sql/init-mysql.sql` para crear esquema y datos de prueba.

- Configurar credenciales con `configurar-bd.bat` para generar/editar `db-config.bat`.

## Empaquetado

- El proyecto incluye scripts en `tools/` y una plantilla portable en `MediLink-Portable/`.

- Recomendación para `jpackage` (referencial):

  - Main class: `app.AppLauncher`.

  - Recursos: incluir `icon.ico`, `logo.png`, `db-config.bat`, `lib/mysql-connector-j-*.jar`.

  - La app-image deberá tener estructura estándar con carpeta `app/` (la GUI ya intenta leer íconos desde ahí).

## Pruebas manuales

- Consultar ejemplos en `docs/backend-api.md` para probar endpoints con PowerShell (`Invoke-RestMethod`).

- Verificar `/health` para validar configuración.

## Solución de problemas

- Si la GUI no conecta: usar el botón "Salud del Sistema" o revisar `/health` en el puerto detectado.

- Si falta `db-config.bat`, ejecutar `configurar-bd.bat` y reiniciar la app.

