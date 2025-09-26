# Mapa del Código (por archivo)

Este documento describe los archivos principales, su responsabilidad y cómo se relacionan entre sí.

## Backend (src/app)

- `AppInfo.java`: Metadatos de la app (NAME, VERSION, COPYRIGHT).

- `AppLauncher.java`: Punto de entrada unificado; arranca el servidor (hilo daemon) y la GUI (EDT) en el mismo proceso.

- `Main.java`: Inicializa `HttpServer`, resuelve puerto, registra endpoints y publica `medilink.baseUrl`; incluye `/health`.

- `Db.java`: Carga perezosa de configuración de BD desde `db-config.bat`; expone `getConnection()`; busca el archivo en múltiples ubicaciones.

- `HttpUtils.java`: Utilidades HTTP (lectura cuerpo, respuesta JSON, escapes y decodificación).

- `AuthEndpoints.java`: Endpoint de login (`/auth/login`); maneja Admin/Admin2025 como bypass offline.

- `PacientesEndpoints.java`: Endpoints de pacientes (listar/buscar, crear, ver diagnósticos por paciente, toggle activo/inactivo).

- `MedicosEndpoints.java`: Endpoints de médicos (listar/buscar, crear —incluye creación automática de usuario—, toggle activo/inactivo).

- `UsuariosEndpoints.java`: Endpoints de usuarios (listar y crear usuarios vinculados a médicos).

- `DiagnosticosEndpoints.java`: Endpoints de diagnósticos (listar/filtrar, crear y eliminar).

## GUI (src/gui)

- `MediLinkGUI.java`: Ventana principal post-login; construye sidebar, estatus, maneja navegación y renderiza vistas; coordina llamadas a `ApiClient`.

### GUI/Models (src/gui/models)

- `ApiClient.java`: Cliente HTTP para interactuar con el backend; autodetección de `baseUrl`; métodos `get/post/delete` y utilidades JSON.

### GUI/Util (src/gui/util)

- `IcoUtil.java`: Lector de `.ico` (múltiples tamaños/formatos) y soporte para PNG dentro de ICO; usado para iconografía de la app.

### GUI/Components (src/gui/components)

- `Notificaciones.java`: Utilidad centralizada para mostrar cuadros de diálogo de éxito/error/estado en la GUI.

### GUI/Views (src/gui/views)

- `LoginFrame.java`: Ventana de inicio de sesión; realiza login contra `/auth/login`; bypass Admin/Admin2025; botón "Configurar BD" abre carpeta de instalación y guía al usuario.

- `DashboardView.java`: Vista principal con contadores globales (pacientes, médicos, diagnósticos, usuarios).

- `EstadisticasView.java`: Métricas/estadísticas agregadas (activos/inactivos, etc.).

- `PacientesView.java`: Listado de pacientes; muestra estado (activo/inactivo) y permite gestionar/fluir a otras acciones.

- `MedicosView.java`: Listado de médicos con estado y controles relacionados.

- `DiagnosticosView.java`: Listado de diagnósticos (con información de médico y paciente asociados).

- `UsuariosView.java`: Listado de usuarios con vínculo al médico correspondiente.

- `AddPacienteView.java`: Diálogo/formulario para crear pacientes (POST `/pacientes/create`).

- `AddMedicoView.java`: Diálogo/formulario para crear médicos (POST `/medicos/create`); muestra credenciales generadas.

- `AddDiagnosticoView.java`: Diálogo/formulario para crear diagnósticos (POST `/diagnosticos/create`).

- `TogglePacientesView.java`: Interfaz para activar/desactivar pacientes (POST `/pacientes/toggle-status`).

- `ToggleMedicosView.java`: Interfaz para activar/desactivar médicos (POST `/medicos/toggle-status`).

- `DeleteDiagnosticosView.java`: Interfaz para eliminar diagnósticos (DELETE `/diagnosticos/delete`).

- `HealthView.java`: Muestra el estado de salud del sistema (`/health`).

- `Creditos.java`: Información de créditos/licencias.

- `LoginDialog.java`: (Si se usa) variante de login modal.

## SQL (sql)

- `init-mysql.sql`: Crea esquema completo (medico, paciente, usuario, diagnostico, estado_entidad) e inserta datos de prueba.

- `migration-fix-estado-entidad.sql`: Limpia duplicados y asegura índice único en `estado_entidad`.

## Tools (tools)

- `configurar-bd.bat`: Script de configuración para generar/editar `db-config.bat` con `DB_URL/DB_USER/DB_PASS`.

- `format-java.ps1`: Formateo de archivos Java (tarea de VS Code: "Format Java: 1 blank line").

- `make-exe.ps1`, `package-win.ps1`: Scripts de empaquetado en Windows (app-image/portable/instalador según configuración del proyecto).

## Distribución (MediLink-Portable)

- Contiene plantilla de distribución portable con `MediLink.exe`, `db-config.bat`, scripts utilitarios y runtime/recursos.

## Recursos (raíz/resources)

- `image.png`, `whatsapp.png`: Imágenes usadas en la GUI o empaquetado.

---

Sugerencia: combine este mapa con `docs/architecture.md` para entender el flujo end-to-end del sistema.