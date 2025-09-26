MediLink - Empaquetado portable en Windows

Objetivo: obtener un portable que funcione en cualquier PC Windows, sin requerir Java instalado y con configuración de base de datos simple.

Flujo recomendado
- Compilar portable: ejecutar build-portable.bat
- Configurar BD: en MediLink-Portable, ejecutar configurar-bd.bat para generar db-config.bat
- Probar: start.bat o MediLink.exe
- Generar app-image con runtime embebido: PowerShell
  - tools/package-win.ps1 -BuildJlink
  - Esto crea MediLink-Portable/runtime (jlink) y MediLink-Portable/MediLink (app-image)
 - Generar instalador (requiere WiX en PATH):
   - Instala WiX Toolset 3.14+ o 4.x desde https://wixtoolset.org/ y agrega su carpeta bin al PATH (light.exe/candle.exe para v3 o wix.exe para v4)
   - tools/package-win.ps1 -BuildJlink -Type exe  (instalador EXE por usuario)
   - tools/package-win.ps1 -BuildJlink -Type msi  (instalador MSI por usuario)

Notas
- El lanzador Java único app.AppLauncher arranca servidor+GUI en el mismo proceso, lo que simplifica el empaquetado (jpackage) y evita problemas de PATH o antivirus bloqueando múltiples procesos.
- db-config.bat es la fuente de verdad para DB_URL/DB_USER/DB_PASS. El código Java lo lee directamente y el launcher C# también lo carga en el entorno.
- Para máxima compatibilidad, use un JDK LTS (21) y genere runtime con jlink. El app-image de jpackage funciona sin Java instalado.
 - El launcher portable (start.bat / MediLink.exe) prioriza abrir el app-image MediLink/MediLink.exe si existe, y si no, ejecuta app.AppLauncher con el runtime embebido (runtime\bin\java.exe) o con el Java del sistema.
