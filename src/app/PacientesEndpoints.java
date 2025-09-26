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

import java.util.ArrayList;

import java.util.List;

import static app.HttpUtils.*;

/**

 * Endpoints para pacientes: listar/crear y ver historial de diagnósticos.

 */

public final class PacientesEndpoints {

    private PacientesEndpoints() {}

    public static void register(HttpServer server) {

        server.createContext("/pacientes", new PacientesHandler());

        server.createContext("/pacientes/create", new CreatePacienteHandler());

        server.createContext("/pacientes/toggle-status", new TogglePacienteStatusHandler());

        server.createContext("/pacientes/", new PacienteDiagnosticosHandler());

    }

    static class PacientesHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            URI uri = exchange.getRequestURI();

            String query = uri.getQuery(); String q = ""; int limit = 50;

            if (query != null) {

                for (String part : query.split("&")) {

                    String[] kv = part.split("=", 2);

                    if (kv.length == 2) {

                        if (kv[0].equals("q")) q = decode(kv[1]);

                        if (kv[0].equals("limit")) { try { limit = Math.min(Math.max(Integer.parseInt(kv[1]), 1), 200); } catch (Exception ignored) {} }

                    }

                }

            }

            List<String> rows = new ArrayList<>();

            String base = "SELECT p.id_paciente, p.nombres, p.apellidos, p.edad, p.genero, p.correo, p.direccion, p.tipo_documento, p.nro_documento, p.nro_contacto, " +

                         "COALESCE((SELECT ee.activo FROM estado_entidad ee WHERE ee.tipo_entidad = 'paciente' AND ee.id_entidad = p.id_paciente ORDER BY ee.fecha_cambio DESC LIMIT 1), true) as activo " +

                         "FROM paciente p";

            try (Connection c = Db.getConnection()) {

                if (q != null && q.trim().length() > 0) {

                    String sql = base + " WHERE p.nombres LIKE ? OR p.apellidos LIKE ? OR p.nro_documento LIKE ? OR p.correo LIKE ? ORDER BY p.id_paciente DESC LIMIT ?";

                    try (PreparedStatement ps = c.prepareStatement(sql)) {

                        String like = "%" + q + "%";

                        ps.setString(1, like); ps.setString(2, like); ps.setString(3, like); ps.setString(4, like); ps.setInt(5, limit);

                        try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(asPacienteJson(rs)); }

                    }

                } else {

                    String sql = base + " ORDER BY p.id_paciente DESC LIMIT ?";

                    try (PreparedStatement ps = c.prepareStatement(sql)) {

                        ps.setInt(1, limit);

                        try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(asPacienteJson(rs)); }

                    }

                }

            } catch (Exception e) {

                writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}"); return;

            }

            writeJson(exchange, 200, "[" + String.join(",", rows) + "]");

        }

        private static String asPacienteJson(ResultSet rs) throws SQLException {

            return String.format("{\"id_paciente\":%d,\"nombres\":\"%s\",\"apellidos\":\"%s\",\"edad\":%d,\"genero\":%s,\"correo\":%s,\"direccion\":\"%s\",\"tipo_documento\":\"%s\",\"nro_documento\":\"%s\",\"nro_contacto\":%s,\"activo\":%s}",

                    rs.getInt("id_paciente"), 

                    esc(rs.getString("nombres")), 

                    esc(rs.getString("apellidos")), 

                    rs.getInt("edad"),

                    toJsonValue(rs.getString("genero")),

                    toJsonValue(rs.getString("correo")),

                    esc(rs.getString("direccion")),

                    esc(rs.getString("tipo_documento")),

                    esc(rs.getString("nro_documento")),

                    toJsonValue(rs.getString("nro_contacto")),

                    rs.getBoolean("activo"));

        }

        private static String toJsonValue(String s) {

            return s == null ? "null" : "\"" + esc(s) + "\"";

        }

    }

    static class CreatePacienteHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equals(exchange.getRequestMethod())) { writeJson(exchange, 405, "{\"error\":\"Method not allowed. Use POST\"}"); return; }

            String body = readRequestBody(exchange);

            if (body == null || body.trim().isEmpty()) { writeJson(exchange, 400, "{\"error\":\"Request body is required\"}"); return; }

            try {

                PacienteData p = parseJsonPaciente(body);

                if (p == null) { writeJson(exchange, 400, "{\"error\":\"Invalid JSON format\"}"); return; }

                if (isBlank(p.nombres) || isBlank(p.apellidos) || p.edad == null || isBlank(p.direccion) || isBlank(p.tipo_documento) || isBlank(p.nro_documento)) {

                    writeJson(exchange, 400, "{\"error\":\"nombres, apellidos, edad, direccion, tipo_documento, and nro_documento are required\"}"); return; 

                }

                int newId = insertPaciente(p);

                if (newId > 0) writeJson(exchange, 201, "{\"success\":true,\"id_paciente\":" + newId + ",\"message\":\"Patient created successfully\"}");

                else writeJson(exchange, 500, "{\"error\":\"Failed to create patient\"}");

            } catch (Exception e) { writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}"); }

        }

        private static PacienteData parseJsonPaciente(String json) {

            try {

                PacienteData p = new PacienteData(); json = json.trim();

                if (!json.startsWith("{") || !json.endsWith("}")) return null; json = json.substring(1, json.length() - 1);

                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (String pair : pairs) {

                    String[] kv = pair.split(":", 2); if (kv.length != 2) continue;

                    String key = kv[0].trim().replace("\"", ""); String value = kv[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);

                    switch (key) {

                        case "nombres": p.nombres = value; break;

                        case "apellidos": p.apellidos = value; break;

                        case "edad": try { p.edad = Integer.parseInt(value); } catch (Exception ignored) {} break;

                        case "genero": p.genero = value; break;

                        case "correo": p.correo = value; break;

                        case "direccion": p.direccion = value; break;

                        case "tipo_documento": p.tipo_documento = value; break;

                        case "nro_documento": p.nro_documento = value; break;

                        case "nro_contacto": p.nro_contacto = value; break;

                    }

                }

                return p;

            } catch (Exception e) { return null; }

        }

        private static int insertPaciente(PacienteData p) {

            String sql = "INSERT INTO paciente (nombres, apellidos, edad, genero, correo, direccion, tipo_documento, nro_documento, nro_contacto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, p.nombres); 

                ps.setString(2, p.apellidos); 

                ps.setInt(3, p.edad);

                ps.setString(4, p.genero);

                ps.setString(5, p.correo);

                ps.setString(6, p.direccion);

                ps.setString(7, p.tipo_documento);

                ps.setString(8, p.nro_documento);

                ps.setString(9, p.nro_contacto);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) { try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); } }

            } catch (SQLException e) { e.printStackTrace(); }

            return -1;

        }

        private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    }

    static class PacienteDiagnosticosHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            if (!"GET".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, 0); exchange.close(); return; }

            String path = exchange.getRequestURI().getPath(); String[] segments = path.split("/");

            if (segments.length < 3 || !"pacientes".equals(segments[1])) { writeJson(exchange, 400, "{\"error\":\"Invalid path format. Use /pacientes/{id}/diagnosticos\"}"); return; }

            int pacienteId; try { pacienteId = Integer.parseInt(segments[2]); } catch (NumberFormatException e) { writeJson(exchange, 400, "{\"error\":\"Invalid patient ID\"}"); return; }

            if (segments.length < 4 || !"diagnosticos".equals(segments[3])) { writeJson(exchange, 400, "{\"error\":\"Invalid path format. Use /pacientes/{id}/diagnosticos\"}"); return; }

            if (!pacienteExists(pacienteId)) { writeJson(exchange, 404, "{\"error\":\"Patient not found\"}"); return; }

            String sql = "SELECT d.id_diagnostico, d.medico_id_medico, d.paciente_id_paciente, d.observaciones, d.fecha, d.hora, " +

                    "m.nombres as medico_nombres, m.apellidos as medico_apellidos, m.especialidad " +

                    "FROM diagnostico d INNER JOIN medico m ON d.medico_id_medico = m.id_medico WHERE d.paciente_id_paciente = ? " +

                    "ORDER BY d.fecha DESC, d.hora DESC";

            StringBuilder json = new StringBuilder("["); boolean first = true;

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, pacienteId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        if (!first) json.append(","); first = false;

                        json.append("{")

                                .append("\"id_diagnostico\":").append(rs.getInt("id_diagnostico")).append(",")

                                .append("\"medico_id_medico\":").append(rs.getInt("medico_id_medico")).append(",")

                                .append("\"paciente_id_paciente\":").append(rs.getInt("paciente_id_paciente")).append(",")

                                .append("\"observaciones\":\"").append(safe(rs.getString("observaciones"))).append("\",")

                                .append("\"fecha\":\"").append(safe(rs.getString("fecha"))).append("\",")

                                .append("\"hora\":\"").append(safe(rs.getString("hora"))).append("\",")

                                .append("\"medico\":{")

                                .append("\"nombres\":\"").append(safe(rs.getString("medico_nombres"))).append("\",")

                                .append("\"apellidos\":\"").append(safe(rs.getString("medico_apellidos"))).append("\",")

                                .append("\"especialidad\":\"").append(safe(rs.getString("especialidad"))).append("\"")

                                .append("}")

                                .append("}");

                    }

                }

            } catch (SQLException e) { e.printStackTrace(); writeJson(exchange, 500, "{\"error\":\"Database error\"}"); return; }

            json.append("]"); writeJson(exchange, 200, json.toString());

        }

        private static boolean pacienteExists(int pacienteId) {

            String sql = "SELECT COUNT(*) FROM paciente WHERE id_paciente = ?";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, pacienteId);

                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }

            } catch (SQLException e) { e.printStackTrace(); }

            return false;

        }

    }

    /** Modelo simple del paciente. */

    static class PacienteData { 

        String nombres; 

        String apellidos; 

        Integer edad;

        String genero;

        String correo;

        String direccion;

        String tipo_documento;

        String nro_documento; 

        String nro_contacto;

    }

    static class TogglePacienteStatusHandler implements HttpHandler {

        @Override 

        public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equals(exchange.getRequestMethod())) {

                writeJson(exchange, 405, "{\"error\":\"Method not allowed\"}");

                return;

            }

            String body = readRequestBody(exchange);

            int pacienteId = -1;

            Boolean solicitado = null; // true=activar, false=inactivar

            String accion = null;

            // ID requerido

            if (body.contains("\"id_paciente\":")) {

                String[] parts = body.split("\"id_paciente\":");

                if (parts.length > 1) {

                    String idPart = parts[1].split("[,}]")[0].trim();

                    try {

                        pacienteId = Integer.parseInt(idPart);

                    } catch (NumberFormatException e) {

                        writeJson(exchange, 400, "{\"error\":\"ID de paciente inválido\"}");

                        return;

                    }

                }

            }

            if (pacienteId == -1) {

                writeJson(exchange, 400, "{\"error\":\"ID de paciente requerido\"}");

                return;

            }

            // Campos opcionales: activo/accion (o action)

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

            String checkSql = "SELECT activo FROM estado_entidad WHERE tipo_entidad = 'paciente' AND id_entidad = ? ORDER BY fecha_cambio DESC LIMIT 1";

            try (Connection c = Db.getConnection()) {

                // Verificar existencia

                String verificarSql = "SELECT id_paciente FROM paciente WHERE id_paciente = ?";

                try (PreparedStatement psVerificar = c.prepareStatement(verificarSql)) {

                    psVerificar.setInt(1, pacienteId);

                    try (ResultSet rs = psVerificar.executeQuery()) {

                        if (!rs.next()) {

                            writeJson(exchange, 404, "{\"error\":\"Paciente no encontrado\"}");

                            return;

                        }

                    }

                }

                // Obtener estado actual

                boolean estadoActual = true; // Por defecto activo

                try (PreparedStatement psCheck = c.prepareStatement(checkSql)) {

                    psCheck.setInt(1, pacienteId);

                    try (ResultSet rs = psCheck.executeQuery()) {

                        if (rs.next()) {

                            estadoActual = rs.getBoolean("activo");

                        }

                    }

                }

                // Si se solicitó un estado explícito y ya coincide, no cambiar

                boolean nuevoEstado = (solicitado != null) ? solicitado : !estadoActual;

                if (solicitado != null && nuevoEstado == estadoActual) {

                    String ya = estadoActual ? "Paciente ya se encuentra activo" : "Paciente ya se encuentra inactivo";

                    writeJson(exchange, 200, "{\"success\":true,\"message\":\"" + ya + "\",\"activo\":" + estadoActual + ",\"changed\":false}");

                    return;

                }

                // Toggle del estado con UPDATE-then-INSERT

                String updSql = "UPDATE estado_entidad SET activo = ?, fecha_cambio = CURRENT_TIMESTAMP WHERE tipo_entidad = 'paciente' AND id_entidad = ?";

                try (PreparedStatement psUpd = c.prepareStatement(updSql)) {

                    psUpd.setBoolean(1, nuevoEstado);

                    psUpd.setInt(2, pacienteId);

                    int updated = psUpd.executeUpdate();

                    if (updated == 0) {

                        String insSql = "INSERT INTO estado_entidad (tipo_entidad, id_entidad, activo) VALUES ('paciente', ?, ?)";

                        try (PreparedStatement psIns = c.prepareStatement(insSql)) {

                            psIns.setInt(1, pacienteId);

                            psIns.setBoolean(2, nuevoEstado);

                            psIns.executeUpdate();

                        }

                    }

                }

                String mensaje = nuevoEstado ? "Paciente activado correctamente" : "Paciente desactivado correctamente";

                writeJson(exchange, 200, "{\"success\":true,\"message\":\"" + mensaje + "\",\"activo\":" + nuevoEstado + ",\"changed\":true}");

            } catch (Exception e) {

                writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}");

            }

        }

    }

}

