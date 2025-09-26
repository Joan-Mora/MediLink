# MediLink — Sistema de Gestión Hospitalaria

MediLink es una aplicación de escritorio con servidor HTTP embebido que integra gestión de pacientes, médicos, usuarios y diagnósticos. La GUI (Swing) y el backend (HttpServer) corren en el mismo proceso JVM para simplificar despliegue y operación.

- Plataforma: Java 17+ (recomendado)
- Base de datos: MySQL/MariaDB
- Empaquetado: app-image/portable con jpackage (guía incluida)

## Características clave
- Proceso único (server + GUI) para evitar dependencias entre procesos.
- API REST local consumida por la GUI.
- Módulos: Pacientes, Médicos, Usuarios, Diagnósticos.
- Estado activo/inactivo por entidad (paciente, médico) con historial.
- Comprobación de salud `/health` (incluye verificación BD).
- Arranque y auto-descubrimiento de puerto (8081–8099 por defecto).
- Botón "Configurar BD" desde el login para guiar al usuario al script de configuración.

## Arquitectura (resumen rápido)
- Lanzador: `app.AppLauncher` (inicia hilo daemon para `app.Main` y luego GUI `gui.views.LoginFrame`).
- Servidor: `app.Main` registra endpoints en `com.sun.net.httpserver.HttpServer` y expone `medilink.baseUrl` a la GUI.
- GUI: `gui.MediLinkGUI` navega vistas (Dashboard, Pacientes, Médicos, Diagnósticos, Usuarios, Salud, Créditos) consumiendo `gui.models.ApiClient`.

Para detalles, consulte `docs/architecture.md`.

## Requisitos
- JDK 17+
- MySQL/MariaDB y conector JDBC (jar en `lib/`)

## Instalación y configuración
1. Base de datos
   - Ejecuta `sql/init-mysql.sql` para crear el esquema y cargar datos de ejemplo.
2. Credenciales BD
   - Ejecuta el script `tools/configurar-bd.bat` o el distribuido junto a la app para generar/editar `db-config.bat` con `DB_URL`, `DB_USER`, `DB_PASS`.
   - En Windows, desde el login, usa el botón "Configurar BD" para abrir la carpeta de instalación y ubicar `configurar-bd.bat`.
3. Puerto HTTP (opcional)
   - Variable de entorno `MEDILINK_HTTP_PORT`. Si no se define, se usa el primer libre entre 8081 y 8099.

## Ejecución (desarrollo)
- Opción recomendada (servidor + GUI): ejecutar `app.AppLauncher`.
- Alternativas: `app.Main` (solo backend) y `gui.views.LoginFrame` (solo GUI, requiere backend o usar Admin/Admin2025).

## Autenticación
- Endpoint: `POST /auth/login` con `{ "nombre":"...", "contrasena":"..." }`.
- Modo emergencia (offline): `Admin` / `Admin2025` (sin BD) para entrar a la GUI y configurar.

## API (resumen)
- Health: `GET /health`
- Médicos: `GET /medicos`, `POST /medicos/create`, `POST /medicos/toggle-status`
- Pacientes: `GET /pacientes`, `POST /pacientes/create`, `GET /pacientes/{id}/diagnosticos`, `POST /pacientes/toggle-status`
- Usuarios: `GET /usuarios`, `POST /usuarios/create`
- Diagnósticos: `GET /diagnosticos`, `POST /diagnosticos/create`, `DELETE /diagnosticos/delete`

Ejemplos completos en `docs/backend-api.md`.

## Estructura principal del repo
- `src/app`: Backend (Main, Endpoints, Db, HttpUtils, AppLauncher, AppInfo)
- `src/gui`: GUI (MediLinkGUI, views, components, models, util)
- `sql`: scripts SQL (init, migraciones)
- `tools`: scripts de empaquetado/configuración/format
- `docs`: documentación detallada (arquitectura, API, BD, GUI, config, desarrollo, ERD, runbook, mapa de código)

## Empaquetado (app-image/portable)
- El diseño de proceso único facilita jpackage. Recomendaciones:
  - Clase principal: `app.AppLauncher`.
  - Incluir `icon.ico`, `logo.png`, `db-config.bat` y `lib/mysql-connector-j-*.jar`.
  - La GUI puede leer iconos desde raíz o `app/` (estructura de app-image típica).
- Ver `docs/development.md` para lineamientos.

## Solución de problemas
- No conecta a BD → revisar `db-config.bat` (ubicaciones posibles en `docs/database.md`) y probar `GET /health`.
- La GUI no ve el servidor → revisar puertos 8081–8099 y "Salud del Sistema" en la app.
- Íconos no cargan → asegurarse de tener `icon.ico` o `logo.png` en raíz o `app/`.

## Seguridad y versionado
- `.gitignore` excluye artefactos de build, binarios, SDKs pesados y archivos sensibles (como `db-config.bat`).
- No versionar credenciales ni backups.

## Licencia y créditos
- © 2025 MediLink. Ver créditos dentro de la app y `docs/`.

## Documentación extendida
La documentación completa se encuentra en la carpeta `docs/`:
- Arquitectura: `docs/architecture.md`
- API REST: `docs/backend-api.md`
- Base de datos: `docs/database.md`
- GUI: `docs/gui.md`
- Configuración: `docs/configuration.md`
- Desarrollo/Empaquetado: `docs/development.md`
- Mapa de código: `docs/code-map.md`
- Runbook: `docs/ops-playbook.md`
- ERD (Mermaid): `docs/erd.md`
# MediLink
