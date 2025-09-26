package gui.views;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

public class Creditos extends JPanel {

    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private static final Color BORDER_GRAY = new Color(230, 235, 240);

    public Creditos() {

        setLayout(new BorderLayout());

        setBackground(LIGHT_BLUE);

        JPanel container = new JPanel(new GridBagLayout());

        container.setOpaque(false);

        JPanel card = new JPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBackground(CARD_WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(BORDER_GRAY, 1),

                new EmptyBorder(30, 40, 30, 40)

        ));

        JLabel title = new JLabel("Créditos", SwingConstants.CENTER);

        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Equipo de desarrollo de MediLink", SwingConstants.CENTER);

        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        subtitle.setForeground(new Color(120, 120, 120));

        // Obtener ruta del icono de WhatsApp (resources), con fallback a app/resources para instalaciones jpackage

        String basePath = System.getProperty("user.dir");

        String waPath = basePath + java.io.File.separator + "resources" + java.io.File.separator + "whatsapp.png";

        if (!new java.io.File(waPath).exists()) {

            waPath = basePath + java.io.File.separator + "app" + java.io.File.separator + "resources" + java.io.File.separator + "whatsapp.png";

        }

        ImageIcon waIconRaw = new ImageIcon(waPath);

        Image waImg = waIconRaw.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);

        ImageIcon waIcon = new ImageIcon(waImg);

        JPanel name1Panel = new JPanel();

        name1Panel.setOpaque(false);

        name1Panel.setLayout(new BoxLayout(name1Panel, BoxLayout.X_AXIS));

        name1Panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel wa1 = new JLabel();

        wa1.setIcon(waIcon);

        wa1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        wa1.setToolTipText("Contactar por WhatsApp");

        wa1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent e) {

                try {

                    String msg = java.net.URLEncoder.encode("Hola, me estoy comunicando con ustedes a través de la aplicación MediLink.\nEstoy interesado(a) en conocer más sobre sus servicios y cómo pueden ayudarme.\n\nQuedo atento(a) a su respuesta.\n¡Muchas gracias!", "UTF-8");

                    String url = "https://wa.me/573219105446?text=" + msg;

                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(null, "No se pudo abrir WhatsApp: " + ex.getMessage());

                }

            }

        });

        JLabel name1 = new JLabel("Darwin Joan Aveiga Mora", SwingConstants.CENTER);

        name1.setFont(new Font("Segoe UI", Font.BOLD, 18));

        name1.setForeground(PRIMARY_BLUE);

        name1Panel.add(wa1);

        name1Panel.add(Box.createRigidArea(new Dimension(8, 0)));

        name1Panel.add(name1);

        JPanel name2Panel = new JPanel();

        name2Panel.setOpaque(false);

        name2Panel.setLayout(new BoxLayout(name2Panel, BoxLayout.X_AXIS));

        name2Panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel wa2 = new JLabel();

        wa2.setIcon(waIcon);

        wa2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        wa2.setToolTipText("Contactar por WhatsApp");

        wa2.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent e) {

                try {

                    String msg = java.net.URLEncoder.encode("Hola, me estoy comunicando con ustedes a través de la aplicación MediLink.\nEstoy interesado(a) en conocer más sobre sus servicios y cómo pueden ayudarme.\n\nQuedo atento(a) a su respuesta.\n¡Muchas gracias!", "UTF-8");

                    String url = "https://wa.me/573508592891?text=" + msg;

                    java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(null, "No se pudo abrir WhatsApp: " + ex.getMessage());

                }

            }

        });

        JLabel name2 = new JLabel("Julian Esteban Chavez Zamora", SwingConstants.CENTER);

        name2.setFont(new Font("Segoe UI", Font.BOLD, 18));

        name2.setForeground(PRIMARY_BLUE);

        name2Panel.add(wa2);

        name2Panel.add(Box.createRigidArea(new Dimension(8, 0)));

        name2Panel.add(name2);

        JLabel thanks = new JLabel("Gracias por usar MediLink. Este software fue creado con dedicación para optimizar la gestión hospitalaria.", SwingConstants.CENTER);

        thanks.setAlignmentX(Component.CENTER_ALIGNMENT);

        thanks.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        thanks.setForeground(new Color(100, 100, 100));

        card.add(title);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        card.add(subtitle);

        card.add(Box.createRigidArea(new Dimension(0, 20)));

        card.add(name1Panel);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        card.add(name2Panel);

        card.add(Box.createRigidArea(new Dimension(0, 20)));

        card.add(thanks);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;

        container.add(card, gbc);

        add(wrapCenter(container), BorderLayout.CENTER);

    }

    private JComponent wrapCenter(JComponent c) {

        JPanel p = new JPanel(new GridBagLayout());

        p.setOpaque(false);

        p.add(c, new GridBagConstraints());

        return p;

    }

}

