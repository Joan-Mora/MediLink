package gui.views;

import gui.models.ApiClient;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

public class AddDiagnosticoView extends JDialog {

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    public AddDiagnosticoView(Frame owner,

                              java.util.function.Consumer<String[]> confirmCallback,

                              java.util.function.Consumer<String> statusCallback,

                              Runnable reloadDashboardStats) {

        super(owner, "Registrar Nuevo Diagnóstico", true);

        setSize(500, 400);

        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.setBackground(CARD_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idMedicoField = new JTextField(20);

    idMedicoField.setToolTipText("Ej: 101");

    addPlaceholder(idMedicoField, "Ej: 101");

        JTextField idPacienteField = new JTextField(20);

    idPacienteField.setToolTipText("Ej: 202");

    addPlaceholder(idPacienteField, "Ej: 202");

        JTextArea observacionesField = new JTextArea(5, 20);

    observacionesField.setToolTipText("Ej: Paciente presenta síntomas de ...");

    addPlaceholder(observacionesField, "Ej: Paciente presenta síntomas de ...");

        observacionesField.setLineWrap(true);

        observacionesField.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3; panel.add(new JLabel("ID Médico:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7; panel.add(idMedicoField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3; panel.add(new JLabel("ID Paciente:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7; panel.add(idPacienteField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3; panel.add(new JLabel("Observaciones:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7; panel.add(new JScrollPane(observacionesField), gbc);

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

                // Validaciones

                if (idMedicoField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo ID Médico es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (idPacienteField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo ID Paciente es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                try {

                    Integer.parseInt(idMedicoField.getText().trim());

                } catch (NumberFormatException ex) {

                    JOptionPane.showMessageDialog(this, "El ID Médico debe ser un número válido.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                try {

                    Integer.parseInt(idPacienteField.getText().trim());

                } catch (NumberFormatException ex) {

                    JOptionPane.showMessageDialog(this, "El ID Paciente debe ser un número válido.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (observacionesField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Observaciones es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                java.time.LocalDate fecha = java.time.LocalDate.now();

                java.time.LocalTime hora = java.time.LocalTime.now();

                String json = String.format(

                        "{\"medico_id_medico\":%s,\"paciente_id_paciente\":%s,\"observaciones\":\"%s\",\"fecha\":\"%s\",\"hora\":\"%s\"}",

                        idMedicoField.getText(), idPacienteField.getText(),

                        escapeJson(observacionesField.getText()), fecha.toString(),

                        hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))

                );

                String response = ApiClient.post("/diagnosticos/create", json);

                // Validar respuesta del backend

                if (response.contains("No existe") || response.contains("no existe") || response.contains("not found") || response.contains("error") || response.contains("null")) {

                    JOptionPane.showMessageDialog(this, "El ID de médico o paciente no existe o no está registrado.\nNo se creó el diagnóstico.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                String detalles = "ID Médico: " + idMedicoField.getText() + "|" +

                        "ID Paciente: " + idPacienteField.getText() + "|" +

                        "Observaciones: " + observacionesField.getText();

                if (confirmCallback != null) confirmCallback.accept(new String[]{"DIAGNÓSTICO", "Registro", "Médico", detalles, null});

                if (statusCallback != null) statusCallback.accept("✅ Diagnóstico registrado correctamente");

                if (reloadDashboardStats != null) reloadDashboardStats.run();

                dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

            }

        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);

        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; panel.add(buttonPanel, gbc);

        add(panel);

    }

    private String escapeJson(String str) {

        if (str == null) return "";

        return str.replace("\"", "\\\"")

                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");

    }

    // Utilidad para placeholder visual en JTextField y JTextArea

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

