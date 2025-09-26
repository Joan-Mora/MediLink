package gui.views;

import gui.models.ApiClient;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

public class AddPacienteView extends JDialog {

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    public AddPacienteView(Frame owner,

                           java.util.function.Consumer<String[]> confirmCallback,

                           java.util.function.Consumer<String> statusCallback,

                           Runnable reloadDashboardStats) {

        super(owner, "Registrar Nuevo Paciente", true);

        setSize(500, 600);

        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.setBackground(CARD_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nombresField = new JTextField(20);

        nombresField.setToolTipText("Ej: Juan Carlos");

        addPlaceholder(nombresField, "Ej: Juan Carlos");

        JTextField apellidosField = new JTextField(20);

        apellidosField.setToolTipText("Ej: Pérez Gómez");

        addPlaceholder(apellidosField, "Ej: Pérez Gómez");

        JTextField edadField = new JTextField(20);

        edadField.setToolTipText("Ej: 35");

        addNumericPlaceholder(edadField, "Ej: 35");

        JComboBox<String> generoField = new JComboBox<>(new String[]{"M", "F"});

    generoField.setToolTipText("Seleccione el género: M (Masculino), F (Femenino)");

        JTextField correoField = new JTextField(20);

        correoField.setToolTipText("Ej: juan@email.com");

        addPlaceholder(correoField, "Ej: juan@email.com");

        JTextField direccionField = new JTextField(20);

        direccionField.setToolTipText("Ej: Calle 123 #45-67");

        addPlaceholder(direccionField, "Ej: Calle 123 #45-67");

        JComboBox<String> tipoDocField = new JComboBox<>(new String[]{"C.C (Cedula de Ciudadania)", "T.I (Tarjeta de Identidad)", "C.E (Cedula de Extranjeria)", "R.C (Registro Civil)", "PPT (Permiso Por Proteccion Temporal)"});

    tipoDocField.setToolTipText("Seleccione el tipo de documento");

        JTextField nroDocField = new JTextField(20);

        nroDocField.setToolTipText("Ej: 1234567890");

        addNumericPlaceholder(nroDocField, "Ej: 1234567890");

        JTextField contactoField = new JTextField(20);

        contactoField.setToolTipText("Ej: 3001234567");

        addNumericPlaceholder(contactoField, "Ej: 3001234567");

        String[] labels = {"Nombres:", "Apellidos:", "Edad:", "Género:", "Correo:",

                "Dirección:", "Tipo Documento:", "Nro. Documento:", "Contacto:"};

        JComponent[] fields = {nombresField, apellidosField, edadField, generoField,

                correoField, direccionField, tipoDocField, nroDocField, contactoField};

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

                // Validaciones

                if (nombresField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Nombres es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (apellidosField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Apellidos es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (edadField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Edad es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                try {

                    int edad = Integer.parseInt(edadField.getText().trim());

                    if (edad < 0 || edad > 120) {

                        JOptionPane.showMessageDialog(this, "La edad debe estar entre 0 y 120.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                        return;

                    }

                } catch (NumberFormatException ex) {

                    JOptionPane.showMessageDialog(this, "La edad debe ser un número válido.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (generoField.getSelectedItem() == null) {

                    JOptionPane.showMessageDialog(this, "Debe seleccionar un género.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (tipoDocField.getSelectedItem() == null) {

                    JOptionPane.showMessageDialog(this, "Debe seleccionar un tipo de documento.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (nroDocField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Nro. Documento es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (contactoField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Contacto es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                if (direccionField.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this, "El campo Dirección es obligatorio.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                // Validación de correo: solo rechazar si hay espacios o está vacío cuando es obligatorio

                if (!correoField.getText().trim().isEmpty() && correoField.getText().contains(" ")) {

                    JOptionPane.showMessageDialog(this, "El correo no debe contener espacios.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                // Validar Nro Documento numérico

                if (!nroDocField.getText().trim().matches("^\\d+$")) {

                    JOptionPane.showMessageDialog(this, "El campo Nro. Documento debe contener solo números.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                // Validar Contacto numérico

                if (!contactoField.getText().trim().matches("^\\d+$")) {

                    JOptionPane.showMessageDialog(this, "El campo Contacto debe contener solo números.", "Error de validación", JOptionPane.ERROR_MESSAGE);

                    return;

                }

                String json = String.format(

                        "{\"nombres\":\"%s\",\"apellidos\":\"%s\",\"edad\":\"%s\",\"genero\":\"%s\"," +

                                "\"correo\":\"%s\",\"direccion\":\"%s\",\"tipo_documento\":\"%s\"," +

                                "\"nro_documento\":\"%s\",\"nro_contacto\":\"%s\"}",

                        escapeJson(nombresField.getText()), escapeJson(apellidosField.getText()),

                        escapeJson(edadField.getText()), escapeJson(generoField.getSelectedItem().toString()),

                        escapeJson(correoField.getText()), escapeJson(direccionField.getText()),

                        escapeJson(tipoDocField.getSelectedItem() != null ? tipoDocField.getSelectedItem().toString() : ""),

                        escapeJson(nroDocField.getText()),

                        escapeJson(contactoField.getText())

                );

        ApiClient.post("/pacientes/create", json);

        String detalles = 

            "Edad: " + edadField.getText() + "|" +

            "Género: " + generoField.getSelectedItem().toString() + "|" +

            "Documento: " + (tipoDocField.getSelectedItem() != null ? tipoDocField.getSelectedItem().toString() : "") + "|" +

            "Contacto: " + contactoField.getText() + "|" +

            "Correo: " + correoField.getText() + "|" +

            "Dirección: " + direccionField.getText();

        if (confirmCallback != null) {

            confirmCallback.accept(new String[]{

                "PACIENTE",

                nombresField.getText(),

                apellidosField.getText(),

                detalles,

                null

            });

        }

        if (statusCallback != null) statusCallback.accept("✅ Paciente registrado correctamente");

        if (reloadDashboardStats != null) reloadDashboardStats.run();

        dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),

                        "Error", JOptionPane.ERROR_MESSAGE);

            }

        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);

        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;

        panel.add(buttonPanel, gbc);

        add(panel);

    }

    private String escapeJson(String str) {

        if (str == null) return "";

        return str.replace("\"", "\\\"")

                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");

    }

    // Placeholder visual para campos numéricos: borra placeholder al primer intento de escribir

    private void addNumericPlaceholder(javax.swing.JTextField field, String placeholder) {

        Color placeholderColor = new Color(150, 150, 150);

        Color normalColor = Color.BLACK;

        field.setText(placeholder);

        field.setForeground(placeholderColor);

        final boolean[] showingPlaceholder = {true};

        field.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override

            public void focusGained(java.awt.event.FocusEvent e) {

                if (showingPlaceholder[0]) {

                    field.setText("");

                    field.setForeground(normalColor);

                    showingPlaceholder[0] = false;

                }

            }

            @Override

            public void focusLost(java.awt.event.FocusEvent e) {

                if (field.getText().isEmpty()) {

                    showingPlaceholder[0] = true;

                    field.setText(placeholder);

                    field.setForeground(placeholderColor);

                }

            }

        });

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

