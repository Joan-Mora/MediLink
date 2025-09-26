# API Backend (REST)

Base URL por defecto: `http://localhost:8081` (auto-detectable por GUI).

## Health

- GET `/health`

- Respuesta: `{ "config": "<ruta db-config.bat>", "url": "jdbc:mysql://...", "db": "connected"|"error", "ok": true|false }`

## Autenticación

- POST `/auth/login`

- Body: `{ "nombre": "usuario", "contrasena": "secreta" }`

- Reglas especiales: credenciales `Admin`/`Admin2025` válidas sin BD.

- Respuesta éxito: `{ success:true, id_usuario, nombre, medico:{...}, role? }`

## Médicos

- GET `/medicos?q=<texto>&limit=<1-200>`

  - Respuesta: `[ { id_medico, nombres, apellidos, especialidad, activo } ]`

- POST `/medicos/create`

  - Body: `{ "nombres":"...", "apellidos":"...", "especialidad":"..." }`

  - Crea además un usuario vinculado automáticamente.

- POST `/medicos/toggle-status`

  - Body: `{ "id_medico": 1, "activo": true|false }` o `{ "id_medico":1, "accion":"activar|inactivar|desactivar" }`

  - Respuesta incluye `changed` y `message`.

## Pacientes

- GET `/pacientes?q=<texto>&limit=<1-200>`

  - `[ { id_paciente, nombres, apellidos, edad, genero?, correo?, direccion, tipo_documento, nro_documento, nro_contacto?, activo } ]`

- POST `/pacientes/create`

  - Body requerido: `nombres, apellidos, edad, direccion, tipo_documento, nro_documento`. Campos opcionales: `genero, correo, nro_contacto`.

- GET `/pacientes/{id}/diagnosticos`

  - Historial de diagnósticos del paciente.

- POST `/pacientes/toggle-status`

  - Igual semántica que médicos, para tipo_entidad `paciente`.

## Usuarios

- GET `/usuarios?medico_id=<id>&limit=<1-200>`

  - `[ { id_usuario, medico_id_medico, nombre, contrasena, medico:{nombres, apellidos, especialidad} } ]`

- POST `/usuarios/create`

  - Body: `{ "nombre":"...", "contrasena":"...", "medico_id_medico": <id> }`

## Diagnósticos

- GET `/diagnosticos?paciente_id=<id>&medico_id=<id>&limit=<1-200>`

  - `[ { id_diagnostico, medico_id_medico, paciente_id_paciente, observaciones, fecha, hora, medico:{...}, paciente:{...} } ]`

- POST `/diagnosticos/create`

  - Body: `{ "medico_id_medico": <id>, "paciente_id_paciente": <id>, "observaciones":"..." }`

- DELETE `/diagnosticos/delete`

  - Body: `{ "id_diagnostico": <id> }` (también acepta POST por compatibilidad)

## Ejemplos (PowerShell)

```powershell

Invoke-RestMethod -Uri "http://localhost:8081/medicos" -Method GET | Format-List

Invoke-RestMethod -Uri "http://localhost:8081/pacientes" -Method GET | Format-List

Invoke-RestMethod -Uri "http://localhost:8081/usuarios" -Method GET | Format-List

Invoke-RestMethod -Uri "http://localhost:8081/diagnosticos" -Method GET | Format-List

# Crear paciente

Invoke-RestMethod -Uri "http://localhost:8081/pacientes/create" -Method POST -Body '{"nombres":"Test","apellidos":"User","edad":30,"direccion":"Calle Test","tipo_documento":"CC","nro_documento":"99999999"}' -ContentType "application/json"
```