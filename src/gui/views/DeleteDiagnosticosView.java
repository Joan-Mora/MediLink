package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.util.ArrayList;

import java.util.List;

import java.util.function.Consumer;

public class DeleteDiagnosticosView extends JPanel {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    public DeleteDiagnosticosView(String diagnosticosJson, Consumer<List<String>> onDelete) {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(CARD_WHITE);

        header.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(DANGER_RED, 2),

                new EmptyBorder(15, 20, 15, 20)

        ));

        JLabel title = new JLabel("ELIMINAR DIAGNÓSTICOS");

        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        title.setForeground(DANGER_RED);

        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        listPanel.setBackground(CARD_WHITE);

        listPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<JCheckBox> cbs = new ArrayList<>();

        List<String> ids = new ArrayList<>();

        for (String diagnostico : extractTopLevelObjects(diagnosticosJson)) {

            try {

                String id = extractValue(diagnostico, "id_diagnostico");

                String observaciones = extractValue(diagnostico, "observaciones");

                String fecha = extractValue(diagnostico, "fecha");

                String pacienteNombres = extractNestedValue(diagnostico, "paciente", "nombres");

                String pacienteApellidos = extractNestedValue(diagnostico, "paciente", "apellidos");

                String medicoNombres = extractNestedValue(diagnostico, "medico", "nombres");

                String medicoApellidos = extractNestedValue(diagnostico, "medico", "apellidos");

                if (id != null && observaciones != null) {

                    JCheckBox cb = new JCheckBox();

                    cb.setBackground(CARD_WHITE);

                    cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                    JPanel item = new JPanel(new BorderLayout());

                    item.setBackground(CARD_WHITE);

                    item.setBorder(BorderFactory.createCompoundBorder(

                            BorderFactory.createLineBorder(new Color(230,235,240), 1),

                            new EmptyBorder(10, 10, 10, 10)

                    ));

                    JPanel info = new JPanel(new GridLayout(4,1,0,2));

                    info.setBackground(CARD_WHITE);

                    JLabel desc = new JLabel("" + (observaciones.length()>50? observaciones.substring(0,50)+"...":observaciones));

                    desc.setFont(new Font("Segoe UI", Font.BOLD, 14));

                    desc.setForeground(TEXT_DARK);

                    JLabel f = new JLabel(" Fecha: " + (fecha!=null?fecha:"N/A"));

                    f.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    f.setForeground(Color.GRAY);

                    JLabel pac = new JLabel(" Paciente: " + ((pacienteNombres!=null?pacienteNombres:"") + " " + (pacienteApellidos!=null?pacienteApellidos:"")).trim());

                    pac.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    pac.setForeground(Color.GRAY);

                    JLabel idLine = new JLabel(" ID: " + id + " |  Médico: " + ((medicoNombres!=null?medicoNombres:"") + " " + (medicoApellidos!=null?medicoApellidos:"")).trim());

                    idLine.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    idLine.setForeground(Color.GRAY);

                    info.add(desc);

                    info.add(f);

                    info.add(pac);

                    info.add(idLine);

                    item.add(cb, BorderLayout.WEST);

                    item.add(info, BorderLayout.CENTER);

                    listPanel.add(item);

                    listPanel.add(Box.createRigidArea(new Dimension(0,10)));

                    cbs.add(cb);

                    ids.add(id);

                }

            } catch (Exception ignored) {}

        }

        JScrollPane scroll = new JScrollPane(listPanel);

        scroll.setPreferredSize(new Dimension(600,300));

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout());

        actions.setBackground(LIGHT_BLUE);

        actions.setBorder(new EmptyBorder(15,0,0,0));

        JButton selectAll = createActionButton("✅ Seleccionar Todo", PRIMARY_BLUE);

        JButton deselectAll = createActionButton("❌ Deseleccionar Todo", Color.GRAY);

        JButton deleteBtn = createActionButton(" Eliminar Seleccionados", DANGER_RED);

        selectAll.addActionListener(e -> cbs.forEach(cb -> cb.setSelected(true)));

        deselectAll.addActionListener(e -> cbs.forEach(cb -> cb.setSelected(false)));

        deleteBtn.addActionListener(e -> {

            List<String> selected = collectSelected(cbs, ids);

            if (selected.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona al menos un diagnóstico para eliminar", "Sin selección", JOptionPane.WARNING_MESSAGE);

                return;

            }

            int confirm = JOptionPane.showConfirmDialog(this,

                    "¿Estás seguro de eliminar " + selected.size() + " diagnóstico(s)?",

                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) onDelete.accept(selected);

        });

        actions.add(selectAll);

        actions.add(deselectAll);

        actions.add(deleteBtn);

        add(actions, BorderLayout.SOUTH);

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

            // Caso string JSON: "key":"..." capturando escapes

            String strPattern = "\\\"" + key + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"";

            java.util.regex.Pattern ps = java.util.regex.Pattern.compile(strPattern);

            java.util.regex.Matcher ms = ps.matcher(json);

            if (ms.find()) {

                String val = ms.group(1);

                return val.replace("\\\"", "\"")

                          .replace("\\n", "\n")

                          .replace("\\r", "\r")

                          .replace("\\t", "\t")

                          .replace("\\\\", "\\");

            }

            // Caso numérico simple: "key":123

            String numPattern = "\\\"" + key + "\\\"\\s*:\\s*([0-9]+)";

            java.util.regex.Pattern pn = java.util.regex.Pattern.compile(numPattern);

            java.util.regex.Matcher mn = pn.matcher(json);

            if (mn.find()) return mn.group(1).trim();

        } catch (Exception ignored) {}

        return null;

    }

    private String extractNestedValue(String json, String parentKey, String childKey) {

        try {

            String parentPattern = "\\\"" + parentKey + "\\\"\\s*:\\s*\\{([^}]+)\\}";

            java.util.regex.Pattern parentP = java.util.regex.Pattern.compile(parentPattern);

            java.util.regex.Matcher parentM = parentP.matcher(json);

            if (parentM.find()) {

                String parentContent = parentM.group(1);

                return extractValue(parentContent, childKey);

            }

        } catch (Exception ignored) {}

        return null;

    }

    private List<String> extractTopLevelObjects(String jsonArray) {

        List<String> objects = new ArrayList<>();

        if (jsonArray == null) return objects;

        String t = jsonArray.trim();

        if (!t.startsWith("[") || !t.endsWith("]")) return objects;

        int depth = 0; int start = -1;

        for (int i = 0; i < t.length(); i++) {

            char c = t.charAt(i);

            if (c == '{') {

                if (depth == 0) start = i;

                depth++;

            } else if (c == '}') {

                depth--;

                if (depth == 0 && start >= 0) {

                    objects.add(t.substring(start, i + 1));

                    start = -1;

                }

            }

        }

        return objects;

    }

}

