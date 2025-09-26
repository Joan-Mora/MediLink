# Interfaz Gráfica (GUI)

## Login

- Clase: `gui.views.LoginFrame`.

- Funciones:

  - Autenticación vía `/auth/login`.

  - Bypass offline: `Admin` / `Admin2025`.

  - Botón "Configurar BD": abre la carpeta de instalación (o selecciona `configurar-bd.bat` en Explorer) y muestra instrucciones.

## Aplicación principal

- Clase: `gui.MediLinkGUI`.

- Navegación lateral (sidebar) a vistas:

  - Dashboard, Estadísticas.

  - Pacientes (listar/crear, ver activos/inactivos y toggle).

  - Médicos (listar/crear, toggle activo/inactivo, crea usuario automáticamente al crear médico).

  - Diagnósticos (listar, crear y eliminar).

  - Usuarios (listar/crear).

  - Salud del sistema (consulta `/health`).

  - Créditos.

- Barra de estado con versión y copyright.

## Cliente API

- Clase: `gui.models.ApiClient`.

  - Resuelve `baseUrl` por propiedad de sistema `medilink.baseUrl` o probando `/health` en puertos 8081-8099.

  - Métodos `get`, `post`, `delete` con `HttpURLConnection`.

## Utilidades

- Íconos: `gui.util.IcoUtil` lee `icon.ico` (múltiples tamaños) y `logo.png`.

- Notificaciones: `gui.components.Notificaciones` centraliza diálogos (éxito, error, estado).

## Buenas prácticas en la UI

- Acciones de red en hilos de fondo; actualización de UI en EDT.

- Manejo de errores con mensajes claros.

- Contadores y métricas en Dashboard/Estadísticas usando los endpoints.
