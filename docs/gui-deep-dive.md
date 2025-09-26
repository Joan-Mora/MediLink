# GUI Deep Dive — MediLink (Swing)

This document explains the GUI codebase in depth, file by file and block by block, and can be exported to PDF.

## Contents
- Overview (login, main window, navigation, background tasks)
- Files walkthrough (MediLinkGUI, LoginFrame, views, components, models, util)
- JSON handling and API calls
- Threading model (EDT vs background threads)
- UX patterns and notifications

## Overview
- The GUI starts at `gui.views.LoginFrame` and opens `gui.MediLinkGUI` after login.
- Navigation is handled through a sidebar with sections: Dashboard, Stats, Patients, Doctors, Diagnostics, Users, Health, Credits.
- API calls are done through `gui.models.ApiClient`.

---

## gui/MediLinkGUI.java
- JFrame with BorderLayout: left sidebar (JScrollPane), center content panel, bottom status bar.
- Color palette and constants for consistent theming.
- Sidebar: buttons mapped to actions; active item highlighting; hover/active styles.
- Views switching: `setContent(Component)` clears and adds the selected view panel.
- Handlers for actions:
  - `showDashboard`, `showStats`, `showPacientes`, `showMedicos`, `showDiagnosticos`, `showUsuarios`, `checkConnection`, `showCreditos`, plus dialogs for creating entities and toggling states.
- Dashboard/stats counters:
  - Uses `countObjects` and `countActivosInactivos` to compute metrics from JSON payloads.
- Status bar: shows connection/user info and version (`app.AppInfo.VERSION`).
- Threading: long-running or network operations are wrapped in background threads, UI updates on EDT.

## gui/views/LoginFrame.java
- JFrame with gradient background and a central card for login form and logo.
- Icon loading: attempts `icon.ico` → `icon.png` from both root and `app/` directory (for app-image layout).
- Fields: username/password, buttons: Login, Exit, Configure DB (green).
- Health check: background thread performs `/health` with retries and offers options (continue/retry/exit) if offline.
- Login flow:
  - Offline bypass if `Admin/Admin2025`.
  - Otherwise posts to `/auth/login`, extracts a display name from JSON.
  - On success opens `MediLinkGUI(displayName)`.
- "Configure DB": opens Windows Explorer selecting `configurar-bd.bat` if present, or the folder otherwise; shows guidance.
- Folder resolution: tries current working dir, jar location, parent dirs; falls back to cwd.

## gui/models/ApiClient.java
- Auto-discovers `baseUrl` from system property `medilink.baseUrl` or probes `/health` on 8081–8099.
- `get/post/delete` using `HttpURLConnection` with timeouts and debug logs.
- Utility methods to escape JSON and quickly build simple JSON payloads.

## gui/util/IcoUtil.java
- Reads `.ico` files extracting the best image (or all images) including embedded PNGs.
- Provides images of multiple sizes so Swing can set a full icon list for Windows/Taskbar.

## gui/components/Notificaciones.java
- Centralized success/error/status dialogs for operations (creation/toggle/delete).
- Improves consistency across views.

## Views (gui/views/*.java)
- `DashboardView`: overview counters.
- `EstadisticasView`: aggregated metrics with active/inactive breakdown.
- `PacientesView`, `MedicosView`, `DiagnosticosView`, `UsuariosView`: list + actions.
- `AddPacienteView`, `AddMedicoView`, `AddDiagnosticoView`: dialogs for POST create flows.
- `TogglePacientesView`, `ToggleMedicosView`: batch state changes.
- `DeleteDiagnosticosView`: batch delete of diagnostics.
- `HealthView`: system health (uses `/health`).
- `Creditos`: static credits and acknowledgements.

## JSON parsing & data handling
- Minimalistic approach: string-based extraction using regex/splits.
- Escaping via `ApiClient.escapeJson` and `HttpUtils.esc/safe` on the backend.
- Consider a JSON library for larger projects; here we keep footprint minimal.

## Threading model
- All Swing UI updates must run on EDT. Background tasks use `new Thread(() -> {...}).start()` or similar.
- Network requests wrapped in background threads; errors reported via dialogs or status bar.

## UX patterns
- Consistent color palette and button states (hover/active).
- Status bar feedback on operations.
- Notifications for batch operations (activate/deactivate/delete).

## Tips & caveats
- Ensure backend started (via AppLauncher) or use Admin/Admin2025 when configuring DB offline.
- Place `icon.ico`/`logo.png` at root or in `app/` within app-image.
- For packaging, prefer `app.AppLauncher` as the main class.
