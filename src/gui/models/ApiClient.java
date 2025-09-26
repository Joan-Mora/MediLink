package gui.models;

import java.io.*;

import java.net.HttpURLConnection;

import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.util.Map;

/**

 * Cliente API para conectar con el backend MediLink

 * Maneja todas las comunicaciones HTTP con los endpoints REST

 */

public class ApiClient {

    private static volatile String discoveredBaseUrl;

    private static String resolveBaseUrl() {

        // 1) Preferir el valor descubierto en runtime

        String d = discoveredBaseUrl;

        if (d != null) return d;

        // 2) Propiedad del sistema (servidor embebido la expone)

        try {

            String s = System.getProperty("medilink.baseUrl");

            if (s != null && !s.trim().isEmpty()) return s.trim();

        } catch (Throwable ignore) {}

        // 3) Intentar descubrir rápido (health) en puertos 8081-8099

        String probed = probeServer();

        if (probed != null) {

            discoveredBaseUrl = probed;

            return probed;

        }

        // 4) fallback

        return "http://localhost:8081";

    }

    private static String probeServer() {

        int oldConnect = 1200, oldRead = 1200;

        for (int p = 8081; p <= 8099; p++) {

            try {

                URL u = new URL("http://localhost:" + p + "/health");

                HttpURLConnection c = (HttpURLConnection) u.openConnection();

                c.setConnectTimeout(oldConnect);

                c.setReadTimeout(oldRead);

                c.setRequestMethod("GET");

                int code = c.getResponseCode();

                if (code >= 200 && code < 300) {

                    try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {

                        String t = r.readLine();

                        if (t != null && t.contains("\"ok\":true")) return "http://localhost:" + p;

                    }

                }

            } catch (Exception ignore) {}

        }

        return null;

    }

    private static final int TIMEOUT = 8000; // 8 segundos

    /**

     * Realiza una petición GET al endpoint especificado

     * @param endpoint El endpoint relativo (ej: "/medicos")

     * @return La respuesta JSON como String

     * @throws Exception Si hay error en la conexión

     */

    public static String get(String endpoint) throws Exception {

        return request("GET", endpoint, null);

    }

    /**

     * Realiza una petición POST al endpoint especificado

     * @param endpoint El endpoint relativo (ej: "/medicos/create")

     * @param jsonData Los datos JSON a enviar

     * @return La respuesta JSON como String

     * @throws Exception Si hay error en la conexión

     */

    public static String post(String endpoint, String jsonData) throws Exception {

        return request("POST", endpoint, jsonData);

    }

    /**

     * Realiza una petición DELETE al endpoint especificado

     * @param endpoint El endpoint relativo (ej: "/medicos/1")

     * @return La respuesta JSON como String

     * @throws Exception Si hay error en la conexión

     */

    public static String delete(String endpoint) throws Exception {

        return request("DELETE", endpoint, null);

    }

    /**

     * Realiza una petición DELETE con cuerpo JSON al endpoint especificado

     * @param endpoint El endpoint relativo (ej: "/diagnosticos/delete")

     * @param jsonData Datos JSON a enviar en el cuerpo

     * @return La respuesta JSON como String

     * @throws Exception Si hay error en la conexión

     */

    public static String delete(String endpoint, String jsonData) throws Exception {

        return request("DELETE", endpoint, jsonData);

    }

    /**

     * Realiza una petición HTTP genérica

     */

    private static String request(String method, String endpoint, String jsonData) throws Exception {

        String base = resolveBaseUrl();

        URL url = new URL(base + endpoint);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Debug: mostrar la petición

        System.out.println("DEBUG ApiClient - " + method + " " + url);

        if (jsonData != null) {

            System.out.println("DEBUG ApiClient - Body: " + jsonData);

        }

        try {

            // Configurar conexión

            conn.setRequestMethod(method);

            conn.setConnectTimeout(TIMEOUT);

            conn.setReadTimeout(TIMEOUT);

            conn.setRequestProperty("Content-Type", "application/json");

            conn.setRequestProperty("Accept", "application/json");

            // Enviar datos si hay cuerpo JSON (para POST/DELETE/etc.)

            if (jsonData != null) {

                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {

                    byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);

                    os.write(input, 0, input.length);

                }

            }

            // Leer respuesta

            int responseCode = conn.getResponseCode();

            InputStream inputStream;

            if (responseCode >= 200 && responseCode < 300) {

                inputStream = conn.getInputStream();

            } else {

                inputStream = conn.getErrorStream();

            }

            if (inputStream == null) {

                throw new Exception("Error de conexión - código: " + responseCode);

            }

            try (BufferedReader reader = new BufferedReader(

                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {

                    response.append(line).append('\n');

                }

                String responseText = response.toString().trim();

                // Debug: mostrar respuesta

                System.out.println("DEBUG ApiClient - Response Code: " + responseCode);

                System.out.println("DEBUG ApiClient - Response: " + responseText);

                // Si llegamos aquí y no se había fijado discoveredBaseUrl, guardarlo

                if (discoveredBaseUrl == null && base != null) discoveredBaseUrl = base;

                return responseText;

            }

        } finally {

            conn.disconnect();

        }

    }

    public static boolean healthOk() {

        try {

            String resp = get("/health");

            return resp != null && resp.contains("\"ok\":true");

        } catch (Exception e) {

            return false;

        }

    }

    /**

     * Verifica si el backend está disponible

     * @return true si está conectado, false si no

     */

    public static boolean isBackendAvailable() {

        try {

            String response = get("/health");

            return response.contains("\"ok\":true");

        } catch (Exception e) {

            return false;

        }

    }

    /**

     * Escapa caracteres especiales para JSON

     */

    public static String escapeJson(String str) {

        if (str == null) return "null";

        return "\"" + str.replace("\\", "\\\\")

                          .replace("\"", "\\\"")

                          .replace("\n", "\\n")

                          .replace("\r", "\\r")

                          .replace("\t", "\\t") + "\"";

    }

    /**

     * Construye un JSON simple con los parámetros dados

     */

    public static String buildJson(Map<String, String> params) {

        StringBuilder json = new StringBuilder("{");

        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {

            if (!first) json.append(",");

            json.append("\"").append(entry.getKey()).append("\":")

                .append(escapeJson(entry.getValue()));

            first = false;

        }

        json.append("}");

        return json.toString();

    }

}

