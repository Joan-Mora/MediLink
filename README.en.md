# MediLink — Hospital Management System

MediLink is a desktop application with an embedded HTTP server. The GUI (Swing) and the backend (HttpServer) run in the same JVM process to simplify deployment and operations.

- Platform: Java 17+ (recommended)
- Database: MySQL/MariaDB
- Packaging: app-image/portable via jpackage (guidance included)

## Key features
- Single-process design: server + GUI in the same JVM (no IPC, fewer moving parts).
- Local REST API consumed by the GUI.
- Modules: Patients, Doctors, Users, Diagnostics.
- Entity activation status (active/inactive) with history (`estado_entidad`).
- `/health` endpoint checks DB connectivity and basic app status.
- Auto-detected port (first free in 8081–8099) unless `MEDILINK_HTTP_PORT` is set.
- "Configure DB" button in the login screen to guide users to the DB setup script.

## Architecture (quick overview)
- Launcher: `app.AppLauncher` starts the backend on a daemon thread and the GUI on the EDT.
- Backend: `app.Main` configures `com.sun.net.httpserver.HttpServer`, registers endpoints and sets the `medilink.baseUrl` system property for the GUI.
- GUI: `gui.MediLinkGUI` navigates views (Dashboard, Patients, Doctors, Diagnostics, Users, Health, Credits) using `gui.models.ApiClient` for HTTP calls.

See `docs/architecture.md` for the full architecture.

## Requirements
- JDK 17+
- MySQL/MariaDB
- MySQL Connector/J (JDBC driver) available in `lib/`
- JavaFX SDK 24 (optional): Not required for the current Swing GUI, but the SDK is included if you plan to add JavaFX views.

See `requirements.txt` for a quick checklist.

## Installation & configuration
1) Database
- Run `sql/init-mysql.sql` to create the schema and sample data.

2) DB credentials
- Run the setup script (`tools/configurar-bd.bat` or the one packaged next to the app) to create/edit `db-config.bat` with `DB_URL`, `DB_USER`, `DB_PASS`.
- On Windows, the login screen has a "Configure DB" button that opens the installation folder and highlights the configuration script.

3) HTTP port (optional)
- Set `MEDILINK_HTTP_PORT`. If not set, the backend will bind the first free port in 8081–8099.

## Running (development)
- Recommended (server + GUI): run `app.AppLauncher`.
- Alternatives: `app.Main` (backend only) and `gui.views.LoginFrame` (GUI only; requires the backend running or the Admin/Admin2025 offline fallback).

## Quick start (Windows PowerShell)

```powershell
# 1) Ensure prerequisites (JDK 17+, MySQL/MariaDB, MySQL Connector/J in lib/)
java -version

# 2) Initialize database (MySQL) using your favorite client with sql/init-mysql.sql
# 3) Configure DB credentials with tools/configurar-bd.bat to create/edit db-config.bat

# 4) Run unified launcher (server + GUI)
# In VS Code, run the class app.AppLauncher.
# Or compile/run via javac/java if you use a simple setup.

# 5) Test health endpoint (optional)
# Open http://localhost:8081/health (actual port may vary 8081–8099)
```

## Code style and formatting
- This project enforces a simple rule for Java sources: 1 blank line after each line of code.
- You can run the VS Code task "Format Java: 1 blank line" which executes `tools/format-java-blank.ps1` to apply the rule across `src/`.

## Authentication
- Endpoint: `POST /auth/login` with `{ "nombre":"...", "contrasena":"..." }`.
- Emergency (offline) credentials: `Admin` / `Admin2025` (no DB required).

## API (summary)
- Health: `GET /health`
- Doctors: `GET /medicos`, `POST /medicos/create`, `POST /medicos/toggle-status`
- Patients: `GET /pacientes`, `POST /pacientes/create`, `GET /pacientes/{id}/diagnosticos`, `POST /pacientes/toggle-status`
- Users: `GET /usuarios`, `POST /usuarios/create`
- Diagnostics: `GET /diagnosticos`, `POST /diagnosticos/create`, `DELETE /diagnosticos/delete`

Full details and examples are in `docs/backend-api.md`.

## Repository layout
- `src/app`: Backend (Main, Endpoints, Db, HttpUtils, AppLauncher, AppInfo)
- `src/gui`: GUI (MediLinkGUI, views, components, models, util)
- `sql`: SQL scripts (init, migrations)
- `tools`: packaging/config/format scripts
- `docs`: full documentation (architecture, API, DB, GUI, config, development, ERD, runbook, code map)

## Packaging
- Single-process design eases jpackage usage:
  - Main class: `app.AppLauncher`.
  - Include `icon.ico`, `logo.png`, `db-config.bat` and `lib/mysql-connector-j-*.jar`.
  - The GUI can read icons from the root or `app/` (typical app-image layout).
- See `docs/development.md` for guidance.

## Troubleshooting
- No DB connection → review `db-config.bat` (see locations in `docs/database.md`) and try `GET /health`.
- GUI cannot reach the server → check ports 8081–8099 and the "System Health" view.
- Missing icons → ensure `icon.ico` or `logo.png` exists in the root or `app/`.

## Security & versioning
- `.gitignore` excludes build artifacts, binaries, large SDKs and sensitive files (like `db-config.bat`).
- Do not commit credentials or backups.

## Documentation
Comprehensive documentation lives in `docs/`:
- Architecture: `docs/architecture.md`
- REST API: `docs/backend-api.md`
- Database: `docs/database.md`
- GUI: `docs/gui.md`
- Configuration: `docs/configuration.md`
- Development/Packaging: `docs/development.md`
- Code map: `docs/code-map.md`
- Runbook: `docs/ops-playbook.md`
- ERD (Mermaid): `docs/erd.md`

## Export to PDF (optional)
If you wish to distribute PDFs, you can export any doc from `docs/` via VS Code (Print to PDF) or with Pandoc:

```powershell
# Example (install pandoc first):
pandoc -s .\docs\backend-deep-dive.md -o .\docs\backend-deep-dive.pdf
pandoc -s .\docs\gui-deep-dive.md -o .\docs\gui-deep-dive.pdf
```