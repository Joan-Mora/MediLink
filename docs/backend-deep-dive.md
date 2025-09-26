# Backend Deep Dive — MediLink

This document explains each backend file and its code blocks: purpose, behavior, key APIs, and caveats.

## Contents

- Overview and runtime model

- Files walkthrough (AppInfo, AppLauncher, Main, Db, HttpUtils, AuthEndpoints, PacientesEndpoints, MedicosEndpoints, UsuariosEndpoints, DiagnosticosEndpoints)

- Error handling & logging

- Performance and scalability notes

- Security considerations

## Runtime model

- Single JVM process. `AppLauncher` starts `Main` (HTTP server) on a daemon thread and the Swing GUI on the EDT.

- The GUI discovers the backend base URL from `System.getProperty("medilink.baseUrl")` or probing `/health` across 8081–8099.

---

## app/AppInfo.java

- Holds application metadata: `NAME`, `VERSION`, `COPYRIGHT`.

- Keep version consistent with release notes and packaging scripts.

## app/AppLauncher.java

- Entry point unifying server + GUI.

- Creates a daemon thread named `medilink-server` to run `app.Main.main`.

- Then schedules the GUI startup with `SwingUtilities.invokeLater(() -> new gui.views.LoginFrame().setVisible(true))`.

- Error handling: prints stack trace and shows a dialog if GUI fails to start.

- Caveats: The server thread is daemon, so the JVM exits when the GUI closes (desired behavior for desktop apps).

## app/Main.java

- Resolves the port using `MEDILINK_HTTP_PORT` or first free between 8081–8099.

- Creates `HttpServer` and registers:

  - `/health` (inner `HealthHandler`)

  - `PacientesEndpoints.register(server)`

  - `MedicosEndpoints.register(server)`

  - `UsuariosEndpoints.register(server)`

  - `AuthEndpoints.register(server)`

  - `DiagnosticosEndpoints.register(server)`

- Sets system property `medilink.baseUrl` before `server.start()` to avoid race conditions on GUI startup.

- Prints an ASCII banner and available endpoints on startup (useful for logs).

### HealthHandler
- Returns JSON including configuration source path, sanitized DB URL, DB connectivity (`SELECT 1`) and `ok` flag.

- Uses `Db.getConfigSourcePath()` and `Db.getConnection()`.

- Always uses `HttpUtils.writeJson(exchange, status, json)` to reply.

## app/Db.java

- Lazily loads DB configuration from `db-config.bat` (searched in multiple locations: next to the exe, `user.dir`, `MediLink-Portable/`, `app/`, `ProgramData/MediLink`, `%APPDATA%/MediLink`).

- Parses `set DB_URL=...`, `set DB_USER=...`, `set DB_PASS=...` lines.

- Exposes `Connection getConnection()` using `DriverManager.getConnection(url, user, pass)`.

- Methods:

  - `get(String key)`: returns cached config values (trimmed)

  - `refresh()`: clears cache for reload after changes

  - `getConfigSourcePath()`: path to the `db-config.bat` used

- Caveats: Provide sensible defaults for user/pass (empty string) and propagate errors up so `/health` can report.

## app/HttpUtils.java

- Utility methods for JSON/HTTP:

  - `writeJson(exchange, status, body)` sets `Content-Type`, sends headers and writes UTF-8 body.

  - `readRequestBody(exchange)` reads the entire request body.

  - `decode(s)` URL-decodes strings.

  - `esc(s)` and `safe(s)` sanitize strings for JSON embedding.

## app/AuthEndpoints.java

- Registers `POST /auth/login`.

- Accepts minimal JSON (name/password) parsed via simple string operations (no external JSON library to keep footprint low).

- Allows `Admin` / `Admin2025` as an offline fallback (no DB required).

- On success: returns `{ success: true, id_usuario, nombre, medico: {...}, role? }`.

- On failure: 401 with `{ success: false, error: ... }`.

- Notes: For production, consider password hashing and a JSON library.

## app/PacientesEndpoints.java

- Endpoints:

  - `GET /pacientes` with optional `q` and `limit` (1–200) → returns a JSON array of patients with the latest `activo` status.

  - `POST /pacientes/create` → validates required fields, inserts and returns `id_paciente`.

  - `GET /pacientes/{id}/diagnosticos` → validates patient, joins with doctor info and returns diagnostics.

  - `POST /pacientes/toggle-status` → toggles or sets explicit active/inactive state via `estado_entidad`, idempotent when requested state matches current.

- Implementation details:

  - Lightweight JSON parsing and building with careful escaping via `HttpUtils.esc/safe`.

  - SQL uses `PreparedStatement` and returns concise JSON.

## app/MedicosEndpoints.java

- Endpoints:

  - `GET /medicos` with `q` and `limit` → paginated list with `activo` flag.

  - `POST /medicos/create` → inserts doctor and auto-creates a linked user with generated credentials.

  - `POST /medicos/toggle-status` → same toggle semantics as patients.

- Notes:

  - Generated username/password are logged and returned; secure appropriately in production.

## app/UsuariosEndpoints.java

- Endpoints:

  - `GET /usuarios?medico_id=...&limit=...` → list users, joining doctor fields.

  - `POST /usuarios/create` → validates existence of `medico_id_medico` and inserts user.

## app/DiagnosticosEndpoints.java

- Endpoints:

  - `GET /diagnosticos?paciente_id=...&medico_id=...&limit=...` → filters and joins doctor/patient info.

  - `POST /diagnosticos/create` → validates FK existence, sets `fecha/hora` to now if missing, returns `id_diagnostico`.

  - `DELETE /diagnosticos/delete` (also accepts POST) → deletes by `id_diagnostico`.

- Notes:

  - Manual JSON parsing for minimal dependencies.

---

## Error handling & logging

- Consistent HTTP codes (200/201/400/401/404/405/500) and JSON error messages.

- System.out logs startup banner and API debug logs.

## Performance & scalability

- Suitable for single-node desktop deployments.

- Use `limit` parameters to cap result size; indexes exist on main FK columns.

## Security considerations
- Avoid committing `db-config.bat`.

- Consider credential hashing and role-based access for production.

- Sanitize JSON and SQL inputs (currently using PreparedStatements and basic string checks).
