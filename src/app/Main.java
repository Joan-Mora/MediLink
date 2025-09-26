package app;

import static app.HttpUtils.writeJson;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

import java.net.InetSocketAddress;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

/**

 * MediLink - Sistema de Gestión Hospitalaria

 * Punto de entrada de la API. Solo inicia el servidor y registra los módulos.

 */

public class Main {

    public static void main(String[] args) {

        int port = resolvePort();

        try {

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Health

            server.createContext("/health", new HealthHandler());

            // Registrar módulos por recurso

            PacientesEndpoints.register(server);

            MedicosEndpoints.register(server);

            UsuariosEndpoints.register(server);

                AuthEndpoints.register(server);

            DiagnosticosEndpoints.register(server);

            server.setExecutor(null);

            // Exponer URL base para la GUI (mismo proceso) ANTES de iniciar el servidor para evitar carreras

            try { System.setProperty("medilink.baseUrl", "http://localhost:" + port); } catch (Throwable ignore) {}

            server.start();

            System.out.println("============================================================");

            System.out.println("███╗   ███╗███████╗██████╗ ██╗██╗     ██╗███╗   ██╗██╗  ██╗");

            System.out.println("████╗ ████║██╔════╝██╔══██╗██║██║     ██║████╗  ██║██║ ██╔╝");

            System.out.println("██╔████╔██║█████╗  ██║  ██║██║██║     ██║██╔██╗ ██║█████╔╝ ");

            System.out.println("██║╚██╔╝██║██╔══╝  ██║  ██║██║██║     ██║██║╚██╗██║██╔═██╗ ");

            System.out.println("██║ ╚═╝ ██║███████╗██████╔╝██║███████╗██║██║ ╚████║██║  ██╗");

            System.out.println("╚═╝     ╚═╝╚══════╝╚═════╝ ╚═╝╚══════╝╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝");

            System.out.println("                  Sistema de Gestión Hospitalaria v" + AppInfo.VERSION);

            System.out.println("============================================================");

            System.out.println("SERVIDOR INICIADO EN PUERTO " + port);

            System.out.println("============================================================");

            System.out.println();

            System.out.println("ENDPOINTS DISPONIBLES:");

            System.out.println();

            System.out.println("HEALTH CHECK:");

            System.out.println("   GET  http://localhost:" + port + "/health");

            System.out.println();

            System.out.println("MEDICOS:");

            System.out.println("   GET  http://localhost:" + port + "/medicos");

            System.out.println("   GET  http://localhost:" + port + "/medicos?q=Julian");

            System.out.println("   POST http://localhost:" + port + "/medicos/create");

            System.out.println("        Body: {\"nombres\":\"Juan\",\"apellidos\":\"Perez\",\"especialidad\":\"Cardiologia\"}");

            System.out.println();

            System.out.println("PACIENTES:");

            System.out.println("   GET  http://localhost:" + port + "/pacientes");

            System.out.println("   GET  http://localhost:" + port + "/pacientes?q=Darwin");

                System.out.println("AUTENTICACIÓN:");

                System.out.println("   POST http://localhost:" + port + "/auth/login");

                System.out.println("        Body: {\"nombre\":\"usuario123\",\"contrasena\":\"pass123\"}");

                System.out.println();

            System.out.println("   POST http://localhost:" + port + "/pacientes/create");

            System.out.println("        Body: {\"nombres\":\"Ana\",\"apellidos\":\"Garcia\",\"edad\":25,\"genero\":\"F\",\"correo\":\"ana@email.com\",\"direccion\":\"Calle 123\",\"tipo_documento\":\"CC\",\"nro_documento\":\"12345678\",\"nro_contacto\":\"3001234567\"}");

            System.out.println("   GET  http://localhost:" + port + "/pacientes/{id}/diagnosticos");

            System.out.println();

            System.out.println("USUARIOS:");

            System.out.println("   GET  http://localhost:" + port + "/usuarios");

            System.out.println("   GET  http://localhost:" + port + "/usuarios?medico_id=1");

            System.out.println("   POST http://localhost:" + port + "/usuarios/create");

            System.out.println("        Body: {\"nombre\":\"usuario123\",\"contrasena\":\"pass123\",\"medico_id_medico\":1}");

            System.out.println();

            System.out.println("DIAGNOSTICOS:");

            System.out.println("   GET  http://localhost:" + port + "/diagnosticos");

            System.out.println("   GET  http://localhost:" + port + "/diagnosticos?medico_id=1&paciente_id=2");

            System.out.println("   POST http://localhost:" + port + "/diagnosticos/create");

            System.out.println("        Body: {\"medico_id_medico\":1,\"paciente_id_paciente\":2,\"observaciones\":\"Revision general\"}");

            System.out.println();

            System.out.println("EJEMPLOS POWERSHELL (FORMATO LISTA):");

            System.out.println("   Invoke-RestMethod -Uri \"http://localhost:" + port + "/medicos\" -Method GET | Format-List");

            System.out.println("   Invoke-RestMethod -Uri \"http://localhost:" + port + "/pacientes\" -Method GET | Format-List");

            System.out.println("   Invoke-RestMethod -Uri \"http://localhost:" + port + "/usuarios\" -Method GET | Format-List");

            System.out.println("   Invoke-RestMethod -Uri \"http://localhost:" + port + "/diagnosticos\" -Method GET | Format-List");

            System.out.println();

            System.out.println("CREAR REGISTROS:");

            System.out.println("   Invoke-RestMethod -Uri \"http://localhost:" + port + "/pacientes/create\" -Method POST -Body '{\"nombres\":\"Test\",\"apellidos\":\"User\",\"edad\":30,\"direccion\":\"Calle Test\",\"tipo_documento\":\"CC\",\"nro_documento\":\"99999999\"}' -ContentType \"application/json\"");

            System.out.println();

            System.out.println("============================================================");

            System.out.println("        MediLink - Conectando el cuidado de la salud");

            System.out.println("============================================================");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static int resolvePort() {

        // 1) Env var MEDILINK_HTTP_PORT si existe y es válido

        try {

            String env = System.getenv("MEDILINK_HTTP_PORT");

            if (env != null && !env.trim().isEmpty()) {

                int p = Integer.parseInt(env.trim());

                if (p > 0 && p < 65536) return p;

            }

        } catch (Exception ignore) {}

        // 2) Buscar puerto libre desde 8081 a 8099

        for (int p = 8081; p <= 8099; p++) {

            try {

                java.net.ServerSocket ss = new java.net.ServerSocket();

                ss.setReuseAddress(true);

                ss.bind(new java.net.InetSocketAddress("127.0.0.1", p));

                ss.close();

                return p;

            } catch (Exception ignore) {}

        }

        // 3) último recurso

        return 8081;

    }

    /**

     * Handler de salud que verifica la conexión a la base de datos con SELECT 1.

     */

    static class HealthHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            StringBuilder json = new StringBuilder();

            json.append('{');

            boolean ok = false;

            String configPath = Db.getConfigSourcePath();

            if (configPath != null) {

                json.append("\"config\":\"").append(configPath.replace("\\", "/").replace("\"","'")).append("\",");

            }

            String url = Db.get("DB_URL");

            if (url != null) {

                // Sanitizar URL para no exponer password si viniera embebida

                String safeUrl = url.replace("\"","'");

                int pIdx = safeUrl.indexOf("password=");

                if (pIdx >= 0) safeUrl = safeUrl.substring(0, pIdx) + "password=***";

                json.append("\"url\":\"").append(safeUrl).append("\",");

            }

            try (Connection c = Db.getConnection();

                 PreparedStatement ps = c.prepareStatement("SELECT 1");

                 ResultSet rs = ps.executeQuery()) {

                if (rs.next() && rs.getInt(1) == 1) {

                    ok = true;

                    json.append("\"db\":\"connected\"");

                }

            } catch (Exception e) {

                json.append("\"db\":\"error\",\"message\":\"")

                    .append(e.getMessage() == null ? "" : e.getMessage().replace("\"", "'"))

                    .append("\"");

            }

            json.append(',').append("\"ok\":").append(ok ? "true" : "false");

            json.append('}');

            writeJson(exchange, 200, json.toString());

        }

    }

}

