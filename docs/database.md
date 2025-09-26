# Base de Datos

## Esquema (MySQL)

El script `sql/init-mysql.sql` crea las tablas:

- `medico(id_medico, nombres, apellidos, especialidad)`

- `paciente(id_paciente, nombres, apellidos, edad, genero?, correo?, direccion, tipo_documento, nro_documento, nro_contacto?)`

- `usuario(id_usuario, medico_id_medico FK→medico, nombre, contrasena)`

- `diagnostico(id_diagnostico, medico_id_medico FK→medico, paciente_id_paciente FK→paciente, observaciones, fecha, hora)`

- `estado_entidad(tipo_entidad, id_entidad, activo, fecha_cambio, uk_estado)` — almacena el estado activo/inactivo de médicos y pacientes.

Relaciones y claves foráneas protegen integridad; hay índices por FK y documentos.

## Migraciones

`sql/migration-fix-estado-entidad.sql`:

- Deduplica `estado_entidad` manteniendo el último registro por `(tipo_entidad, id_entidad)`.

- Asegura índice único `uk_estado`.

## Configuración de conexión

La app utiliza variables leídas desde `db-config.bat`:

- `DB_URL` — p. ej.: `jdbc:mysql://localhost:3306/hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`

- `DB_USER`

- `DB_PASS`

Ubicación detectada por `app.Db` (en orden):

1. JPackage app-path (junto al `.exe`), buscando `db-config.bat` en raíz o `app/`.

2. Directorio de trabajo (`user.dir`), con alternativas `MediLink-Portable/` y `app/`.

3. Carpeta actual canónica.

4. Rutas comunes: `%ProgramData%/MediLink/` o `%APPDATA%/MediLink/`.

`/health` ejecuta `SELECT 1` para verificar conectividad.

## Datos de ejemplo

El script `init-mysql.sql` incluye médicos, pacientes y un usuario de prueba para facilitar pruebas locales.
