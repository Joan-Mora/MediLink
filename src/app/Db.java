package app;

import java.io.BufferedReader;

import java.io.File;

import java.io.FileInputStream;

import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.HashMap;

import java.util.Map;

import java.util.regex.Matcher;

import java.util.regex.Pattern;

/**

 * Acceso a base de datos: constantes de conexión y helper para obtener conexiones.

 */

public final class Db {

    private Db() {}

    // Configuración cargada de forma perezosa para evitar fallar al iniciar sin BD

    private static volatile Map<String, String> cached;

    private static volatile String configSourcePath; // ruta del db-config.bat utilizado

    public static Connection getConnection() throws SQLException {

        String url = get("DB_URL");

        String user = get("DB_USER");

        String pass = get("DB_PASS");

        if (isBlank(url)) throw new SQLException("Base de datos no configurada (DB_URL vacía)");

        if (user == null) user = ""; // permitir usuario vacío

        if (pass == null) pass = "";

        return DriverManager.getConnection(url, user, pass);

    }

    public static String get(String key) {

        Map<String, String> m = ensureLoaded();

        return nullSafeTrim(m.get(key));

    }

    /** Devuelve la ruta del archivo db-config.bat usado actualmente, o null si no hay. */

    public static String getConfigSourcePath() {

        return configSourcePath;

    }

    private static Map<String, String> ensureLoaded() {

        Map<String, String> m = cached;

        if (m != null) return m;

        synchronized (Db.class) {

            if (cached == null) cached = loadFromDbConfigBat();

            return cached;

        }

    }

    public static void refresh() { // para recargar después de un cambio

        synchronized (Db.class) { cached = null; }

    }

    private static String nullSafeTrim(String s) { return (s == null) ? null : s.trim(); }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static Map<String, String> loadFromDbConfigBat() {

        Map<String, String> map = new HashMap<>();

        File f = findDbConfigFile();

        configSourcePath = null;

        if (f == null || !f.isFile()) return map;

        try { configSourcePath = f.getAbsolutePath(); } catch (Exception ignored) {}

        Pattern p = Pattern.compile("^\\s*set\\s+(DB_URL|DB_USER|DB_PASS)=(.*)$", Pattern.CASE_INSENSITIVE);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                Matcher m = p.matcher(line);

                if (m.find()) {

                    String k = m.group(1).toUpperCase();

                    String val = m.group(2).trim();

                    // Desescapar ^& usado en batch

                    val = val.replace("^&", "&");

                    map.put(k, val);

                }

            }

        } catch (Exception ignored) {}

        return map;

    }

    private static File findDbConfigFile() {

        // 1) Ruta del ejecutable de jpackage (preferir archivo junto al .exe)

        try {

            String appPath = System.getProperty("jpackage.app-path");

            if (appPath != null) {

                File exe = new File(appPath);

                File dir = exe.getParentFile();

                if (dir != null) {

                    File a = new File(dir, "db-config.bat"); if (a.isFile()) return a;

                    File b = new File(dir, "app/db-config.bat"); if (b.isFile()) return b;

                }

            }

        } catch (Exception ignored) {}

        // 2) Directorio de trabajo (junto al portable o instalación)

        try {

            String wd = System.getProperty("user.dir");

            if (wd != null) {

                File a = new File(wd, "db-config.bat"); if (a.isFile()) return a;

                File b = new File(wd, "MediLink-Portable/db-config.bat"); if (b.isFile()) return b;

                File c = new File(wd, "app/db-config.bat"); if (c.isFile()) return c;

            }

        } catch (Exception ignored) {}

        // 3) Último intento: carpeta actual canónica

        try {

            File here = new File(".").getCanonicalFile();

            File c = new File(here, "db-config.bat"); if (c.isFile()) return c;

        } catch (Exception ignored) {}

        // 4) Directorios comunes del sistema (fallback)

        try {

            String programData = System.getenv("ProgramData");

            if (programData != null) {

                File d = new File(programData, "MediLink/db-config.bat"); if (d.isFile()) return d;

            }

        } catch (Exception ignored) {}

        try {

            String appData = System.getenv("APPDATA");

            if (appData != null) {

                File d = new File(appData, "MediLink/db-config.bat"); if (d.isFile()) return d;

            }

        } catch (Exception ignored) {}

        return null;

    }

}

