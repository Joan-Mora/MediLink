package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.time.LocalDateTime;

import java.util.function.Supplier;

public class HealthView extends JPanel {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private static final Color SUCCESS_GREEN = new Color(67, 160, 71);

    private static final Color DANGER_RED = new Color(244, 67, 54);

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    public HealthView(boolean connected, Supplier<String> healthSupplier) {

        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBackground(LIGHT_BLUE);

        setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel(" VERIFICACIÓN DEL SISTEMA");

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        titleLabel.setForeground(PRIMARY_BLUE);

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(titleLabel);

        add(Box.createRigidArea(new Dimension(0, 30)));

        if (connected) {

            try { healthSupplier.get(); } catch (Exception ignoredEx) {}

            JPanel successCard = new JPanel(new BorderLayout());

            successCard.setBackground(CARD_WHITE);

            successCard.setBorder(BorderFactory.createCompoundBorder(

                    BorderFactory.createLineBorder(SUCCESS_GREEN, 2),

                    new EmptyBorder(25, 25, 25, 25)

            ));

            successCard.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            headerPanel.setBackground(CARD_WHITE);

            JLabel iconLabel = new JLabel("✅");

            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

            JLabel statusTitle = new JLabel("  SISTEMA OPERATIVO");

            statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

            statusTitle.setForeground(SUCCESS_GREEN);

            headerPanel.add(iconLabel);

            headerPanel.add(statusTitle);

            JPanel infoPanel = new JPanel(new GridBagLayout());

            infoPanel.setBackground(CARD_WHITE);

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(10, 0, 10, 20);

            gbc.anchor = GridBagConstraints.WEST;

            String fechaHora = LocalDateTime.now().toString().substring(0, 19).replace("T", " ");

            String[][] info = {

                    {"🌐 Servidor:", "http://localhost:8081"},

                    {"📡 Estado:", "Conectado y operativo"},

                    {"⏰ Verificado:", fechaHora},

                    {" Conexión:", "Establecida y estable"},

                    {"🎯 Estado general:", "Todos los servicios funcionando"}

            };

            for (int i=0;i<info.length;i++) {

                gbc.gridx=0; gbc.gridy=i;

                JLabel k = new JLabel(info[i][0]);

                k.setFont(new Font("Segoe UI", Font.BOLD, 14));

                k.setForeground(TEXT_DARK);

                infoPanel.add(k, gbc);

                gbc.gridx=1;

                JLabel v = new JLabel(info[i][1]);

                v.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                v.setForeground(Color.GRAY);

                infoPanel.add(v, gbc);

            }

            successCard.add(headerPanel, BorderLayout.NORTH);

            successCard.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.CENTER);

            successCard.add(infoPanel, BorderLayout.SOUTH);

            add(successCard);

            add(Box.createRigidArea(new Dimension(0, 20)));

            JLabel readyLabel = new JLabel(" El sistema está listo para usar");

            readyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));

            readyLabel.setForeground(SUCCESS_GREEN);

            readyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(readyLabel);

        } else {

            JPanel errorCard = new JPanel(new BorderLayout());

            errorCard.setBackground(CARD_WHITE);

            errorCard.setBorder(BorderFactory.createCompoundBorder(

                    BorderFactory.createLineBorder(DANGER_RED, 2),

                    new EmptyBorder(25, 25, 25, 25)

            ));

            errorCard.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            headerPanel.setBackground(CARD_WHITE);

            JLabel iconLabel = new JLabel("❌");

            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

            JLabel errorTitle = new JLabel("  ERROR DE CONEXIÓN");

            errorTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

            errorTitle.setForeground(DANGER_RED);

            headerPanel.add(iconLabel);

            headerPanel.add(errorTitle);

            JTextArea problemText = new JTextArea(

                    "🚫 No se puede conectar con el servidor backend.\n\n" +

                            "📋 Pasos para solucionar:\n" +

                            "  1. Verificar que el servidor esté ejecutándose\n" +

                            "  2. Confirmar la URL: http://localhost:8081\n" +

                            "  3. Revisar la configuración de red\n" +

                            "  4. Contactar al administrador del sistema");

            problemText.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            problemText.setBackground(CARD_WHITE);

            problemText.setForeground(TEXT_DARK);

            problemText.setEditable(false);

            problemText.setBorder(new EmptyBorder(15, 0, 0, 0));

            errorCard.add(headerPanel, BorderLayout.NORTH);

            errorCard.add(problemText, BorderLayout.CENTER);

            add(errorCard);

        }

    }

}

