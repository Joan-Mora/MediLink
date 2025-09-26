package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.util.function.Consumer;

public class DashboardView extends JPanel {

    // Copiamos los colores para mantener el mismo diseño visual

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color WARNING_ORANGE = new Color(255, 152, 0);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private final Consumer<String> onNavigate;

    private JLabel totalPacientesLabel;

    private JLabel totalMedicosLabel;

    private JLabel totalDiagnosticosLabel;

    private JLabel totalUsuariosLabel;

    public DashboardView(Consumer<String> onNavigate) {

        super(new BorderLayout());

        this.onNavigate = onNavigate;

        setOpaque(false);

        setBorder(new EmptyBorder(20, 0, 0, 0));

        buildUI();

    }

    public void setCounts(int pacientes, int medicos, int diagnosticos, int usuarios) {

        if (totalPacientesLabel != null) totalPacientesLabel.setText(String.valueOf(pacientes));

        if (totalMedicosLabel != null) totalMedicosLabel.setText(String.valueOf(medicos));

        if (totalDiagnosticosLabel != null) totalDiagnosticosLabel.setText(String.valueOf(diagnosticos));

        if (totalUsuariosLabel != null) totalUsuariosLabel.setText(String.valueOf(usuarios));

    }

    private void buildUI() {

        JPanel statsPanel = createStatsPanel();

        add(statsPanel, BorderLayout.NORTH);

        JPanel quickActionsPanel = createQuickActionsPanel();

        add(quickActionsPanel, BorderLayout.CENTER);

    }

    private JPanel createStatsPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));

        panel.setBackground(LIGHT_BLUE);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel pacientesCard = createStatCard("Pacientes", "0", PRIMARY_BLUE);

        JPanel medicosCard = createStatCard("Médicos", "0", SUCCESS_GREEN);

        JPanel diagnosticosCard = createStatCard("Diagnósticos", "0", WARNING_ORANGE);

        JPanel usuariosCard = createStatCard("Usuarios", "0", DANGER_RED);

        totalPacientesLabel = (JLabel) pacientesCard.getComponent(2);

        totalMedicosLabel = (JLabel) medicosCard.getComponent(2);

        totalDiagnosticosLabel = (JLabel) diagnosticosCard.getComponent(2);

        totalUsuariosLabel = (JLabel) usuariosCard.getComponent(2);

        panel.add(pacientesCard);

        panel.add(medicosCard);

        panel.add(diagnosticosCard);

        panel.add(usuariosCard);

        return panel;

    }

    private JPanel createStatCard(String title, String value, Color color) {

        JPanel card = new JPanel() {

            @Override

            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 20));

                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);

                GradientPaint gradient = new GradientPaint(0, 0, CARD_WHITE, 0, getHeight(), new Color(250, 252, 255));

                g2d.setPaint(gradient);

                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);

                g2d.setColor(color);

                g2d.fillRoundRect(0, 0, 4, getHeight() - 2, 10, 10);

                g2d.dispose();

            }

        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setOpaque(false);

        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        titleLabel.setForeground(new Color(100, 100, 100));

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));

        valueLabel.setForeground(color);

        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(valueLabel);

        return card;

    }

    private JPanel createQuickActionsPanel() {

        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));

        panel.setBackground(LIGHT_BLUE);

        panel.setBorder(new EmptyBorder(30, 0, 0, 0));

        panel.add(createQuickActionCard("👥 Ver Pacientes", "Gestionar información de pacientes", "pacientes"));

        panel.add(createQuickActionCard("🩺 Ver Médicos", "Administrar médicos del hospital", "medicos"));

        panel.add(createQuickActionCard("📋 Diagnósticos", "Revisar diagnósticos médicos", "diagnosticos"));

        panel.add(createQuickActionCard("➕ Nuevo Paciente", "Registrar nuevo paciente", "add_paciente"));

        panel.add(createQuickActionCard("➕ Nuevo Médico", "Agregar médico al sistema", "add_medico"));

        panel.add(createQuickActionCard("📊 Estadísticas", "Ver estadísticas del sistema", "stats"));

        return panel;

    }

    private JPanel createQuickActionCard(String title, String description, String action) {

        JPanel card = new JPanel() {

            @Override

            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 15));

                g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 20, 20);

                GradientPaint gradient = new GradientPaint(0, 0, CARD_WHITE, 0, getHeight(), new Color(252, 252, 254));

                g2d.setPaint(gradient);

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2d.setColor(new Color(225, 225, 235));

                g2d.setStroke(new BasicStroke(1.5f));

                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2d.dispose();

            }

        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setOpaque(false);

        card.setBorder(new EmptyBorder(25, 20, 25, 20));

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.setPreferredSize(new Dimension(200, 140));

        String[] parts = title.split(" ", 2);

        String icon = parts[0];

        String text = parts.length > 1 ? parts[1] : "";

        JLabel iconLabel = new JLabel(icon);

        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(text);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        titleLabel.setForeground(TEXT_DARK);

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><div style='text-align: center; width: 160px;'>" + description + "</div></html>");

        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        descLabel.setForeground(new Color(100, 100, 120));

        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(iconLabel);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        card.add(titleLabel);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        card.add(descLabel);

        card.addMouseListener(new MouseAdapter() {

            @Override

            public void mouseEntered(MouseEvent e) {

                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

                titleLabel.setForeground(PRIMARY_BLUE);

                card.repaint();

            }

            @Override

            public void mouseExited(MouseEvent e) {

                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

                titleLabel.setForeground(TEXT_DARK);

                card.repaint();

            }

            @Override

            public void mouseClicked(MouseEvent e) {

                if (onNavigate != null) onNavigate.accept(action);

            }

        });

        return card;

    }

}

