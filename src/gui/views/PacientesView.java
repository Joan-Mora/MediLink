package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.awt.event.*;

public class PacientesView extends JPanel {

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private String[][] allPacientesData;

    private JPanel pacientesCardsContainer;

    public PacientesView() {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

    }

    public void render(String json, int activos, int inactivos) {

        removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BLUE);

        JPanel headerPanel = createPacientesHeader();

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = createStatsCards(activos, inactivos, "Pacientes");

        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.setBackground(LIGHT_BLUE);

        centerPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel();

        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));

        cardsContainer.setBackground(LIGHT_BLUE);

        cardsContainer.setBorder(new EmptyBorder(15, 25, 25, 25));

        String[][] data = parseTable(json, new String[]{

            "id_paciente","nombres","apellidos","edad","genero",

            "correo","direccion","tipo_documento","nro_documento","nro_contacto","activo"

        });

        allPacientesData = data;

        pacientesCardsContainer = cardsContainer;

        for (String[] paciente : data) {

            JPanel card = createExpandablePacienteCard(paciente);

            cardsContainer.add(card);

            cardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        }

        JScrollPane scrollPane = new JScrollPane(cardsContainer);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setBlockIncrement(50);

        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        scrollPane.setPreferredSize(new Dimension(0, 400));

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        revalidate();

        repaint();

    }

    private JPanel createPacientesHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(CARD_WHITE);

        header.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 240)),

            new EmptyBorder(20, 25, 20, 25)

        ));

        JLabel countLabel = new JLabel("Total de pacientes registrados");

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        countLabel.setForeground(new Color(100, 100, 100));

        JTextField searchField = new JTextField("Buscar por nombre o documento...");

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

                if (searchField.getText().equals("Buscar por nombre o documento...")) {

                    searchField.setText("");

                    searchField.setForeground(TEXT_DARK);

                }

            }

            @Override

            public void focusLost(java.awt.event.FocusEvent e) {

                if (searchField.getText().isEmpty()) {

                    searchField.setText("Buscar por nombre o documento...");

                    searchField.setForeground(new Color(150, 150, 150));

                }

            }

        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override

            public void keyReleased(java.awt.event.KeyEvent e) {

                String searchText = searchField.getText().toLowerCase();

                if (searchText.equals("buscar por nombre o documento...")) searchText = "";

                filterPacientes(searchText);

            }

        });

        header.add(countLabel, BorderLayout.WEST);

        header.add(searchField, BorderLayout.EAST);

        return header;

    }

    private void filterPacientes(String searchText) {

        if (allPacientesData == null || pacientesCardsContainer == null) return;

        pacientesCardsContainer.removeAll();

        java.util.List<String[]> filteredData = new java.util.ArrayList<>();

        for (String[] paciente : allPacientesData) {

            String nombres = paciente[1].toLowerCase();

            String apellidos = paciente[2].toLowerCase();

            String documento = paciente[8].toLowerCase();

            String id = paciente[0].toLowerCase();

            if (searchText.isEmpty() || nombres.contains(searchText) || apellidos.contains(searchText) || documento.contains(searchText) || id.contains(searchText)) {

                filteredData.add(paciente);

            }

        }

        for (String[] paciente : filteredData) {

            JPanel card = createExpandablePacienteCard(paciente);

            pacientesCardsContainer.add(card);

            pacientesCardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        }

        pacientesCardsContainer.revalidate();

        pacientesCardsContainer.repaint();

    }

    private JPanel createExpandablePacienteCard(String[] datos) {

        JPanel mainCard = new JPanel(new BorderLayout());

        mainCard.setBackground(CARD_WHITE);

        mainCard.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

            BorderFactory.createEmptyBorder()

        ));

        mainCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel compactPanel = createCompactPacienteView(datos);

        mainCard.add(compactPanel, BorderLayout.NORTH);

        JPanel expandedPanel = createExpandedPacienteView(datos);

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

    private JPanel createCompactPacienteView(String[] datos) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(CARD_WHITE);

        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftInfo.setOpaque(false);

        JLabel avatar = new JLabel("●");

        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        avatar.setForeground(PRIMARY_BLUE);

        avatar.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.setOpaque(false);

        String nombreCompleto = datos[1] + " " + datos[2];

        JLabel nombreLabel = new JLabel(nombreCompleto);

        nombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        nombreLabel.setForeground(TEXT_DARK);

        String infoBasica = datos[7] + " " + datos[8] + " • " + datos[3] + " años • " + datos[4];

        JLabel infoLabel = new JLabel(infoBasica);

        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        infoLabel.setForeground(new Color(120, 120, 120));

        boolean activo = "true".equals(datos[10]);

        String estadoTexto = activo ? "● ACTIVO" : "● INACTIVO";

        JLabel estadoLabel = new JLabel(estadoTexto);

        estadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        estadoLabel.setForeground(activo ? SUCCESS_GREEN : Color.RED);

        infoPanel.add(nombreLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(infoLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(estadoLabel);

        leftInfo.add(avatar);

        leftInfo.add(infoPanel);

        JLabel expandIndicator = new JLabel("▼");

        expandIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        expandIndicator.setForeground(new Color(150, 150, 150));

        expandIndicator.setBorder(new EmptyBorder(0, 10, 0, 0));

        panel.add(leftInfo, BorderLayout.CENTER);

        panel.add(expandIndicator, BorderLayout.EAST);

        return panel;

    }

    private JPanel createExpandedPacienteView(String[] datos) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(new Color(248, 251, 255));

        panel.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)),

            new EmptyBorder(20, 25, 20, 25)

        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 15);

        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"● Correo:", "● Dirección:", "● Contacto:", "● ID Paciente:"};

        String[] values = {datos[5], datos[6], datos[9], datos[0]};

        for (int i = 0; i < labels.length; i++) {

            gbc.gridx = (i % 2) * 2; gbc.gridy = i / 2; gbc.weightx = 0;

            JLabel labelField = new JLabel(labels[i]);

            labelField.setFont(new Font("Segoe UI", Font.BOLD, 12));

            labelField.setForeground(new Color(80, 80, 80));

            panel.add(labelField, gbc);

            gbc.gridx++; gbc.weightx = 1.0;

            JLabel valueField = new JLabel(values[i] != null && !values[i].isEmpty() ? values[i] : "No especificado");

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

    // Utilidades (idénticas a las del monolito para no cambiar comportamiento)

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

