package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.awt.event.KeyAdapter;

import java.awt.event.KeyEvent;

public class DiagnosticosView extends JPanel {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private JPanel diagnosticosCardsContainer;

    private String[][] allDiagnosticosData;

    public DiagnosticosView() {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

    }

    public void render(String json) {

        removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BLUE);

        JPanel headerPanel = createDiagnosticosHeader();

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel();

        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));

        cardsContainer.setBackground(LIGHT_BLUE);

        cardsContainer.setBorder(new EmptyBorder(15, 25, 25, 25));

        String[][] data = parseTable(json, new String[]{

                "id_diagnostico","medico_id_medico","paciente_id_paciente","observaciones","fecha","hora"

        });

        allDiagnosticosData = data;

        diagnosticosCardsContainer = cardsContainer;

        for (String[] diagnostico : data) {

            JPanel card = createExpandableDiagnosticoCard(diagnostico);

            cardsContainer.add(card);

            cardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        }

        JScrollPane scrollPane = new JScrollPane(cardsContainer);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setBlockIncrement(64);

        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        revalidate();

        repaint();

    }

    private JPanel createDiagnosticosHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(CARD_WHITE);

        header.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 235, 240)),

                new EmptyBorder(20, 25, 20, 25)

        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftPanel.setOpaque(false);

        JLabel icon = new JLabel("📋");

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel textPanel = new JPanel();

        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Diagnósticos Médicos");

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        titleLabel.setForeground(TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Historiales y observaciones clínicas");

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        subtitleLabel.setForeground(new Color(120, 120, 120));

        textPanel.add(titleLabel);

        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        textPanel.add(subtitleLabel);

        leftPanel.add(icon);

        leftPanel.add(textPanel);

        JTextField searchField = new JTextField(" Buscar por ID, observaciones...");

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        searchField.setForeground(new Color(150, 150, 150));

        searchField.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),

                new EmptyBorder(8, 12, 8, 12)

        ));

        searchField.setBackground(new Color(250, 250, 250));

        searchField.setPreferredSize(new Dimension(280, 35));

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override

            public void focusGained(java.awt.event.FocusEvent e) {

                if (searchField.getText().equals(" Buscar por ID, observaciones...")) {

                    searchField.setText("");

                    searchField.setForeground(TEXT_DARK);

                }

            }

            @Override

            public void focusLost(java.awt.event.FocusEvent e) {

                if (searchField.getText().isEmpty()) {

                    searchField.setText(" Buscar por ID, observaciones...");

                    searchField.setForeground(new Color(150, 150, 150));

                }

            }

        });

        searchField.addKeyListener(new KeyAdapter() {

            @Override

            public void keyReleased(KeyEvent e) {

                String searchText = searchField.getText().toLowerCase();

                if (searchText.equals(" buscar por id, observaciones...")) searchText = "";

                filterDiagnosticos(searchText);

            }

        });

        header.add(leftPanel, BorderLayout.WEST);

        header.add(searchField, BorderLayout.EAST);

        return header;

    }

    private void filterDiagnosticos(String searchText) {

        diagnosticosCardsContainer.removeAll();

        boolean isNumeric = searchText.matches("\\d+");

        for (String[] diagnostico : allDiagnosticosData) {

            if (isNumeric) {

                // Buscar solo por ID exacto

                if (diagnostico[0].equals(searchText)) {

                    JPanel card = createExpandableDiagnosticoCard(diagnostico);

                    diagnosticosCardsContainer.add(card);

                    diagnosticosCardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

                }

            } else {

                // Buscar en observaciones (campo 3) y resto de campos

                String combined = String.join(" ", diagnostico).toLowerCase();

                if (combined.contains(searchText)) {

                    JPanel card = createExpandableDiagnosticoCard(diagnostico);

                    diagnosticosCardsContainer.add(card);

                    diagnosticosCardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

                }

            }

        }

        diagnosticosCardsContainer.revalidate();

        diagnosticosCardsContainer.repaint();

    }

    private JPanel createExpandableDiagnosticoCard(String[] datos) {

        JPanel mainCard = new JPanel(new BorderLayout());

        mainCard.setBackground(CARD_WHITE);

        mainCard.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

                BorderFactory.createEmptyBorder()

        ));

        mainCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel compactPanel = createCompactDiagnosticoView(datos);

        mainCard.add(compactPanel, BorderLayout.NORTH);

        JPanel expandedPanel = createExpandedDiagnosticoView(datos);

        expandedPanel.setVisible(false);

        mainCard.add(expandedPanel, BorderLayout.CENTER);

        compactPanel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override

            public void mouseClicked(java.awt.event.MouseEvent e) {

                boolean isExpanded = expandedPanel.isVisible();

                expandedPanel.setVisible(!isExpanded);

                JLabel indicator = findIndicatorLabel(compactPanel);

                if (indicator != null) indicator.setText(isExpanded ? "▼" : "▲");

                mainCard.revalidate();

                mainCard.repaint();

            }

            @Override

            public void mouseEntered(java.awt.event.MouseEvent e) {

                compactPanel.setBackground(new Color(248, 251, 255));

            }

            @Override

            public void mouseExited(java.awt.event.MouseEvent e) {

                compactPanel.setBackground(CARD_WHITE);

            }

        });

        return mainCard;

    }

    private JPanel createCompactDiagnosticoView(String[] datos) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(CARD_WHITE);

        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftInfo.setOpaque(false);

        JLabel icon = new JLabel("📋");

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        icon.setForeground(new Color(67, 160, 71));

        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.setOpaque(false);

        JLabel diagnosticoLabel = new JLabel("Diagnóstico ID: " + datos[0]);

        diagnosticoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        diagnosticoLabel.setForeground(TEXT_DARK);

        String resumen = "● " + datos[4] + " ● " + datos[5] + " | ● Dr. ID:" + datos[1] + " | ● Paciente ID:" + datos[2];

        JLabel resumenLabel = new JLabel(resumen);

        resumenLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        resumenLabel.setForeground(new Color(120, 120, 120));

        String observaciones = datos[3];

        if (observaciones.length() > 80) observaciones = observaciones.substring(0, 77) + "...";

        JLabel observacionesLabel = new JLabel("● " + observaciones);

        observacionesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        observacionesLabel.setForeground(new Color(100, 100, 100));

        infoPanel.add(diagnosticoLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(resumenLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(observacionesLabel);

        leftInfo.add(icon);

        leftInfo.add(infoPanel);

        JLabel expandIndicator = new JLabel("▼");

        expandIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        expandIndicator.setForeground(new Color(150, 150, 150));

        panel.add(leftInfo, BorderLayout.CENTER);

        panel.add(expandIndicator, BorderLayout.EAST);

        return panel;

    }

    private JPanel createExpandedDiagnosticoView(String[] datos) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(new Color(248, 251, 255));

        panel.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)),

                new EmptyBorder(20, 25, 20, 25)

        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 5, 8, 15);

        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"● ID Diagnóstico:", "● ID Médico:", "● ID Paciente:", "● Fecha:", "● Hora:", "● Observaciones:"};

        String[] values = {datos[0], datos[1], datos[2], datos[4], datos[5], datos[3]};

        for (int i = 0; i < labels.length; i++) {

            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;

            JLabel labelField = new JLabel(labels[i]);

            labelField.setFont(new Font("Segoe UI", Font.BOLD, 12));

            labelField.setForeground(new Color(80, 80, 80));

            panel.add(labelField, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;

            if (i == 5) {

                JTextArea observacionesArea = new JTextArea(values[i]);

                observacionesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                observacionesArea.setForeground(TEXT_DARK);

                observacionesArea.setBackground(new Color(248, 251, 255));

                observacionesArea.setEditable(false);

                observacionesArea.setLineWrap(true);

                observacionesArea.setWrapStyleWord(true);

                observacionesArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

                JScrollPane scrollPane = new JScrollPane(observacionesArea);

                scrollPane.setPreferredSize(new Dimension(400, 80));

                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                panel.add(scrollPane, gbc);

            } else {

                JLabel valueField = new JLabel(values[i]);

                valueField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                valueField.setForeground(TEXT_DARK);

                panel.add(valueField, gbc);

            }

        }

        return panel;

    }

    private JLabel findIndicatorLabel(JPanel panel) {

        for (Component c : panel.getComponents()) {

            if (c instanceof JLabel) {

                JLabel l = (JLabel) c;

                if ("▼".equals(l.getText()) || "▲".equals(l.getText())) return l;

            } else if (c instanceof JPanel) {

                JLabel found = findIndicatorLabel((JPanel) c);

                if (found != null) return found;

            }

        }

        return null;

    }

    private String[][] parseTable(String json, String[] fields) {

        if (json == null || json.trim().isEmpty()) {

            return new String[0][fields.length];

        }

        String trimmed = json.trim();

        if (trimmed.startsWith("[")) trimmed = trimmed.substring(1);

        if (trimmed.endsWith("]")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        String[] objects = trimmed.split("},\\s*\\{");

        String[][] data = new String[objects.length][fields.length];

        for (int i = 0; i < objects.length; i++) {

            String obj = objects[i];

            if (!obj.startsWith("{")) obj = "{" + obj;

            if (!obj.endsWith("}")) obj = obj + "}";

            for (int j = 0; j < fields.length; j++) {

                data[i][j] = extractJsonValue(obj, fields[j]);

            }

        }

        return data;

    }

    private String extractJsonValue(String json, String key) {

        String keyPattern = "\"" + key + "\"";

        int keyIndex = json.indexOf(keyPattern);

        if (keyIndex == -1) return "";

        int colonIndex = json.indexOf(':', keyIndex);

        if (colonIndex == -1) return "";

        int start = colonIndex + 1;

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start < json.length() && json.charAt(start) == '"') {

            start++;

            int end = json.indexOf('"', start);

            if (end == -1) end = json.length();

            return json.substring(start, end);

        } else {

            int end = json.indexOf(',', start);

            if (end == -1) end = json.indexOf('}', start);

            if (end == -1) end = json.length();

            return json.substring(start, end).trim();

        }

    }

}

