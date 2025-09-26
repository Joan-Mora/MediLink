param(
    [string]$Root = "$PSScriptRoot/../src"
)

Write-Host "Formateando .java en" $Root

if (-not (Test-Path $Root)) {
    Write-Error "Ruta no encontrada: $Root"
    exit 1
}

$files = Get-ChildItem -Path $Root -Recurse -Filter *.java -File

foreach ($f in $files) {
    try {
        $lines = Get-Content -Path $f.FullName -Encoding UTF8
        # En casos raros PowerShell puede devolver una sola cadena y la indexación produce [char]
        if ($lines -is [string]) {
            # Asegura un arreglo de lineas
            $lines = $lines -split "`r?`n"
        }
        $result = New-Object System.Collections.Generic.List[string]

        $i = 0
        while ($i -lt $lines.Count) {
            $line = $lines[$i]
            if ($line.Trim().Length -eq 0) {
                # Saltar líneas en blanco aquí; las gestionamos al cerrar cada línea no vacía
                $i++
                continue
            }

            $result.Add($line)

            # Saltar todas las líneas en blanco que sigan en el archivo original
            $j = $i + 1
            while ($j -lt $lines.Count -and ($lines[$j].Trim().Length -eq 0)) { $j++ }

            # Asegurar exactamente una línea en blanco después de la línea de código
            $result.Add("")
            $i = $j
        }

        $newContent = [string]::Join("`r`n", $result)

        $origContent = Get-Content -Path $f.FullName -Raw -Encoding UTF8
        if ($origContent -ne $newContent) {
            Set-Content -Path $f.FullName -Value $newContent -Encoding UTF8
            Write-Host "Formateado:" $f.FullName
        }
    } catch {
        Write-Warning "No se pudo formatear $($f.FullName): $($_.Exception.Message)"
    }
}

Write-Host "Formateo completado."
