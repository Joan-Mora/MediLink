param(
  [string]$ProductVersion = '1.0.0',
  [string]$FileVersion = '1.0.0.0',
  [string]$ProductName = 'MediLink',
  [string]$Company = 'MediLink',
  [string]$Description = 'MediLink Portable Launcher'
)

$ErrorActionPreference = 'Stop'

# Generar fuente C# con metadatos de versión
$asm = @"
using System.Reflection;
[assembly: AssemblyTitle("$ProductName")]
[assembly: AssemblyDescription("$Description")]
[assembly: AssemblyCompany("$Company")]
[assembly: AssemblyProduct("$ProductName")]
[assembly: AssemblyVersion("$FileVersion")]
[assembly: AssemblyFileVersion("$FileVersion")]
[assembly: AssemblyInformationalVersion("$ProductVersion")]
"@

$cs = @"
using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;
class Program {
  static void SetEnvFromConfig(string path) {
    try {
      if (!File.Exists(path)) return;
      foreach (var line in File.ReadAllLines(path)) {
        var s = line.Trim();
        if (s.StartsWith("set ", StringComparison.OrdinalIgnoreCase)) {
          var kv = s.Substring(4);
          var idx = kv.IndexOf('=');
          if (idx > 0) {
            var key = kv.Substring(0, idx);
            var val = kv.Substring(idx + 1);
            Environment.SetEnvironmentVariable(key, val);
          }
        }
      }
    } catch {}
  }
  static Process StartHidden(string file, string args, string workdir) {
    var psi = new ProcessStartInfo(file, args);
    psi.WorkingDirectory = workdir;
    psi.UseShellExecute = false;
    psi.CreateNoWindow = true;
    psi.WindowStyle = ProcessWindowStyle.Hidden;
    return Process.Start(psi);
  }
  [STAThread]
  static int Main(string[] args) {
    var baseDir = AppDomain.CurrentDomain.BaseDirectory;
    try { SetEnvFromConfig(Path.Combine(baseDir, "config.bat")); } catch {}
    // 1) Si existe app-image generado por jpackage, usarlo
    try {
      var jpExe = Path.Combine(baseDir, "MediLink", "MediLink.exe");
      if (File.Exists(jpExe)) {
        var p = StartHidden(jpExe, "", Path.GetDirectoryName(jpExe));
        return 0;
      }
    } catch {}
    // 2) Buscar java embebido (jlink) o del sistema
    string javaBin = Path.Combine(baseDir, "runtime", "bin", "java.exe");
    if (!File.Exists(javaBin)) javaBin = "java";
    string cp = "classes;lib/*";
    try {
      var p = StartHidden(javaBin, "-cp " + cp + " app.AppLauncher", baseDir);
      return 0;
    } catch (Exception ex) {
      MessageBox.Show("No se pudo iniciar MediLink.\n" + ex.ToString(), "$ProductName");
      return 1;
    }
  }
}
"@

$launcher = Join-Path $PSScriptRoot 'MediLink-Launcher.cs'
$asmInfo = Join-Path $PSScriptRoot 'AssemblyInfo.cs'
$cs | Set-Content -Encoding ASCII $launcher
$asm | Set-Content -Encoding ASCII $asmInfo

$cscCandidates = @(
  (Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'),
  (Join-Path $env:WINDIR 'Microsoft.NET\Framework\v4.0.30319\csc.exe')
)
$csc = $cscCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $csc) { throw "No se encontro csc.exe (.NET Framework) en el sistema." }

$icon = Join-Path $PSScriptRoot 'icon.ico'
$iconArg = if (Test-Path $icon) { "/win32icon:`"$icon`"" } else { "" }

$outExe = Join-Path $PSScriptRoot 'MediLink.exe'
& $csc /noconfig /target:winexe /out:"$outExe" $iconArg /r:System.dll /r:System.Core.dll /r:System.Windows.Forms.dll "$launcher" "$asmInfo"
