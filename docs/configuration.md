# Configuración

## Base de Datos

- Ejecutar `configurar-bd.bat` (distribuido junto a la app) para definir `DB_URL`, `DB_USER`, `DB_PASS` en `db-config.bat`.

- El botón "Configurar BD" en `LoginFrame` abre la carpeta correspondiente en Windows Explorer y muestra instrucciones.

- La app detecta `db-config.bat` en múltiples ubicaciones (ver `database.md`).

## Puerto HTTP

- Variable de entorno opcional: `MEDILINK_HTTP_PORT`.

- Si no se define, el backend elige el primer puerto libre entre 8081 y 8099.

- La URL base se publica con `System.setProperty("medilink.baseUrl", ...)` para que la GUI la consuma.

## Íconos e imágenes

- Ubicar `icon.ico` y `logo.png` en la raíz o en `app/` (para app-image de jpackage). La GUI intentará ambas ubicaciones.

## Dependencias

- MySQL Connector/J (`lib/mysql-connector-j-*.jar`).

- Java 17+ recomendado.

## Salud del sistema

- Vista "Salud del Sistema" consulta `/health` para verificar configuración y conexión a BD.
