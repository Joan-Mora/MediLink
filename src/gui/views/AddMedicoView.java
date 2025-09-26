package gui.views;

import gui.models.ApiClient;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

public class AddMedicoView extends JDialog {

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    public AddMedicoView(Frame owner,

                         java.util.function.Consumer<String[]> confirmCallback,

                         java.util.function.Consumer<String> statusCallback,

                         java.util.function.Function<String[], String[]> createUserCallback,

                         Runnable reloadDashboardStats) {

        super(owner, "Registrar Nuevo Médico", true);

        setSize(400, 350);

        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.setBackground(CARD_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nombresField = new JTextField(20);

    nombresField.setToolTipText("Ej: Ana María");

    addPlaceholder(nombresField, "Ej: Ana María");

        JTextField apellidosField = new JTextField(20);

    apellidosField.setToolTipText("Ej: Rodríguez Pérez");

    addPlaceholder(apellidosField, "Ej: Rodríguez Pérez");

        JTextField especialidadField = new JTextField(20);

    especialidadField.setToolTipText("Ej: Pediatría");

    addPlaceholder(especialidadField, "Ej: Pediatría");

        JTextField nroDocumentoField = new JTextField(20);

    nroDocumentoField.setToolTipText("Ej: 1234567890");

    addPlaceholder(nroDocumentoField, "Ej: 1234567890");

        String[] labels = {"Nombres:", "Apellidos:", "Especialidad:", "Nro. Documento:"};

        JTextField[] fields = {nombresField, apellidosField, especialidadField, nroDocumentoField};

        for (int i = 0; i < labels.length; i++) {

            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;

            JLabel label = new JLabel(labels[i]);

            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            panel.add(label, gbc);

            gbc.gridx = 1; gbc.weightx = 0.7;

            panel.add(fields[i], gbc);

        }

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.setBackground(CARD_WHITE);

        JButton saveButton = new JButton("💾 Guardar");

        saveButton.setBackground(SUCCESS_GREEN);

        saveButton.setForeground(Color.WHITE);

        JButton cancelButton = new JButton("❌ Cancelar");

        cancelButton.setBackground(DANGER_RED);

        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> {

            try {

                String nombres = nombresField.getText().trim();

                String apellidos = apellidosField.getText().trim();

                String especialidad = especialidadField.getText().trim();

                String nroDocumento = nroDocumentoField.getText().trim();

                // Validaciones

                if (nombres.isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Nombres es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (apellidos.isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Apellidos es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (especialidad.isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Especialidad es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (nroDocumento.isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Nro. Documento es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (!nroDocumento.matches("\\d+")) {

                    JOptionPane.showMessageDialog(this, "El número de documento debe contener solo números.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                String medicoJson = String.format(

                        "{\"nombres\":\"%s\",\"apellidos\":\"%s\",\"especialidad\":\"%s\",\"nro_documento\":\"%s\"}",

                        escapeJson(nombres), escapeJson(apellidos), escapeJson(especialidad), escapeJson(nroDocumento)

                );

                String medicoResponse = ApiClient.post("/medicos/create", medicoJson);

                String medicoId = extractIdFromResponse(medicoResponse);

                String detalles = "Especialidad: " + especialidad + "|" +

                        "Documento: " + nroDocumento + "|" +

                        "ID Sistema: " + (medicoId != null ? medicoId : "?");

                // Intentar obtener credenciales del backend (evita duplicación)

                String backendUser = extractStringFromResponse(medicoResponse, "usuario");

                String backendPass = extractStringFromResponse(medicoResponse, "contrasena");

                String cred = null;

                if (backendUser != null && backendPass != null) {

                    cred = "Usuario: " + backendUser + "\nContraseña: " + backendPass + "\n\n💡 Estas credenciales permiten acceso al sistema";

                } else if (medicoId != null && createUserCallback != null) {

                    // Respaldo: crear usuario desde la GUI solo si el backend no lo devolvió

                    String[] result = createUserCallback.apply(new String[]{nombres, apellidos, nroDocumento, medicoId});

                    if (result != null) {

                        cred = "Usuario: " + result[0] + "\nContraseña: " + result[1] + "\n\n💡 Estas credenciales permiten acceso al sistema";

                    }

                }

                if (confirmCallback != null) confirmCallback.accept(new String[]{"MÉDICO" + (cred != null ? " Y USUARIO" : ""), nombres, apellidos, detalles, cred});

                if (statusCallback != null) statusCallback.accept(cred != null ? "✅ Médico y usuario registrados correctamente" : "✅ Médico registrado - Usuario automático falló");

                if (reloadDashboardStats != null) reloadDashboardStats.run();

                dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

            }

        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);

        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;

        panel.add(buttonPanel, gbc);

        add(panel);

    }

    private String extractIdFromResponse(String response) {

        if (response == null) return null;

        int idIndex = response.indexOf("\"id_medico\"");

        if (idIndex == -1) idIndex = response.indexOf("\"id\"");

        if (idIndex == -1) return null;

        int colonIndex = response.indexOf(':', idIndex);

        if (colonIndex == -1) return null;

        int start = colonIndex + 1;

        while (start < response.length() && !Character.isDigit(response.charAt(start))) start++;

        int end = start;

        while (end < response.length() && Character.isDigit(response.charAt(end))) end++;

        return response.substring(start, end);

    }

    private String extractStringFromResponse(String response, String key) {

        if (response == null || key == null) return null;

        int k = response.indexOf("\"" + key + "\"");

        if (k == -1) return null;

        int colon = response.indexOf(':', k);

        if (colon == -1) return null;

        int start = colon + 1;

        // Saltar espacios y comillas iniciales

        while (start < response.length() && (response.charAt(start) == ' ' || response.charAt(start) == '"')) start++;

        int end = start;

        while (end < response.length() && response.charAt(end) != '"' && response.charAt(end) != ',' && response.charAt(end) != '}') end++;

        String raw = response.substring(start, end);

        // Si terminó por comilla, eliminarla si quedó incluida

        if (raw.endsWith("\"")) raw = raw.substring(0, raw.length() - 1);

        return raw.trim().isEmpty() ? null : raw.trim();

    }

    private String escapeJson(String str) {

        if (str == null) return "";

        return str.replace("\"", "\\\"")

                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");

    }

    // Utilidad para placeholder visual en JTextField

    private void addPlaceholder(javax.swing.text.JTextComponent field, String placeholder) {

        Color placeholderColor = new Color(150, 150, 150);

        Color normalColor = Color.BLACK;

        field.setText(placeholder);

        field.setForeground(placeholderColor);

        field.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override

            public void focusGained(java.awt.event.FocusEvent e) {

                if (field.getText().equals(placeholder)) {

                    field.setText("");

                    field.setForeground(normalColor);

                }

            }

            @Override

            public void focusLost(java.awt.event.FocusEvent e) {

                if (field.getText().isEmpty()) {

                    field.setText(placeholder);

                    field.setForeground(placeholderColor);

                }

            }

        });

    }

}

