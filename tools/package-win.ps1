param(
  [string]$AppName = 'MediLink',
  [string]$AppVersion = '1.0.0',
  [string]$Vendor = 'MediLink',
  [ValidateSet('app-image','msi','exe')]
  [string]$Type = 'app-image',
  [string]$JdkHome,
  [switch]$BuildJlink,
  [switch]$Sign,
  [string]$CertPfx,
  [string]$CertPass,
  [string]$TimestampUrl = 'http://timestamp.digicert.com',
  [string]$SignTool
)

$ErrorActionPreference = 'Stop'
Set-Location "$PSScriptRoot\.."

$portable = Join-Path (Get-Location) 'MediLink-Portable'
if (-not (Test-Path $portable)) { throw "No existe MediLink-Portable. Ejecute build-portable.bat primero" }

if (-not $JdkHome) {
  $JdkHome = $env:JAVA_HOME
}
if (-not $JdkHome -or -not (Test-Path (Join-Path $JdkHome 'bin\java.exe'))) {
  throw "JDK no encontrado. Defina -JdkHome o JAVA_HOME (JDK 21 recomendado)."
}

$jlink = Join-Path $JdkHome 'bin\jlink.exe'
$jpackage = Join-Path $JdkHome 'bin\jpackage.exe'

$mods = @(
  'java.base',
  'java.desktop',
  'java.datatransfer',
  'java.logging',
  'java.sql',
  'jdk.httpserver',
  'jdk.unsupported',
  # Extras para JDBC/MySQL y TLS
  'jdk.crypto.ec',       # TLS EC provider (MySQL SSL por defecto PREFERRED)
  'java.naming',         # Algunos drivers usan JNDI/DNS ops
  'java.xml'             # Utilidades XML usadas por drivers o formatos
)

$modsCsv = ($mods | Sort-Object -Unique) -join ','
$imageOut = Join-Path $portable 'runtime'
# Intenta cerrar procesos que bloqueen una carpeta (ej. MediLink.exe ejecutándose)
function Stop-AppLocks {
  param([string]$path)
  try {
    $procs = Get-Process -ErrorAction SilentlyContinue | Where-Object {
      try { $_.MainModule.FileName -like (Join-Path $path '*') } catch { $false }
    }
    foreach ($p in $procs) {
      try { Write-Warning "Deteniendo proceso bloqueando '$path': $($p.ProcessName) ($($p.Id))"; Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue } catch {}
    }
    # Intentar también por nombre conocido
    try { Stop-Process -Name MediLink -Force -ErrorAction SilentlyContinue } catch {}
  } catch {}
}

# Preparar carpeta de trabajo ASCII (evita problemas de Unicode en jpackage/jlink)
$workRoot = Join-Path $env:TEMP 'MediLinkPkg'
if (Test-Path $workRoot) { Remove-Item -Recurse -Force $workRoot }
New-Item -ItemType Directory -Path $workRoot | Out-Null
$workPortable = Join-Path $workRoot 'MediLink-Portable'
Copy-Item -Recurse -Force $portable $workPortable
if ($BuildJlink) {
  if (Test-Path $imageOut) { Remove-Item -Recurse -Force $imageOut }
  $workRuntime = Join-Path $workPortable 'runtime'
  if (Test-Path $workRuntime) { Remove-Item -Recurse -Force $workRuntime }
  & $jlink --no-header-files --no-man-pages --strip-debug --compress=2 `
    --add-modules $modsCsv `
    --output $workRuntime
}

# Generar app image con jpackage (sin instalador; portable)
$runtimeArgs = @()
if ($BuildJlink) {
  # Ejecutamos jpackage dentro de $workPortable, así que referenciamos 'runtime' relativa
  $runtimeArgs = @('--runtime-image','runtime')
}

$classPath = 'lib/*'
<# El icono puede causar errores en algunas versiones de jpackage; se omite por compatibilidad #>

Push-Location $workPortable
  # Si existe una app-image previa en la carpeta temporal (p.ej., por copiado desde el portable), eliminarla
  $existingAppImage = Join-Path $workPortable $AppName
  if (Test-Path $existingAppImage) { Remove-Item -Recurse -Force $existingAppImage }
  # Empaquetar clases en un JAR simple (sin manifest main) y usar --main-class
  $jar = Join-Path $JdkHome 'bin\jar.exe'
  if (-not (Test-Path $jar)) { throw "No se encontro jar.exe en el JDK." }
  if (Test-Path 'app.jar') { Remove-Item 'app.jar' -Force }
  # Construir manifest con Main-Class y Class-Path apuntando a jars de lib
  $libJars = @()
  if (Test-Path 'lib') {
    $libJars = Get-ChildItem -Path 'lib' -Filter '*.jar' | ForEach-Object { $_.Name }
  }
  $cpLine = ($libJars -join ' ')
  $manifest = @()
  $manifest += 'Manifest-Version: 1.0'
  $manifest += 'Main-Class: app.AppLauncher'
  if ($cpLine -and $cpLine.Length -gt 0) { $manifest += ('Class-Path: ' + $cpLine) }
  Set-Content -Path 'MANIFEST.MF' -Value $manifest -Encoding ASCII
  & $jar cfm 'app.jar' 'MANIFEST.MF' -C 'classes' .
  Remove-Item 'MANIFEST.MF' -Force

  # Preparar carpeta bundle con app.jar y dependencias para que jpackage las coloque en app/ (junto a app.jar)
  $bundle = Join-Path $workPortable 'bundle'
  if (Test-Path $bundle) { Remove-Item -Recurse -Force $bundle }
  New-Item -ItemType Directory -Path $bundle | Out-Null
  Copy-Item 'app.jar' -Destination (Join-Path $bundle 'app.jar') -Force
  if (Test-Path 'lib') {
    $libFiles = Get-ChildItem -Path 'lib' -Filter '*.jar'
    foreach ($f in $libFiles) {
      Copy-Item -LiteralPath $f.FullName -Destination (Join-Path $bundle $f.Name) -Force
    }
  }
  # No incluimos logo/iconos en el bundle para evitar que queden dentro de app/

  $iconArg = @()
  $iconPath = Join-Path $workPortable 'icon.ico'
  if (Test-Path $iconPath) { 
    # Verificar que el icono sea válido para jpackage
    $iconArg = @('--icon', $iconPath)
  }

# Generar según el tipo solicitado
if ($Type -eq 'app-image') {
  # Solo generar app-image
  & $jpackage --name $AppName --app-version $AppVersion --type app-image `
    --input $bundle `
    --main-jar app.jar `
    --dest . `
    --vendor $Vendor `
    @iconArg `
    @runtimeArgs
  if ($LASTEXITCODE -ne 0) { throw "jpackage fallo (app-image) con codigo $LASTEXITCODE" }
  
  Write-Host "App image generado en carpeta temporal"

  # Copiar app-image generado al portable original
  $appSrc = Join-Path $workPortable $AppName
  if (Test-Path $appSrc) {
    $appDst = Join-Path $portable $AppName
    $inPlace = $false
    if (Test-Path $appDst) {
      try {
        Stop-AppLocks -path $appDst
        Remove-Item -Recurse -Force $appDst
      } catch {
        Write-Warning "No se pudo eliminar '$appDst' (en uso). Intentando reemplazo in-place."
        $inPlace = $true
      }
    }
    if ($inPlace) {
      # Copia in-place con robocopy para evitar fallar por archivos bloqueados
      $rc = Start-Process -FilePath robocopy -ArgumentList @("$appSrc","$appDst","/E","/R:1","/W:1","/NFL","/NDL","/NJH","/NJS","/NC","/NS") -Wait -PassThru
      $code = $rc.ExitCode
      if ($code -ge 8) { Write-Warning "robocopy fallo con codigo $code" } else { Write-Host "App image actualizado in-place en MediLink-Portable\\$AppName" }
    } else {
      Copy-Item -Recurse -Force $appSrc $appDst
      Write-Host "App image copiado a MediLink-Portable\\$AppName"
    }
  }

  # Copiar archivos auxiliares al app-image para que se resuelvan via CWD
  $appDir = Join-Path $portable $AppName
  if (Test-Path $appDir) {
    $copyIf = @('db-config.bat','configurar-bd.bat','icon.ico','icon.png','logo.png','Instrucciones.md','Respaldo-Limpiar-BD.bat')
    foreach ($f in $copyIf) {
      $src = Join-Path $portable $f
      if (Test-Path $src) { Copy-Item $src -Destination (Join-Path $appDir $f) -Force }
    }
    # Copiar recursos en minúsculas y mayúsculas si existen
    foreach ($dir in @('resources','Resources','sql')) {
      $srcd = Join-Path $portable $dir
      if (Test-Path $srcd) { Copy-Item $srcd -Destination (Join-Path $appDir $dir) -Recurse -Force }
    }
    # Crear Backups/backups vacíos
    foreach ($bdir in @('Backups','backups')) {
      $dstB = Join-Path $appDir $bdir
      if (Test-Path $dstB) { Get-ChildItem -Path $dstB -Recurse -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue }
      if (-not (Test-Path $dstB)) { New-Item -ItemType Directory -Path $dstB -Force | Out-Null }
    }
    Write-Host "Archivos de configuracion y recursos copiados a $AppName/"
  }
} else {
  # Primero, generar un app-image en la carpeta de trabajo
  & $jpackage --name $AppName --app-version $AppVersion --type app-image `
    --input $bundle `
    --main-jar app.jar `
    --dest . `
    --vendor $Vendor `
    @iconArg `
    @runtimeArgs
  if ($LASTEXITCODE -ne 0) { throw "jpackage fallo (app-image previo a installer) con codigo $LASTEXITCODE" }

  # Copiamos los archivos requeridos a la RAIZ del app-image para que queden junto al .exe
  $preApp = Join-Path $workPortable $AppName
  if (Test-Path $preApp) {
    $copyIf2 = @('db-config.bat','configurar-bd.bat','icon.ico','icon.png','logo.png','Instrucciones.md','Respaldo-Limpiar-BD.bat')
    foreach ($f in $copyIf2) {
      $src = Join-Path $workPortable $f
      if (Test-Path $src) { Copy-Item $src -Destination (Join-Path $preApp $f) -Force }
    }
    foreach ($dir in @('resources','Resources','sql')) {
      $srcd = Join-Path $workPortable $dir
      if (Test-Path $srcd) { Copy-Item $srcd -Destination (Join-Path $preApp $dir) -Recurse -Force }
    }
    foreach ($bdir in @('Backups','backups')) {
      $dstB = Join-Path $preApp $bdir
      if (Test-Path $dstB) { Get-ChildItem -Path $dstB -Recurse -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue }
      if (-not (Test-Path $dstB)) { New-Item -ItemType Directory -Path $dstB -Force | Out-Null }
    }
    Write-Host "App-image previo listo con archivos en raiz para instalador"
  }

  # Parámetros específicos según el tipo de instalador
  $installerArgs = @()
  if ($Type -eq 'msi') {
    $installerArgs += @('--win-upgrade-uuid', '12345678-1234-5678-9ABC-123456789012')
    $installerArgs += @('--win-per-user-install')
  } else {
    $installerArgs += @('--win-dir-chooser')
    $installerArgs += @('--win-per-user-install')
  }
  
  $dist = Join-Path $workPortable 'dist'
  if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }
  New-Item -ItemType Directory -Path $dist | Out-Null
  
  # Crear el instalador a partir del app-image preparado (esto preserva la estructura de raiz)
  & $jpackage --name $AppName --app-version $AppVersion --type $Type `
    --app-image $preApp `
    --dest $dist `
    --vendor $Vendor `
    --win-shortcut `
    --win-menu `
    --win-menu-group $Vendor `
    @iconArg `
    @installerArgs
  if ($LASTEXITCODE -ne 0) { throw "jpackage fallo (installer) con codigo $LASTEXITCODE" }

  # Copiar instalador a MediLink-Portable\dist
  $dstDist = Join-Path $portable 'dist'
  if (-not (Test-Path $dstDist)) { New-Item -ItemType Directory -Path $dstDist | Out-Null }
  $pattern = if ($Type -eq 'msi') { '*.msi' } else { '*.exe' }
  $installer = Get-ChildItem -Path $dist -Filter $pattern | Select-Object -First 1
  if ($null -ne $installer) {
    # Renombrar instalador para cumplir el nombre solicitado
    $customName = if ($Type -eq 'msi') { 'MediLink 1.0.0 - Installer.msi' } else { 'MediLink 1.0.0 - Installer.exe' }
    $outInstaller = Join-Path $dstDist $customName
    # Intentar liberar archivo de destino si existe
    if (Test-Path $outInstaller) {
      try {
        Stop-AppLocks -path (Split-Path $outInstaller -Parent)
        Remove-Item $outInstaller -Force -ErrorAction SilentlyContinue
      } catch {}
    }
    try {
      Copy-Item $installer.FullName -Destination $outInstaller -Force
      Write-Host "Instalador $Type generado: $outInstaller"
    } catch {
      Write-Warning "No se pudo copiar instalador (en uso): $($_.Exception.Message)"
      Write-Host "Instalador disponible en: $($installer.FullName)"
    }
  } else {
    Write-Warning "No se encontro instalador generado por jpackage."
  }
}
Pop-Location

# Firmado de binarios opcional
function Find-SignTool {
  param([string]$Hint)
  if ($Hint -and (Test-Path $Hint)) { return $Hint }
  try { $p = (Get-Command signtool.exe -ErrorAction Stop).Source; if ($p) { return $p } } catch {}
  $kits = @(
    Join-Path ${env:ProgramFiles(x86)} 'Windows Kits\10\bin',
    Join-Path ${env:ProgramFiles} 'Windows Kits\10\bin'
  )
  foreach ($k in $kits) {
    if (Test-Path $k) {
      $candidates = Get-ChildItem -Path $k -Recurse -Filter signtool.exe -ErrorAction SilentlyContinue | Sort-Object FullName -Descending
      if ($candidates -and $candidates[0]) { return $candidates[0].FullName }
    }
  }
  return $null
}

function Sign-Files {
  param(
    [string]$Tool,
    [string]$Pfx,
    [string]$Pass,
    [string]$TsUrl,
    [string[]]$Files
  )
  if (-not $Tool) { throw "signtool.exe no encontrado. Proporcione -SignTool o instale Windows SDK." }
  if (-not (Test-Path $Pfx)) { throw "No se encontro el certificado PFX: $Pfx" }
  foreach ($f in $Files) {
    if (-not (Test-Path $f)) { Write-Warning "No existe: $f"; continue }
    Write-Host "Firmando $f"
    & $Tool sign /f "$Pfx" /p "$Pass" /fd SHA256 /td SHA256 /tr "$TsUrl" "$f"
  }
}

if ($Sign) {
  $tool = Find-SignTool -Hint $SignTool
  $toSign = @()
  $exe1 = Join-Path $portable (Join-Path $AppName ("$AppName.exe"))
  if (Test-Path $exe1) { $toSign += $exe1 }
  $exe2 = Join-Path $portable 'MediLink.exe'
  if (Test-Path $exe2) { $toSign += $exe2 }
  if ($Type -ne 'app-image') {
    $dstDist = Join-Path $portable 'dist'
    if (Test-Path $dstDist) {
      $pattern = if ($Type -eq 'msi') { '*.msi' } else { '*.exe' }
      $inst = Get-ChildItem -Path $dstDist -Filter $pattern | Select-Object -First 1
      if ($inst) { $toSign += $inst.FullName }
    }
  }
  if ($toSign.Count -gt 0) {
    Sign-Files -Tool $tool -Pfx $CertPfx -Pass $CertPass -TsUrl $TimestampUrl -Files $toSign
  } else {
    Write-Warning "No se encontraron binarios para firmar."
  }
}

# Limpiar temporal
Remove-Item -Recurse -Force $workRoot
