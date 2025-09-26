# Arquitectura

## Visión general

- Proceso único JVM con dos componentes:

  - Servidor HTTP embebido (`com.sun.net.httpserver.HttpServer`) iniciado por `app.Main`.

  - Interfaz gráfica Swing iniciada por `gui.views.LoginFrame`.

- Lanzamiento unificado mediante `app.AppLauncher`:

  - Arranca `app.Main` en un hilo daemon llamado `medilink-server`.

  - Lanza la GUI en el Event Dispatch Thread (EDT) con `SwingUtilities.invokeLater`.

- La GUI consume la API local vía `gui.models.ApiClient`.

## Flujo de arranque

1. `AppLauncher.main` crea hilo daemon y ejecuta `app.Main.main` (servidor HTTP).

2. `Main`:

   - Resuelve el puerto (env `MEDILINK_HTTP_PORT` o primer libre 8081-8099).

   - Inicializa `HttpServer` y registra endpoints (Pacientes/Medicos/Usuarios/Diagnosticos/Auth) y `/health`.

   - Publica `System.setProperty("medilink.baseUrl", "http://localhost:"+port)` para la GUI.

3. `AppLauncher` inicia `LoginFrame` en EDT.

4. `ApiClient` detecta el `baseUrl` por propiedad de sistema o probando `/health` en 8081-8099.

## Backend

- Endpoints registrados desde clases `*Endpoints.register(server)`.

- Handlers implementan `HttpHandler#handle` y responden JSON con `HttpUtils.writeJson`.

- Acceso a BD mediante `Db.getConnection()` que lee `db-config.bat` para `DB_URL`, `DB_USER`, `DB_PASS`.

- Handler `/health` ejecuta `SELECT 1` y devuelve `{"ok":true}` cuando hay conectividad.

## GUI

- `gui.MediLinkGUI` es el contenedor principal tras autenticación.

- `gui.views.LoginFrame` ofrece login y botón "Configurar BD" que abre el folder de instalación para ejecutar `configurar-bd.bat`.

- Vistas principales en `gui/views`: Dashboard, Pacientes, Médicos, Diagnósticos, Usuarios, Salud, etc.

- `Notificaciones` centraliza diálogos de resultado.

## Concurrencia

- Servidor en hilo daemon; GUI en EDT.

- Llamadas HTTP realizadas en hilos de fondo para no bloquear la UI.

## Gestión de errores

- Backend envía códigos HTTP apropiados (200/201/400/401/404/405/500).

- GUI captura excepciones, muestra mensajes y mantiene la app responsiva.

## Empaquetado

- El diseño "proceso único" facilita `jpackage` creando un ejecutable que arranca server+GUI.

- Íconos (`icon.ico`, `logo.png`) se cargan tanto desde raíz como desde `app/` (estructura app-image).
