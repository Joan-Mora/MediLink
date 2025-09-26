# Runbook / Playbook de Operaciones

Este documento resume tareas comunes de operación, validación y solución de problemas.

## Arranque local (desarrollo)

- Ejecutar `app.AppLauncher` (recomendado) para iniciar servidor + GUI en un mismo proceso.

- Alternativas:

  - Solo servidor: `app.Main`.

  - Solo GUI: `gui.views.LoginFrame` (requiere servidor activo o usar bypass Admin/Admin2025).

## Verificar salud

- GUI: Menú "Salud del Sistema".

- API: `GET /health` — Debe responder `{ "ok": true }` y `"db":"connected"`.

## Credenciales de emergencia (offline)

- Usuario: `Admin`

- Contraseña: `Admin2025`

- Útil cuando no existe conexión a BD.

## Configurar conexión de BD

1. Abrir la app y en login pulsar "Configurar BD" (abre la carpeta en Explorer).

2. Ejecutar `configurar-bd.bat` y completar `DB_URL`, `DB_USER`, `DB_PASS`.

3. Cerrar y reabrir MediLink.

Valores típicos (`db-config.bat`):
```
set DB_URL=jdbc:mysql://localhost:3306/hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

set DB_USER=root

set DB_PASS=secret
```

## Inicializar base de datos

- Ejecutar `sql/init-mysql.sql` en MySQL.

- Verificar tablas y datos de ejemplo.

## Endpoints clave para diagnóstico

- `GET /health` — Salud general y origen de configuración.

- `GET /pacientes`, `GET /medicos`, `GET /usuarios`, `GET /diagnosticos` — Validar lecturas básicas.

## Re-generar binarios (guía general)

- Asegurar JDK 17+ y MySQL Connector/J en `lib/`.

- Apuntar clase principal a `app.AppLauncher` en el empaquetado.

- Incluir recursos: `icon.ico`, `logo.png`, `db-config.bat`.

- Ver `docs/development.md` para detalles de empaquetado (jpackage/app-image/portable).

## Solución de problemas

- No conecta a BD: revisar `db-config.bat`, permisos de MySQL y red; validar con `/health`.

- GUI no encuentra servidor: confirmar que `app.Main` está corriendo; revisar puertos 8081-8099; usar "Salud del Sistema".

- Íconos no cargan: colocar `icon.ico` o `logo.png` en raíz o en `app/` (estructura app-image).

## Respaldo/Limpieza (portable)

- Usar scripts en `MediLink-Portable/` (si aplica) para respaldos y limpieza de BD.
