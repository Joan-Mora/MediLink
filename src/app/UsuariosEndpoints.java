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

 * Endpoints para usuarios del sistema (vinculados a médicos).

 */

public final class UsuariosEndpoints {

    private UsuariosEndpoints() {}

    public static void register(HttpServer server) {

        server.createContext("/usuarios", new UsuariosHandler());

        server.createContext("/usuarios/create", new CreateUsuarioHandler());

    }

    static class UsuariosHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            URI uri = exchange.getRequestURI(); String query = uri.getQuery(); String medico_id = ""; int limit = 50;

            if (query != null) {

                for (String part : query.split("&")) {

                    String[] kv = part.split("=", 2);

                    if (kv.length == 2) {

                        if (kv[0].equals("medico_id")) medico_id = decode(kv[1]);

                        if (kv[0].equals("limit")) { try { limit = Math.min(Math.max(Integer.parseInt(kv[1]), 1), 200); } catch (Exception ignored) {} }

                    }

                }

            }

            List<String> rows = new ArrayList<>();

            String base = "SELECT u.id_usuario, u.medico_id_medico, u.nombre, u.contrasena, m.nombres, m.apellidos, m.especialidad FROM usuario u LEFT JOIN medico m ON u.medico_id_medico = m.id_medico";

            try (Connection c = Db.getConnection()) {

                if (medico_id != null && medico_id.trim().length() > 0) {

                    String sql = base + " WHERE u.medico_id_medico = ? ORDER BY u.id_usuario DESC LIMIT ?";

                    try (PreparedStatement ps = c.prepareStatement(sql)) {

                        ps.setInt(1, Integer.parseInt(medico_id)); ps.setInt(2, limit);

                        try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(asUsuarioJson(rs)); }

                    }

                } else {

                    String sql = base + " ORDER BY u.id_usuario DESC LIMIT ?";

                    try (PreparedStatement ps = c.prepareStatement(sql)) {

                        ps.setInt(1, limit);

                        try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(asUsuarioJson(rs)); }

                    }

                }

            } catch (Exception e) { writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}"); return; }

            writeJson(exchange, 200, "[" + String.join(",", rows) + "]");

        }

        private static String asUsuarioJson(ResultSet rs) throws SQLException {

            return String.format("{\"id_usuario\":%d,\"medico_id_medico\":%d,\"nombre\":\"%s\",\"contrasena\":\"%s\",\"medico\":{\"nombres\":\"%s\",\"apellidos\":\"%s\",\"especialidad\":\"%s\"}}",

                    rs.getInt("id_usuario"), rs.getInt("medico_id_medico"), esc(rs.getString("nombre")), esc(rs.getString("contrasena")), esc(rs.getString("nombres")), esc(rs.getString("apellidos")), esc(rs.getString("especialidad")));

        }

    }

    static class CreateUsuarioHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equals(exchange.getRequestMethod())) { writeJson(exchange, 405, "{\"error\":\"Method not allowed. Use POST\"}"); return; }

            String body = readRequestBody(exchange);

            if (isBlank(body)) { writeJson(exchange, 400, "{\"error\":\"Request body is required\"}"); return; }

            try {

                UsuarioData u = parseJsonUsuario(body);

                if (u == null) { writeJson(exchange, 400, "{\"error\":\"Invalid JSON format\"}"); return; }

                if (isBlank(u.nombre) || isBlank(u.contrasena) || u.medico_id_medico == null) {

                    writeJson(exchange, 400, "{\"error\":\"nombre, contrasena, and medico_id_medico are required\"}"); return; 

                }

                if (!medicoExists(u.medico_id_medico)) { writeJson(exchange, 400, "{\"error\":\"medico_id_medico does not exist\"}"); return; }

                int newId = insertUsuario(u);

                if (newId > 0) writeJson(exchange, 201, "{\"success\":true,\"id_usuario\":" + newId + ",\"message\":\"User created successfully\"}");

                else writeJson(exchange, 500, "{\"error\":\"Failed to create user\"}");

            } catch (Exception e) { writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}"); }

        }

        private static UsuarioData parseJsonUsuario(String json) {

            try {

                UsuarioData u = new UsuarioData(); json = json.trim();

                if (!json.startsWith("{") || !json.endsWith("}")) return null; json = json.substring(1, json.length() - 1);

                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (String pair : pairs) {

                    String[] kv = pair.split(":", 2); if (kv.length != 2) continue;

                    String key = kv[0].trim().replace("\"", ""); String value = kv[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);

                    switch (key) {

                        case "nombre": u.nombre = value; break;

                        case "contrasena": u.contrasena = value; break;

                        case "medico_id_medico": try { u.medico_id_medico = Integer.parseInt(value); } catch (Exception ignored) {} break;

                    }

                }

                return u;

            } catch (Exception e) { return null; }

        }

        private static int insertUsuario(UsuarioData u) {

            String sql = "INSERT INTO usuario (medico_id_medico, nombre, contrasena) VALUES (?, ?, ?)";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, u.medico_id_medico); ps.setString(2, u.nombre); ps.setString(3, u.contrasena);

                int rowsAffected = ps.executeUpdate(); if (rowsAffected > 0) { try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); } }

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

        private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    }

    /** Modelo simple del usuario. */

    static class UsuarioData { Integer medico_id_medico; String nombre; String contrasena; }

}

