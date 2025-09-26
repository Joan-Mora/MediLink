package app;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import static app.HttpUtils.*;

public final class AuthEndpoints {

    private AuthEndpoints() {}

    public static void register(HttpServer server) {

        server.createContext("/auth/login", new LoginHandler());

    }

    static class LoginHandler implements HttpHandler {

        @Override public void handle(HttpExchange exchange) throws IOException {

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                writeJson(exchange, 405, "{\"error\":\"Method not allowed. Use POST\"}");

                return;

            }

            String body = readRequestBody(exchange);

            if (body == null || body.trim().isEmpty()) {

                writeJson(exchange, 400, "{\"error\":\"Body requerido\"}");

                return;

            }

            String nombre = null, contrasena = null;

            try {

                String json = body.trim();

                if (json.startsWith("{") && json.endsWith("}")) json = json.substring(1, json.length()-1);

                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (String pair : pairs) {

                    String[] kv = pair.split(":", 2);

                    if (kv.length != 2) continue;

                    String key = kv[0].trim().replace("\"", "");

                    String value = kv[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);

                    if ("nombre".equals(key)) nombre = value;

                    if ("contrasena".equals(key)) contrasena = value;

                }

            } catch (Exception ignore) {}

            System.out.println("[LOGIN] nombre recibido: [" + nombre + "] contrasena recibida: [" + contrasena + "]");

            if (nombre == null || nombre.isEmpty() || contrasena == null || contrasena.isEmpty()) {

                writeJson(exchange, 400, "{\"error\":\"nombre y contrasena son requeridos\"}");

                return;

            }

            // Admin por defecto (fuera de BD), tolera comillas extra

            String nombreClean = nombre != null ? nombre.replaceAll("[\"' ]", "").trim() : null;

            String passClean = contrasena != null ? contrasena.replaceAll("[\"' ]", "").trim() : null;

            if ("Admin".equalsIgnoreCase(nombreClean) && "Admin2025".equals(passClean)) {

                String resp = "{\"success\":true,\"id_usuario\":0,\"nombre\":\"Admin\",\"medico\":{\"id\":0,\"nombres\":\"Administrador\",\"apellidos\":\"del Sistema\",\"especialidad\":\"Admin\"},\"role\":\"ADMIN\"}";

                writeJson(exchange, 200, resp);

                return;

            }

            String sql = "SELECT u.id_usuario, u.medico_id_medico, u.nombre, m.nombres, m.apellidos, m.especialidad " +

                    "FROM usuario u LEFT JOIN medico m ON m.id_medico = u.medico_id_medico " +

                    "WHERE u.nombre = ? AND u.contrasena = ? LIMIT 1";

            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, nombre);

                ps.setString(2, contrasena);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        String resp = String.format("{\"success\":true,\"id_usuario\":%d,\"nombre\":\"%s\",\"medico\":{\"id\":%d,\"nombres\":\"%s\",\"apellidos\":\"%s\",\"especialidad\":\"%s\"}}",

                                rs.getInt("id_usuario"), esc(rs.getString("nombre")), rs.getInt("medico_id_medico"), esc(rs.getString("nombres")), esc(rs.getString("apellidos")), esc(rs.getString("especialidad")));

                        writeJson(exchange, 200, resp);

                    } else {

                        writeJson(exchange, 401, "{\"success\":false,\"error\":\"Credenciales inválidas\"}");

                    }

                }

            } catch (Exception e) {

                writeJson(exchange, 500, "{\"error\":\"" + safe(e.getMessage()) + "\"}");

            }

        }

    }

}

