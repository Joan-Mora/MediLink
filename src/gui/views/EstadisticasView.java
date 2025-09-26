package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

public class EstadisticasView extends JPanel {

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color WARNING_ORANGE = new Color(255, 152, 0);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    public EstadisticasView() {

        super(new BorderLayout());

        setBackground(LIGHT_BLUE);

    }

    public void render(int pacientes, int medicos, int medicosActivos, int medicosInactivos, int diagnosticos, int usuarios) {

        // Verificar estado de conexión a la BD

        String estadoSistema = verificarEstadoSistema();

        removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(LIGHT_BLUE);

        JPanel headerPanel = createEstadisticasHeader();

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel(new GridLayout(2, 2, 20, 20));

        cardsContainer.setBackground(LIGHT_BLUE);

        cardsContainer.setBorder(new EmptyBorder(25, 25, 25, 25));

        cardsContainer.add(createStatCard(" Pacientes", String.valueOf(pacientes), "Total de pacientes registrados", PRIMARY_BLUE));

        cardsContainer.add(createStatCard(" Médicos", String.valueOf(medicos), " " + medicosActivos + " activos,  " + medicosInactivos + " inactivos", SUCCESS_GREEN));

        cardsContainer.add(createStatCard("Diagnósticos", String.valueOf(diagnosticos), "Consultas registradas", WARNING_ORANGE));

        cardsContainer.add(createStatCard("Usuarios", String.valueOf(usuarios), "Cuentas del sistema", DANGER_RED));

        JPanel resumenPanel = createResumenEstadisticas(pacientes, medicos, diagnosticos, usuarios, estadoSistema);

        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.setBackground(LIGHT_BLUE);

        centerPanel.add(cardsContainer, BorderLayout.NORTH);

        centerPanel.add(resumenPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(centerPanel);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        revalidate();

        repaint();

    }

    private JPanel createEstadisticasHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(CARD_WHITE);

        header.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 235, 240)),

            new EmptyBorder(20, 25, 20, 25)

        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftPanel.setOpaque(false);

        JLabel icon = new JLabel("■");

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel textPanel = new JPanel();

        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Estadísticas del Sistema");

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        titleLabel.setForeground(TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Métricas y resumen general del hospital");

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        subtitleLabel.setForeground(new Color(120, 120, 120));

        textPanel.add(titleLabel);

        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        textPanel.add(subtitleLabel);

        leftPanel.add(icon);

        leftPanel.add(textPanel);

        JLabel fechaLabel = new JLabel("" + java.time.LocalDateTime.now().toString().substring(0, 19).replace("T", " "));

        fechaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        fechaLabel.setForeground(new Color(120, 120, 120));

        header.add(leftPanel, BorderLayout.WEST);

        header.add(fechaLabel, BorderLayout.EAST);

        return header;

    }

    private JPanel createStatCard(String titulo, String valor, String descripcion, Color accentColor) {

        JPanel card = new JPanel(new BorderLayout());

        card.setBackground(CARD_WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

            new EmptyBorder(20, 20, 20, 20)

        ));

        JPanel colorStrip = new JPanel();

        colorStrip.setBackground(accentColor);

        colorStrip.setPreferredSize(new Dimension(0, 4));

        JPanel contentPanel = new JPanel();

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.setOpaque(false);

        JLabel tituloLabel = new JLabel(titulo);

        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tituloLabel.setForeground(new Color(80, 80, 80));

        tituloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valorLabel = new JLabel(valor);

        valorLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));

        valorLabel.setForeground(accentColor);

        valorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcionLabel = new JLabel(descripcion);

        descripcionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        descripcionLabel.setForeground(new Color(120, 120, 120));

        descripcionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        contentPanel.add(tituloLabel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        contentPanel.add(valorLabel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        contentPanel.add(descripcionLabel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(colorStrip, BorderLayout.NORTH);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;

    }

    private String verificarEstadoSistema() {

        try {

            String healthJson = gui.models.ApiClient.get("/health");

            if (healthJson != null && healthJson.contains("\"db\":\"connected\"")) {

                return "✅ Operativo - Base de datos conectada";

            } else if (healthJson != null && healthJson.contains("\"ok\":true")) {

                return "⚠️ Servidor activo - Base de datos desconectada";

            } else {

                return "❌ Sistema no disponible";

            }

        } catch (Exception ex) {

            return "❌ Error de conexión: " + ex.getMessage();

        }

    }

    private JPanel createResumenEstadisticas(int pacientes, int medicos, int diagnosticos, int usuarios, String estadoSistema) {

        JPanel mainCard = new JPanel(new BorderLayout());

        mainCard.setBackground(CARD_WHITE);

        mainCard.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),

            BorderFactory.createEmptyBorder()

        ));

        mainCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel compactPanel = new JPanel(new BorderLayout());

        compactPanel.setBackground(CARD_WHITE);

        compactPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        leftInfo.setOpaque(false);

        JLabel icon = new JLabel("■");

        icon.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        icon.setForeground(PRIMARY_BLUE);

        icon.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Resumen Detallado");

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        titleLabel.setForeground(TEXT_DARK);

        int total = pacientes + medicos + diagnosticos + usuarios;

        JLabel subtitleLabel = new JLabel("Total de registros: " + total + " | Última actualización: ahora");

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        subtitleLabel.setForeground(new Color(120, 120, 120));

        infoPanel.add(titleLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        infoPanel.add(subtitleLabel);

        leftInfo.add(icon);

        leftInfo.add(infoPanel);

        JLabel expandIndicator = new JLabel("▼");

        expandIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        expandIndicator.setForeground(new Color(150, 150, 150));

        compactPanel.add(leftInfo, BorderLayout.CENTER);

        compactPanel.add(expandIndicator, BorderLayout.EAST);

        JPanel expandedPanel = createResumenExpandido(pacientes, medicos, diagnosticos, usuarios, estadoSistema);

        expandedPanel.setVisible(false);

        compactPanel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override

            public void mouseClicked(java.awt.event.MouseEvent e) {

                boolean isExpanded = expandedPanel.isVisible();

                expandedPanel.setVisible(!isExpanded);

                expandIndicator.setText(isExpanded ? "▼" : "▲");

                mainCard.revalidate();

                mainCard.repaint();

            }

            @Override public void mouseEntered(java.awt.event.MouseEvent e) { compactPanel.setBackground(new Color(248, 251, 255)); }

            @Override public void mouseExited(java.awt.event.MouseEvent e) { compactPanel.setBackground(CARD_WHITE); }

        });

        mainCard.add(compactPanel, BorderLayout.NORTH);

        mainCard.add(expandedPanel, BorderLayout.CENTER);

        return mainCard;

    }

    private JPanel createResumenExpandido(int pacientes, int medicos, int diagnosticos, int usuarios, String estadoSistema) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(new Color(248, 251, 255));

        panel.setBorder(BorderFactory.createCompoundBorder(

            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)),

            new EmptyBorder(20, 25, 20, 25)

        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 5, 8, 15);

        gbc.anchor = GridBagConstraints.WEST;

        double promedioDiagnosticosPorMedico = medicos > 0 ? (double) diagnosticos / medicos : 0;

        double promedioDiagnosticosPorPaciente = pacientes > 0 ? (double) diagnosticos / pacientes : 0;

        String[] labels = {

            "Total de registros:",

            "■ Promedio diagnósticos/médico:",

            "■ Promedio diagnósticos/paciente:",

            "Ratio médicos/pacientes:",

            "■ Usuarios por médico:",

            "■ Estado del sistema:"

        };

        String[] values = {

            String.valueOf(pacientes + medicos + diagnosticos + usuarios),

            String.format("%.1f", promedioDiagnosticosPorMedico),

            String.format("%.1f", promedioDiagnosticosPorPaciente),

            String.format("1:%.1f", pacientes > 0 ? (double) pacientes / medicos : 0),

            String.format("%.1f", medicos > 0 ? (double) usuarios / medicos : 0),

            estadoSistema

        };

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

}

