package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.awt.event.*;

public class MedicosView extends JPanel {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private String[][] allMedicosData;

    private JPanel medicosCardsContainer;

    public MedicosView() {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

    }

    public void render(String json, int activos, int inactivos) {

        removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BLUE);

        JPanel headerPanel = createMedicosHeader();

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = createStatsCards(activos, inactivos, "Médicos");

        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.setBackground(LIGHT_BLUE);

        centerPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel();

        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));

        cardsContainer.setBackground(LIGHT_BLUE);

        cardsContainer.setBorder(new EmptyBorder(15, 25, 25, 25));

        String[][] data = parseTable(json, new String[]{

            "id_medico","nombres","apellidos","especialidad","activo"

        });

        allMedicosData = data;

        medicosCardsContainer = cardsContainer;

        for (String[] medico : data) {

            JPanel card = createExpandableMedicoCard(medico);

            cardsContainer.add(card);

            cardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        }

        JScrollPane scrollPane = new JScrollPane(cardsContainer);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        revalidate();

        repaint();

    }

    private JPanel createMedicosHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(CARD_WHITE);

        header.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 240)),

            new EmptyBorder(20, 25, 20, 25)

        ));

        JLabel countLabel = new JLabel("Profesionales médicos registrados");

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        countLabel.setForeground(new Color(100, 100, 100));

        JTextField searchField = new JTextField(" Buscar por nombre o especialidad...");

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchField.setForeground(new Color(150, 150, 150));

        searchField.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createLineBorder(new Color(200, 205, 210), 1),

            new EmptyBorder(8, 12, 8, 12)

        ));

        searchField.setPreferredSize(new Dimension(300, 35));

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override

            public void focusGained(java.awt.event.FocusEvent e) {

                if (searchField.getText().equals(" Buscar por nombre o especialidad...")) {

                    searchField.setText("");

                    searchField.setForeground(TEXT_DARK);

                }

            }

            @Override

            public void focusLost(java.awt.event.FocusEvent e) {

                if (searchField.getText().isEmpty()) {

                    searchField.setText(" Buscar por nombre o especialidad...");

                    searchField.setForeground(new Color(150, 150, 150));

                }

            }

        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override

            public void keyReleased(java.awt.event.KeyEvent e) {

                String searchText = searchField.getText().toLowerCase();

                if (searchText.equals(" buscar por nombre o especialidad...")) searchText = "";

                filterMedicos(searchText);

            }

        });

        header.add(countLabel, BorderLayout.WEST);

        header.add(searchField, BorderLayout.EAST);

        return header;

    }

    private void filterMedicos(String searchText) {

        if (allMedicosData == null || medicosCardsContainer == null) return;

        medicosCardsContainer.removeAll();

        java.util.List<String[]> filteredData = new java.util.ArrayList<>();

        for (String[] medico : allMedicosData) {

            String nombres = medico[1].toLowerCase();

            String apellidos = medico[2].toLowerCase();

            String especialidad = medico[3].toLowerCase();

            String id = medico[0].toLowerCase();

            if (searchText.isEmpty() || nombres.contains(searchText) || apellidos.contains(searchText) || especialidad.contains(searchText) || id.contains(searchText)) {

                filteredData.add(medico);

            }

        }

        for (String[] medico : filteredData) {

            JPanel card = createExpandableMedicoCard(medico);

            medicosCardsContainer.add(card);

            medicosCardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        }

        medicosCardsContainer.revalidate();

        medicosCardsContainer.repaint();

    }

    private JPanel createExpandableMedicoCard(String[] datos) {

        JPanel mainCard = new JPanel(new BorderLayout());

        mainCard.setBackground(CARD_WHITE);

        mainCard.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

            BorderFactory.createEmptyBorder()

        ));

        mainCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel compactPanel = createCompactMedicoView(datos);

        mainCard.add(compactPanel, BorderLayout.NORTH);

        JPanel expandedPanel = createExpandedMedicoView(datos);

        expandedPanel.setVisible(false);

        mainCard.add(expandedPanel, BorderLayout.CENTER);

        compactPanel.addMouseListener(new MouseAdapter() {

            @Override

            public void mouseClicked(MouseEvent e) {

                boolean isExpanded = expandedPanel.isVisible();

                expandedPanel.setVisible(!isExpanded);

                JLabel indicator = findIndicatorLabel(compactPanel);

                if (indicator != null) indicator.setText(isExpanded ? "▼" : "▲");

                mainCard.revalidate();

                mainCard.repaint();

            }

            @Override public void mouseEntered(MouseEvent e) { compactPanel.setBackground(new Color(248, 251, 255)); }

            @Override public void mouseExited(MouseEvent e) { compactPanel.setBackground(CARD_WHITE); }

        });

        return mainCard;

    }

    private JPanel createCompactMedicoView(String[] datos) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(CARD_WHITE);

        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftInfo.setOpaque(false);

        JLabel avatar = new JLabel("♦");

        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        avatar.setForeground(SUCCESS_GREEN);

        avatar.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.setOpaque(false);

        String nombreCompleto = datos[1] + " " + datos[2];

        JLabel nombreLabel = new JLabel(nombreCompleto);

        nombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        nombreLabel.setForeground(TEXT_DARK);

        String especialidad = "Especialidad: " + datos[3];

        JLabel especialidadLabel = new JLabel(especialidad);

        especialidadLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        especialidadLabel.setForeground(new Color(120, 120, 120));

        boolean activo = "true".equals(datos[4]);

        String estadoTexto = activo ? "● ACTIVO" : "● INACTIVO";

        JLabel estadoLabel = new JLabel(estadoTexto);

        estadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        estadoLabel.setForeground(activo ? SUCCESS_GREEN : Color.RED);

        infoPanel.add(nombreLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(especialidadLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(estadoLabel);

        leftInfo.add(avatar);

        leftInfo.add(infoPanel);

        JLabel expandIndicator = new JLabel("▼");

        expandIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        expandIndicator.setForeground(new Color(150, 150, 150));

        panel.add(leftInfo, BorderLayout.CENTER);

        panel.add(expandIndicator, BorderLayout.EAST);

        return panel;

    }

    private JPanel createExpandedMedicoView(String[] datos) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(new Color(248, 251, 255));

        panel.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)),

            new EmptyBorder(20, 25, 20, 25)

        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 15);

        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"🆔 ID Médico:", "📋 Especialidad completa:"};

        String[] values = {datos[0], datos[3]};

        for (int i = 0; i < labels.length; i++) {

            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;

            JLabel labelField = new JLabel(labels[i]);

            labelField.setFont(new Font("Segoe UI", Font.BOLD, 12));

            labelField.setForeground(new Color(80, 80, 80));

            panel.add(labelField, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;

            JLabel valueField = new JLabel(values[i]);

            valueField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            valueField.setForeground(TEXT_DARK);

            panel.add(valueField, gbc);

        }

        return panel;

    }

    private JLabel findIndicatorLabel(Container container) {

        for (Component comp : container.getComponents()) {

            if (comp instanceof JLabel && ("▼".equals(((JLabel) comp).getText()) || "▲".equals(((JLabel) comp).getText()))) {

                return (JLabel) comp;

            } else if (comp instanceof Container) {

                JLabel found = findIndicatorLabel((Container) comp);

                if (found != null) return found;

            }

        }

        return null;

    }

    private JPanel createStatsCards(int activos, int inactivos, String tipo) {

        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));

        panel.setBackground(LIGHT_BLUE);

        panel.setBorder(new EmptyBorder(20, 25, 10, 25));

        panel.add(createStatsCard(" Activos", String.valueOf(activos), "Registros habilitados", new Color(67, 160, 71)));

        panel.add(createStatsCard(" Inactivos", String.valueOf(inactivos), "Registros deshabilitados", new Color(244, 67, 54)));

        return panel;

    }

    private JPanel createStatsCard(String title, String value, String description, Color accentColor) {

        JPanel card = new JPanel(new BorderLayout());

        card.setBackground(CARD_WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

            new EmptyBorder(16, 16, 16, 16)

        ));

        JPanel colorStrip = new JPanel();

        colorStrip.setBackground(accentColor);

        colorStrip.setPreferredSize(new Dimension(0, 4));

        JPanel contentPanel = new JPanel();

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.setOpaque(false);

        JLabel tituloLabel = new JLabel(title);

        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tituloLabel.setForeground(new Color(80, 80, 80));

        tituloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valorLabel = new JLabel(value);

        valorLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));

        valorLabel.setForeground(accentColor);

        valorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcionLabel = new JLabel(description);

        descripcionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        descripcionLabel.setForeground(new Color(120, 120, 120));

        descripcionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        contentPanel.add(tituloLabel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        contentPanel.add(valorLabel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        contentPanel.add(descripcionLabel);

        card.add(colorStrip, BorderLayout.NORTH);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;

    }

    private String[][] parseTable(String json, String[] keys) {

        if (json == null || json.length() < 2) return new String[0][0];

        String trimmed = json.trim();

        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return new String[0][0];

        String body = trimmed.substring(1, trimmed.length() - 1).trim();

        if (body.isEmpty()) return new String[0][0];

        String[] objects = body.split("\\},\\s*\\{");

        String[][] table = new String[objects.length][keys.length];

        for (int i = 0; i < objects.length; i++) {

            String obj = objects[i];

            if (!obj.startsWith("{")) obj = "{" + obj;

            if (!obj.endsWith("}")) obj = obj + "}";

            for (int k = 0; k < keys.length; k++) {

                String v = extractJsonValue(obj, keys[k]);

                table[i][k] = v == null ? "" : v;

            }

        }

        return table;

    }

    private String extractJsonValue(String json, String key) {

        String pattern = "\"" + key + "\"" + ":";

        int idx = json.indexOf(pattern);

        if (idx == -1) return null;

        int start = idx + pattern.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start >= json.length()) return null;

        char c = json.charAt(start);

        if (c == '"') {

            int end = start + 1;

            StringBuilder sb = new StringBuilder();

            boolean escape = false;

            for (; end < json.length(); end++) {

                char ch = json.charAt(end);

                if (escape) { sb.append(ch); escape = false; continue; }

                if (ch == '\\') { escape = true; continue; }

                if (ch == '"') break;

                sb.append(ch);

            }

            return sb.toString();

        } else {

            int end = start;

            while (end < json.length() && ",}\n\r\t ".indexOf(json.charAt(end)) == -1) end++;

            return json.substring(start, end);

        }

    }

}

