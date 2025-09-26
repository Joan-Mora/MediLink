package app;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

import java.net.URI;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;

import java.util.List;

import static app.HttpUtils.*;

/** Endpoints para médicos: listar/buscar, crear y activar/desactivar. */

public final class MedicosEndpoints {

    private MedicosEndpoints() {}

    public static void register(HttpServer server) {

        server.createContext("/medicos", new MedicosHandler());

        server.createContext("/medicos/create", new CreateMedicoHandler());

        server.createContext("/medicos/toggle-status", new ToggleMedicoStatusHandler());

    }

    static class MedicosHandler implements HttpHandler {

        @Override 

        public void handle(HttpExchange exchange) throws IOException {

            URI uri = exchange.getRequestURI();

            String query = uri.getQuery();

            String q = ""; 

            int limit = 50;

            if (query != null) {

                for (String part : query.split("&")) {

                    String[] kv = part.split("=", 2);

                    if (kv.length == 2) {

                        if (kv[0].equals("q")) q = decode(kv[1]);

                        if (kv[0].equals("limit")) { 

                            try { 

                                limit = Math.min(Math.max(Integer.parseInt(kv[1]), 1), 200); 

                            } catch (Exception ignored) {} 

                        }

                    }

                }

            }

            List<String> rows = new ArrayList<>();

            String sql = "SELECT m.id_medico, m.nombres, m.apellidos, m.especialidad, " +

                        "COALESCE((SELECT ee.activo FROM estado_entidad ee WHERE ee.tipo_entidad = 'medico' AND ee.id_entidad = m.id_medico ORDER BY ee.fecha_cambio DESC LIMIT 1), true) as activo " +

                        "FROM medico m " +

                        "WHERE (m.nombres LIKE ? OR m.apellidos LIKE ? OR m.especialidad LIKE ?) " +

                        "ORDER BY m.nombres, m.apellidos LIMIT ?";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                String pattern = "%" + q + "%";

                ps.setString(1, pattern); 

                ps.setString(2, pattern); 

                ps.setString(3, pattern); 

                ps.setInt(4, limit);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) rows.add(asMedicoJson(rs));

                }

            } catch (Exception e) {

                writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}");

                return;

            }

            writeJson(exchange, 200, "[" + String.join(",", rows) + "]");

        }

        private static String asMedicoJson(ResultSet rs) throws SQLException {

            return String.format("{\"id_medico\":%d,\"nombres\":\"%s\",\"apellidos\":\"%s\",\"especialidad\":\"%s\",\"activo\":%s}",

                    rs.getInt("id_medico"), esc(rs.getString("nombres")), esc(rs.getString("apellidos")), 

                    esc(rs.getString("especialidad")), rs.getBoolean("activo"));

        }

    }

    static class CreateMedicoHandler implements HttpHandler {

        @Override 

        public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equals(exchange.getRequestMethod())) {

                writeJson(exchange, 405, "{\"error\":\"Method not allowed\"}");

                return;

            }

            String body = readRequestBody(exchange);

            String nombres = "", apellidos = "", especialidad = "";

            // Parse JSON body

            if (body.contains("\"nombres\":")) {

                String[] parts = body.split("\"nombres\":");

                if (parts.length > 1) {

                    String value = parts[1].split("[,}]")[0].trim().replaceAll("\"", "");

                    nombres = value;

                }

            }

            if (body.contains("\"apellidos\":")) {

                String[] parts = body.split("\"apellidos\":");

                if (parts.length > 1) {

                    String value = parts[1].split("[,}]")[0].trim().replaceAll("\"", "");

                    apellidos = value;

                }

            }

            if (body.contains("\"especialidad\":")) {

                String[] parts = body.split("\"especialidad\":");

                if (parts.length > 1) {

                    String value = parts[1].split("[,}]")[0].trim().replaceAll("\"", "");

                    especialidad = value;

                }

            }

            System.out.println("Datos recibidos - nombres: " + nombres + ", apellidos: " + apellidos + ", especialidad: " + especialidad);

            if (nombres.isEmpty() || apellidos.isEmpty() || especialidad.isEmpty()) {

                writeJson(exchange, 400, "{\"error\":\"Faltan campos requeridos: nombres, apellidos, especialidad\"}");

                return;

            }

            // Insertar solo con los campos que existen en la tabla

            String sql = "INSERT INTO medico (nombres, apellidos, especialidad) VALUES (?, ?, ?)";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, nombres);

                ps.setString(2, apellidos);

                ps.setString(3, especialidad);

                ps.executeUpdate();

                // Obtener el ID del médico creado

                int medicoId = -1;

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {

                        medicoId = rs.getInt(1);

                    }

                }

                // Crear usuario automático

                String nombreUsuario = "";

                String contrasena = "";

                if (medicoId > 0) {

                    nombreUsuario = nombres.toLowerCase().replaceAll("\\s+", "") + "." + apellidos.toLowerCase().replaceAll("\\s+", "") + medicoId;

                    contrasena = nombres.substring(0, Math.min(2, nombres.length())).toLowerCase() + 

                                      apellidos.substring(0, Math.min(2, apellidos.length())).toLowerCase() + 

                                      String.format("%04d", medicoId);

                    // Usar los campos correctos: medico_id_medico, nombre, contrasena

                    String userSql = "INSERT INTO usuario (medico_id_medico, nombre, contrasena) VALUES (?, ?, ?)";

                    try (PreparedStatement userPs = c.prepareStatement(userSql)) {

                        userPs.setInt(1, medicoId);

                        userPs.setString(2, nombreUsuario);

                        userPs.setString(3, contrasena);

                        userPs.executeUpdate();

                        System.out.println("Usuario creado automáticamente: " + nombreUsuario + " / " + contrasena);

                    }

                }

                writeJson(exchange, 200, "{\"success\":true,\"message\":\"Médico y usuario creados exitosamente\",\"id_medico\":" + medicoId + ",\"usuario\":\"" + nombreUsuario + "\",\"contrasena\":\"" + contrasena + "\"}");

            } catch (Exception e) {

                writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}");

            }

        }

    }

    static class ToggleMedicoStatusHandler implements HttpHandler {

        @Override 

        public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equals(exchange.getRequestMethod())) {

                writeJson(exchange, 405, "{\"error\":\"Method not allowed\"}");

                return;

            }

            String body = readRequestBody(exchange);

            int medicoId = -1;

            Boolean solicitado = null; // true=activar, false=inactivar

            String accion = null;

            // Parse JSON body to get medico ID

            if (body.contains("\"id_medico\":")) {

                String[] parts = body.split("\"id_medico\":");

                if (parts.length > 1) {

                    String idPart = parts[1].split("[,}]")[0].trim();

                    try {

                        medicoId = Integer.parseInt(idPart);

                    } catch (NumberFormatException e) {

                        writeJson(exchange, 400, "{\"error\":\"ID de médico inválido\"}");

                        return;

                    }

                }

            }

            if (medicoId == -1) {

                writeJson(exchange, 400, "{\"error\":\"ID de médico requerido\"}");

                return;

            }

            if (body.contains("\"activo\":")) {

                String[] partsAct = body.split("\"activo\":");

                if (partsAct.length > 1) {

                    String v = partsAct[1].split("[,}]")[0].trim().replace("\"", "");

                    if (v.equalsIgnoreCase("true") || v.equals("1")) solicitado = Boolean.TRUE;

                    else if (v.equalsIgnoreCase("false") || v.equals("0")) solicitado = Boolean.FALSE;

                }

            }

            String bodyLower = body.toLowerCase();

            if (bodyLower.contains("\"accion\":")) {

                String[] partsAc = bodyLower.split("\"accion\":");

                if (partsAc.length > 1) accion = partsAc[1].split("[,}]")[0].trim().replace("\"", "");

            } else if (bodyLower.contains("\"action\":")) {

                String[] partsAc = bodyLower.split("\"action\":");

                if (partsAc.length > 1) accion = partsAc[1].split("[,}]")[0].trim().replace("\"", "");

            }

            if (accion != null) {

                if ("activar".equalsIgnoreCase(accion)) solicitado = Boolean.TRUE;

                else if ("inactivar".equalsIgnoreCase(accion) || "desactivar".equalsIgnoreCase(accion)) solicitado = Boolean.FALSE;

            }

            String checkSql = "SELECT activo FROM estado_entidad WHERE tipo_entidad = 'medico' AND id_entidad = ? ORDER BY fecha_cambio DESC LIMIT 1";

            try (Connection c = Db.getConnection()) {

                // Verificar que el médico existe

                String verificarSql = "SELECT id_medico FROM medico WHERE id_medico = ?";

                try (PreparedStatement psVerificar = c.prepareStatement(verificarSql)) {

                    psVerificar.setInt(1, medicoId);

                    try (ResultSet rs = psVerificar.executeQuery()) {

                        if (!rs.next()) {

                            writeJson(exchange, 404, "{\"error\":\"Médico no encontrado\"}");

                            return;

                        }

                    }

                }

                // Obtener estado actual

                boolean estadoActual = true; // Por defecto activo

                try (PreparedStatement psCheck = c.prepareStatement(checkSql)) {

                    psCheck.setInt(1, medicoId);

                    try (ResultSet rs = psCheck.executeQuery()) {

                        if (rs.next()) {

                            estadoActual = rs.getBoolean("activo");

                        }

                    }

                }

                boolean nuevoEstado = (solicitado != null) ? solicitado : !estadoActual;

                if (solicitado != null && nuevoEstado == estadoActual) {

                    String ya = estadoActual ? "Médico ya se encuentra activo" : "Médico ya se encuentra inactivo";

                    writeJson(exchange, 200, "{\"success\":true,\"message\":\"" + ya + "\",\"activo\":" + estadoActual + ",\"changed\":false}");

                    return;

                }

                // Toggle el estado con UPDATE-then-INSERT

                String updSql = "UPDATE estado_entidad SET activo = ?, fecha_cambio = CURRENT_TIMESTAMP WHERE tipo_entidad = 'medico' AND id_entidad = ?";

                try (PreparedStatement psUpd = c.prepareStatement(updSql)) {

                    psUpd.setBoolean(1, nuevoEstado);

                    psUpd.setInt(2, medicoId);

                    int updated = psUpd.executeUpdate();

                    if (updated == 0) {

                        String insSql = "INSERT INTO estado_entidad (tipo_entidad, id_entidad, activo) VALUES ('medico', ?, ?)";

                        try (PreparedStatement psIns = c.prepareStatement(insSql)) {

                            psIns.setInt(1, medicoId);

                            psIns.setBoolean(2, nuevoEstado);

                            psIns.executeUpdate();

                        }

                    }

                }

                String mensaje = nuevoEstado ? "Médico activado correctamente" : "Médico desactivado correctamente";

                writeJson(exchange, 200, "{\"success\":true,\"message\":\"" + mensaje + "\",\"activo\":" + nuevoEstado + ",\"changed\":true}");

            } catch (Exception e) {

                writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}");

            }

        }

    }

}

