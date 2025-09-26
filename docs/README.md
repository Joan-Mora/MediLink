# MediLink — Documentación General

MediLink es un sistema de gestión hospitalaria que integra un servidor HTTP embebido (REST) y una interfaz de usuario de escritorio (Swing) en el mismo proceso de la JVM. Este diseño simplifica el despliegue (por ejemplo, con jpackage) y evita coordinar múltiples procesos.

Esta documentación está organizada en varios documentos especializados:

- Arquitectura general y componentes: consulte `architecture.md`.

- API Backend (endpoints REST): consulte `backend-api.md`.

- Esquema y migraciones de Base de Datos: consulte `database.md`.

- Interfaz gráfica (GUI) y flujos: consulte `gui.md`.

- Configuración (db-config.bat, puertos, etc.): consulte `configuration.md`.

- Desarrollo, build y empaquetado: consulte `development.md`.

## Estructura del proyecto (resumen)

- `src/app`: Backend HTTP embebido y utilidades de infraestructura.

- `src/gui`: Aplicación Swing (vistas, componentes y cliente API).

- `sql`: Scripts de base de datos (creación y migraciones).

- `tools`: Scripts para desarrollo y empaquetado en Windows.

- `lib`: Dependencias locales (p. ej., MySQL Connector/J).

- `MediLink-Portable`: Artefactos y plantilla de distribución portable.

Para detalles de interacción entre servidor y GUI, vea `architecture.md`.

---

© 2025 MediLink