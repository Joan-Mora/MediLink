package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.awt.event.KeyAdapter;

import java.awt.event.KeyEvent;

public class UsuariosView extends JPanel {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private JPanel usuariosCardsContainer;

    private String[][] allUsuariosData;

    public UsuariosView() {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

    }

    public void render(String json) {

        removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BLUE);

        JPanel headerPanel = createUsuariosHeader();

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel();

        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));

        cardsContainer.setBackground(LIGHT_BLUE);

        cardsContainer.setBorder(new EmptyBorder(15, 25, 25, 25));

        String[][] data = parseTable(json, new String[]{

                "id_usuario","medico_id_medico","nombre","contrasena"

        });

        allUsuariosData = data;

        usuariosCardsContainer = cardsContainer;

        for (String[] usuario : data) {

            JPanel card = createExpandableUsuarioCard(usuario);

            cardsContainer.add(card);

            cardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        }

        JScrollPane scrollPane = new JScrollPane(cardsContainer);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        revalidate();

        repaint();

    }

    private JPanel createUsuariosHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(CARD_WHITE);

        header.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 235, 240)),

                new EmptyBorder(20, 25, 20, 25)

        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftPanel.setOpaque(false);

        JLabel icon = new JLabel("👤");

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel textPanel = new JPanel();

        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Usuarios del Sistema");

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        titleLabel.setForeground(TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Cuentas asociadas a médicos");

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        subtitleLabel.setForeground(new Color(120, 120, 120));

        textPanel.add(titleLabel);

        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        textPanel.add(subtitleLabel);

        leftPanel.add(icon);

        leftPanel.add(textPanel);

        JTextField searchField = new JTextField(" Buscar por ID, nombre...");

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

                if (searchField.getText().equals(" Buscar por ID, nombre...")) {

                    searchField.setText("");

                    searchField.setForeground(TEXT_DARK);

                }

            }

            @Override

            public void focusLost(java.awt.event.FocusEvent e) {

                if (searchField.getText().isEmpty()) {

                    searchField.setText(" Buscar por ID, nombre...");

                    searchField.setForeground(new Color(150, 150, 150));

                }

            }

        });

        searchField.addKeyListener(new KeyAdapter() {

            @Override

            public void keyReleased(KeyEvent e) {

                String searchText = searchField.getText().toLowerCase();

                if (searchText.equals(" buscar por id, nombre...")) searchText = "";

                filterUsuarios(searchText);

            }

        });

        header.add(leftPanel, BorderLayout.WEST);

        header.add(searchField, BorderLayout.EAST);

        return header;

    }

    private void filterUsuarios(String searchText) {

        usuariosCardsContainer.removeAll();

        for (String[] usuario : allUsuariosData) {

            String combined = String.join(" ", usuario).toLowerCase();

            if (combined.contains(searchText)) {

                JPanel card = createExpandableUsuarioCard(usuario);

                usuariosCardsContainer.add(card);

                usuariosCardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));

            }

        }

        usuariosCardsContainer.revalidate();

        usuariosCardsContainer.repaint();

    }

    private JPanel createExpandableUsuarioCard(String[] datos) {

        JPanel mainCard = new JPanel(new BorderLayout());

        mainCard.setBackground(CARD_WHITE);

        mainCard.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

                BorderFactory.createEmptyBorder()

        ));

        mainCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel compactPanel = createCompactUsuarioView(datos);

        mainCard.add(compactPanel, BorderLayout.NORTH);

        JPanel expandedPanel = createExpandedUsuarioView(datos);

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

    private JPanel createCompactUsuarioView(String[] datos) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(CARD_WHITE);

        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftInfo.setOpaque(false);

        JLabel icon = new JLabel("👤");

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        icon.setForeground(new Color(67, 160, 71));

        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.setOpaque(false);

        JLabel usuarioLabel = new JLabel("Usuario ID: " + datos[0] + "  |  Médico ID: " + datos[1]);

        usuarioLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        usuarioLabel.setForeground(TEXT_DARK);

        String resumen = "● " + datos[2];

        JLabel resumenLabel = new JLabel(resumen);

        resumenLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        resumenLabel.setForeground(new Color(120, 120, 120));

        infoPanel.add(usuarioLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(resumenLabel);

        leftInfo.add(icon);

        leftInfo.add(infoPanel);

        JLabel expandIndicator = new JLabel("▼");

        expandIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        expandIndicator.setForeground(new Color(150, 150, 150));

        panel.add(leftInfo, BorderLayout.CENTER);

        panel.add(expandIndicator, BorderLayout.EAST);

        return panel;

    }

    private JPanel createExpandedUsuarioView(String[] datos) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(new Color(248, 251, 255));

        panel.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)),

                new EmptyBorder(20, 25, 20, 25)

        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 5, 8, 15);

        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"● ID Usuario:", "● ID Médico:", "● Usuario:"};

        String[] values = {datos[0], datos[1], datos[2]};

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

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;

        JLabel passLabel = new JLabel("● Contraseña:");

        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        passLabel.setForeground(new Color(80, 80, 80));

        panel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;

        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        passPanel.setOpaque(false);

    String plainPass = datos[3] == null ? "" : datos[3];

    String masked = maskPassword(plainPass);

        JLabel passValue = new JLabel(masked);

        passValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        passValue.setForeground(TEXT_DARK);

        passValue.setBorder(new EmptyBorder(0, 0, 0, 10));

        JButton toggleBtn = new JButton("Mostrar");

        toggleBtn.setFocusPainted(false);

        toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        toggleBtn.setBackground(new Color(240, 244, 248));

        toggleBtn.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 222)));

        final boolean[] showing = {false};

        toggleBtn.addActionListener(e -> {

            showing[0] = !showing[0];

            if (showing[0]) {

                passValue.setText(plainPass);

                toggleBtn.setText("Ocultar");

            } else {

                String m = maskPassword(plainPass);

                passValue.setText(m);

                toggleBtn.setText("Mostrar");

            }

        });

        passPanel.add(passValue);

        passPanel.add(toggleBtn);

        panel.add(passPanel, gbc);

        return panel;

    }

    private String maskPassword(String plain) {

        if (plain == null || plain.isEmpty()) return "";

        int n = Math.max(6, Math.min(plain.length(), 12));

        char[] arr = new char[n];

        java.util.Arrays.fill(arr, '•');

        return new String(arr);

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

