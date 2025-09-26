package app;

import static app.HttpUtils.*;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

import java.sql.*;

import java.time.LocalDate;

import java.time.LocalTime;

import java.util.ArrayList;

import java.util.List;

/** Endpoints para diagnósticos: listar/filtrar, crear y eliminar. */

public final class DiagnosticosEndpoints {

    private DiagnosticosEndpoints() {}

    public static void register(HttpServer server) {

        server.createContext("/diagnosticos", new DiagnosticosHandler());

        server.createContext("/diagnosticos/create", new CreateDiagnosticoHandler());

        server.createContext("/diagnosticos/delete", new DeleteDiagnosticoHandler());

    }

    static class DiagnosticosHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery(); String pacienteId = null, medicoId = null; int limit = 50;

            if (query != null) {

                for (String part : query.split("&")) {

                    String[] kv = part.split("=", 2);

                    if (kv.length == 2) {

                        if (kv[0].equals("paciente_id")) pacienteId = decode(kv[1]);

                        if (kv[0].equals("medico_id")) medicoId = decode(kv[1]);

                        if (kv[0].equals("limit")) { try { limit = Math.min(Math.max(Integer.parseInt(kv[1]), 1), 200); } catch (Exception ignored) {} }

                    }

                }

            }

            List<String> rows = new ArrayList<>();

            String base = "SELECT d.id_diagnostico, d.medico_id_medico, d.paciente_id_paciente, d.observaciones, d.fecha, d.hora, " +

                    "m.nombres AS medico_nombres, m.apellidos AS medico_apellidos, m.especialidad, " +

                    "p.nombres AS paciente_nombres, p.apellidos AS paciente_apellidos, p.nro_documento " +

                    "FROM diagnostico d INNER JOIN medico m ON d.medico_id_medico = m.id_medico INNER JOIN paciente p ON d.paciente_id_paciente = p.id_paciente";

            String where = ""; List<Object> params = new ArrayList<>();

            if (pacienteId != null && pacienteId.trim().length() > 0) { where += (where.isEmpty()?" WHERE ":" AND ") + "d.paciente_id_paciente = ?"; params.add(Integer.parseInt(pacienteId)); }

            if (medicoId != null && medicoId.trim().length() > 0) { where += (where.isEmpty()?" WHERE ":" AND ") + "d.medico_id_medico = ?"; params.add(Integer.parseInt(medicoId)); }

            String sql = base + where + " ORDER BY d.id_diagnostico DESC LIMIT ?";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                int idx = 1; for (Object p : params) { ps.setObject(idx++, p); } ps.setInt(idx, limit);

                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(asDiagnosticoJson(rs)); }

            } catch (Exception e) { writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}"); return; }

            writeJson(exchange, 200, "[" + String.join(",", rows) + "]");

        }

        private static String asDiagnosticoJson(ResultSet rs) throws SQLException {

            return String.format("{\"id_diagnostico\":%d,\"medico_id_medico\":%d,\"paciente_id_paciente\":%d,\"observaciones\":\"%s\",\"fecha\":\"%s\",\"hora\":\"%s\",\"medico\":{\"nombres\":\"%s\",\"apellidos\":\"%s\",\"especialidad\":\"%s\"},\"paciente\":{\"nombres\":\"%s\",\"apellidos\":\"%s\",\"nro_documento\":\"%s\"}}",

                    rs.getInt("id_diagnostico"), rs.getInt("medico_id_medico"), rs.getInt("paciente_id_paciente"), esc(rs.getString("observaciones")),

                    esc(rs.getString("fecha")), esc(rs.getString("hora")), esc(rs.getString("medico_nombres")), esc(rs.getString("medico_apellidos")), esc(rs.getString("especialidad")),

                    esc(rs.getString("paciente_nombres")), esc(rs.getString("paciente_apellidos")), esc(rs.getString("nro_documento")));

        }

    }

    static class CreateDiagnosticoHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equals(exchange.getRequestMethod())) { writeJson(exchange, 405, "{\"error\":\"Method not allowed. Use POST\"}"); return; }

            String body = readRequestBody(exchange);

            if (body == null || body.trim().isEmpty()) { writeJson(exchange, 400, "{\"error\":\"Request body is required\"}"); return; }

            try {

                DiagnosticoData d = parseJsonDiagnostico(body);

                if (d == null) { writeJson(exchange, 400, "{\"error\":\"Invalid JSON format\"}"); return; }

                if (d.medico_id_medico == null || d.paciente_id_paciente == null || isBlank(d.observaciones)) { writeJson(exchange, 400, "{\"error\":\"medico_id_medico, paciente_id_paciente y observaciones son requeridos\"}"); return; }

                if (!medicoExists(d.medico_id_medico)) { writeJson(exchange, 400, "{\"error\":\"medico_id_medico does not exist\"}"); return; }

                if (!pacienteExists(d.paciente_id_paciente)) { writeJson(exchange, 400, "{\"error\":\"paciente_id_paciente does not exist\"}"); return; }

                int newId = insertDiagnostico(d);

                if (newId > 0) writeJson(exchange, 201, "{\"success\":true,\"id_diagnostico\":" + newId + ",\"message\":\"Diagnostico created successfully\"}");

                else writeJson(exchange, 500, "{\"error\":\"Failed to create diagnostico\"}");

            } catch (Exception e) { writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}"); }

        }

        private static DiagnosticoData parseJsonDiagnostico(String json) {

            try {

                DiagnosticoData d = new DiagnosticoData(); json = json.trim();

                if (!json.startsWith("{") || !json.endsWith("}")) return null; json = json.substring(1, json.length() - 1);

                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (String pair : pairs) {

                    String[] kv = pair.split(":", 2); if (kv.length != 2) continue;

                    String key = kv[0].trim().replace("\"", ""); String value = kv[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);

                    switch (key) {

                        case "medico_id_medico": try { d.medico_id_medico = Integer.parseInt(value); } catch (Exception ignored) {} break;

                        case "paciente_id_paciente": try { d.paciente_id_paciente = Integer.parseInt(value); } catch (Exception ignored) {} break;

                        case "observaciones": d.observaciones = value; break;

                    }

                }

                return d;

            } catch (Exception e) { return null; }

        }

        private static int insertDiagnostico(DiagnosticoData d) {

            if (d.fecha == null) d.fecha = Date.valueOf(LocalDate.now());

            if (d.hora == null) d.hora = Time.valueOf(LocalTime.now().withNano(0));

            String sql = "INSERT INTO diagnostico (medico_id_medico, paciente_id_paciente, observaciones, fecha, hora) VALUES (?, ?, ?, ?, ?)";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, d.medico_id_medico); ps.setInt(2, d.paciente_id_paciente); ps.setString(3, d.observaciones); ps.setDate(4, d.fecha); ps.setTime(5, d.hora);

                int rows = ps.executeUpdate(); if (rows > 0) { try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); } }

            } catch (SQLException e) { e.printStackTrace(); }

            return -1;

        }

        private static boolean medicoExists(Integer medicoId) {

            String sql = "SELECT COUNT(*) FROM medico WHERE id_medico = ?";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, medicoId);

                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }

            } catch (SQLException e) { e.printStackTrace(); }

            return false;

        }

        private static boolean pacienteExists(Integer pacienteId) {

            String sql = "SELECT COUNT(*) FROM paciente WHERE id_paciente = ?";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, pacienteId);

                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }

            } catch (SQLException e) { e.printStackTrace(); }

            return false;

        }

        private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    }

    static class DeleteDiagnosticoHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            System.out.println("DEBUG - Método recibido: " + exchange.getRequestMethod());

            System.out.println("DEBUG - URI: " + exchange.getRequestURI());

            // Permitir tanto DELETE como POST para debug

            if (!"DELETE".equals(exchange.getRequestMethod()) && !"POST".equals(exchange.getRequestMethod())) {

                writeJson(exchange, 405, "{\"error\":\"Method not allowed. Use DELETE or POST\"}");

                return;

            }

            String body = readRequestBody(exchange);

            System.out.println("DEBUG - Body recibido: " + body);

            if (body == null || body.trim().isEmpty()) {

                writeJson(exchange, 400, "{\"error\":\"Request body is required\"}");

                return;

            }

            try {

                // Parse ID from JSON

                Integer idDiagnostico = parseDeleteRequest(body);

                System.out.println("DEBUG - ID parseado: " + idDiagnostico);

                if (idDiagnostico == null) {

                    writeJson(exchange, 400, "{\"error\":\"Invalid ID format\"}");

                    return;

                }

                // Delete diagnostic

                boolean deleted = deleteDiagnostico(idDiagnostico);

                System.out.println("DEBUG - Resultado eliminación: " + deleted);

                if (deleted) {

                    writeJson(exchange, 200, "{\"message\":\"Diagnóstico eliminado exitosamente\"}");

                } else {

                    writeJson(exchange, 404, "{\"error\":\"Diagnóstico no encontrado\"}");

                }

            } catch (Exception e) {

                e.printStackTrace();

                writeJson(exchange, 500, "{\"error\":\"Error interno del servidor\"}");

            }

        }

        private static Integer parseDeleteRequest(String json) {

            try {

                json = json.trim();

                if (!json.startsWith("{") || !json.endsWith("}")) return null;

                json = json.substring(1, json.length() - 1);

                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (String pair : pairs) {

                    String[] kv = pair.split(":", 2);

                    if (kv.length != 2) continue;

                    String key = kv[0].trim().replace("\"", "");

                    String value = kv[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) {

                        value = value.substring(1, value.length() - 1);

                    }

                    if ("id_diagnostico".equals(key)) {

                        return Integer.parseInt(value);

                    }

                }

                return null;

            } catch (Exception e) {

                return null;

            }

        }

        private static boolean deleteDiagnostico(Integer idDiagnostico) {

            String sql = "DELETE FROM diagnostico WHERE id_diagnostico = ?";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, idDiagnostico);

                int rowsAffected = ps.executeUpdate();

                return rowsAffected > 0;

            } catch (SQLException e) {

                e.printStackTrace();

                return false;

            }

        }

    }

    static class DiagnosticoData {

        Integer medico_id_medico; 

        Integer paciente_id_paciente; 

        String observaciones; 

        Date fecha; 

        Time hora;

    }

}

