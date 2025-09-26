package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.util.ArrayList;

import java.util.List;

import java.util.function.BiConsumer;

public class TogglePacientesView extends JPanel {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    public TogglePacientesView(String pacientesJson, BiConsumer<List<String>, Boolean> onToggle) {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.setBackground(CARD_WHITE);

        headerPanel.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(SUCCESS_GREEN, 2),

                new EmptyBorder(15, 20, 15, 20)

        ));

        JLabel titleLabel = new JLabel("⚡ ACTIVAR/DESACTIVAR PACIENTES");

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        titleLabel.setForeground(SUCCESS_GREEN);

        JLabel warningLabel = new JLabel("<html><i>Los pacientes desactivados mantendrán su historial pero no aparecerán en reportes activos</i></html>");

        warningLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        warningLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);

        headerPanel.add(warningLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BLUE);

        JPanel listPanel = new JPanel();

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        listPanel.setBackground(CARD_WHITE);

        listPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<JCheckBox> checkboxes = new ArrayList<>();

        List<String> ids = new ArrayList<>();

        String[] pacientes = pacientesJson.split("},");

        for (String paciente : pacientes) {

            try {

                String id = extractValue(paciente, "id_paciente");

                String nombres = extractValue(paciente, "nombres");

                String apellidos = extractValue(paciente, "apellidos");

                String nroDocumento = extractValue(paciente, "nro_documento");

                String activoStr = extractValue(paciente, "activo");

                boolean activo = "true".equals(activoStr);

                if (id != null && nombres != null && apellidos != null) {

                    JCheckBox checkbox = new JCheckBox();

                    checkbox.setBackground(CARD_WHITE);

                    checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                    JPanel item = new JPanel(new BorderLayout());

                    item.setBackground(CARD_WHITE);

                    item.setBorder(BorderFactory.createCompoundBorder(

                            BorderFactory.createLineBorder(activo ? SUCCESS_GREEN : Color.ORANGE, 2),

                            new EmptyBorder(10, 10, 10, 10)

                    ));

                    JPanel info = new JPanel(new GridLayout(4, 1, 0, 2));

                    info.setBackground(CARD_WHITE);

                    JLabel name = new JLabel("👤 " + nombres + " " + apellidos);

                    name.setFont(new Font("Segoe UI", Font.BOLD, 14));

                    name.setForeground(TEXT_DARK);

                    JLabel doc = new JLabel("📄 Documento: " + (nroDocumento != null ? nroDocumento : "N/A"));

                    doc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    doc.setForeground(Color.GRAY);

                    JLabel status = new JLabel(activo ? "✅ ACTIVO" : "❌ INACTIVO");

                    status.setFont(new Font("Segoe UI", Font.BOLD, 12));

                    status.setForeground(activo ? SUCCESS_GREEN : Color.ORANGE);

                    JLabel idLabel = new JLabel("🆔 ID: " + id);

                    idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    idLabel.setForeground(Color.GRAY);

                    info.add(name);

                    info.add(doc);

                    info.add(status);

                    info.add(idLabel);

                    item.add(checkbox, BorderLayout.WEST);

                    item.add(info, BorderLayout.CENTER);

                    listPanel.add(item);

                    listPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                    checkboxes.add(checkbox);

                    ids.add(id);

                }

            } catch (Exception ignored) {}

        }

        JScrollPane scrollPane = new JScrollPane(listPanel);

        scrollPane.setPreferredSize(new Dimension(600, 300));

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());

        buttons.setBackground(LIGHT_BLUE);

        buttons.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton selectAll = createActionButton("✅ Seleccionar Todo", PRIMARY_BLUE);

        JButton deselectAll = createActionButton("❌ Deseleccionar Todo", Color.GRAY);

        JButton activar = createActionButton("🟢 Activar Seleccionados", SUCCESS_GREEN);

        JButton desactivar = createActionButton("🟡 Desactivar Seleccionados", Color.ORANGE);

        selectAll.addActionListener(e -> checkboxes.forEach(cb -> cb.setSelected(true)));

        deselectAll.addActionListener(e -> checkboxes.forEach(cb -> cb.setSelected(false)));

        activar.addActionListener(e -> {

            List<String> selected = collectSelected(checkboxes, ids);

            if (selected.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona al menos un paciente para activar", "Sin selección", JOptionPane.WARNING_MESSAGE);

                return;

            }

            onToggle.accept(selected, true);

        });

        desactivar.addActionListener(e -> {

            List<String> selected = collectSelected(checkboxes, ids);

            if (selected.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona al menos un paciente para desactivar", "Sin selección", JOptionPane.WARNING_MESSAGE);

                return;

            }

            int confirm = JOptionPane.showConfirmDialog(this,

                    "¿Estás seguro de desactivar " + selected.size() + " paciente(s)?\nLos pacientes desactivados no aparecerán en reportes activos.",

                    "Confirmar desactivación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) onToggle.accept(selected, false);

        });

        buttons.add(selectAll);

        buttons.add(deselectAll);

        buttons.add(activar);

        buttons.add(desactivar);

        mainPanel.add(buttons, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

    }

    private List<String> collectSelected(List<JCheckBox> cbs, List<String> ids) {

        List<String> out = new ArrayList<>();

        for (int i = 0; i < cbs.size(); i++) if (cbs.get(i).isSelected()) out.add(ids.get(i));

        return out;

    }

    private JButton createActionButton(String text, Color bgColor) {

        JButton button = new JButton(text);

        button.setBackground(bgColor);

        button.setForeground(Color.WHITE);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));

        button.setBorder(new EmptyBorder(8, 15, 8, 15));

        button.setFocusPainted(false);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;

    }

    private String extractValue(String json, String key) {

        try {

            String pattern = "\\\"" + key + "\\\"\\s*:\\s*\\\"?([^,}\\\\\"]+)\\\"?";

            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);

            java.util.regex.Matcher m = p.matcher(json);

            if (m.find()) return m.group(1).trim();

        } catch (Exception ignored) {}

        return null;

    }

}

