package app;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import java.io.OutputStream;

import java.net.URLDecoder;

import java.nio.charset.StandardCharsets;

/**

 * Utilidades HTTP y JSON sencillas usadas por los handlers.

 */

public final class HttpUtils {

    private HttpUtils() {}

    public static void writeJson(HttpExchange exchange, int status, String body) throws IOException {

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {

            os.write(bytes);

        }

    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {

        StringBuilder body = new StringBuilder();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(

                new java.io.InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                body.append(line);

            }

        }

        return body.toString();

    }

    public static String decode(String s) {

        try { return URLDecoder.decode(s, "UTF-8"); } catch (Exception e) { return s; }

    }

    public static String esc(String s) {

        if (s == null) return "";

        return s.replace("\\", "\\\\").replace("\"", "\\\"");

    }

    public static String safe(String s) { return esc(s == null ? "" : s); }

}

