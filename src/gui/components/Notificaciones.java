package gui.components;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.awt.datatransfer.StringSelection;

public class Notificaciones {

    // Colores base y estilo

    private static final Color CANVAS_BG = new Color(245, 250, 255);

    private static final Color BORDER_SOFT = new Color(220, 225, 230);

    private static final Color TEXT_DARK = new Color(33, 33, 33);

    private static final Color TEXT_MUTED = new Color(100, 110, 125);

    private static final Color SUCCESS = new Color(67, 160, 71);

    private static final Color WARNING = new Color(255, 152, 0);

    private static final Color ERROR = new Color(229, 57, 53);

    // API pública

    public static void exitoRegistro(Component parent, String tipo, String nombre, String apellidos, String detalles, String credenciales) {

    JPanel card = buildCard("✔ Registro exitoso", "Se completó el registro en el sistema.", SUCCESS, "✔");

        JPanel body = new JPanel();

        body.setOpaque(false);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(row("Tipo", tipo));

        body.add(row("Nombre", nombre + " " + apellidos));

        if (detalles != null && !detalles.trim().isEmpty()) {

            body.add(spacer(8));

            body.add(section("Detalles", detalles.replace('|', '\n')));

        }

        if (credenciales != null && !credenciales.trim().isEmpty()) {

            body.add(spacer(8));

            body.add(section("Credenciales", credenciales));

        }

        addBody(card, body);

        if (credenciales != null && !credenciales.trim().isEmpty()) {

            Object footerObj = card.getClientProperty("footerPanel");

            if (footerObj instanceof JPanel) {

                JButton copiar = outlineButton("Copiar credenciales", SUCCESS);

                copiar.addActionListener(e -> {

                    copyToClipboard(credenciales);

                    showToast(parent, "Credenciales copiadas al portapapeles");

                });

                ((JPanel) footerObj).add(copiar, 0);

            }

        }

        showCustomDialog(parent, card, "Confirmación");

    }

    public static void operacionEstado(Component parent, boolean exito, String titulo, String mensaje) {

        Color base = exito ? SUCCESS : WARNING;

        String icon = exito ? "✔" : "⚠";

        JPanel card = buildCard(titulo, exito ? "La operación se realizó correctamente." : "La operación finalizó con avisos.", base, icon);

        JTextArea area = multiline(mensaje);

        addBody(card, area);

        showCustomDialog(parent, card, "Resultado");

    }

    public static void error(Component parent, String titulo, String mensaje) {

        JPanel card = buildCard(titulo, "Se produjo un error durante la operación.", ERROR, "✖");

        JTextArea area = multiline(mensaje);

        addBody(card, area);

        showCustomDialog(parent, card, "Error");

    }

    // Construcción visual

    private static JPanel buildCard(String title, String subtitle, Color base, String iconText) {

    JPanel container = new JPanel(new BorderLayout());

        container.setOpaque(false);

        container.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel card = new RoundedCard(14, base);

        card.setLayout(new BorderLayout());

        card.setOpaque(false);

        // Header con gradiente e icono

    JPanel header = new GradientHeader(base);

        header.setLayout(new BorderLayout());

        header.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        left.setOpaque(false);

        BadgeIcon badge = new BadgeIcon(iconText, base);

        JLabel titleLabel = new JLabel("  " + title);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        titleLabel.setForeground(Color.WHITE);

        left.add(badge);

        left.add(titleLabel);

    JLabel subLabel = new JLabel(subtitle);

    subLabel.setFont(subLabel.getFont().deriveFont(Font.PLAIN, 12f));

    subLabel.setForeground(new Color(255, 255, 255, 220));

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    right.setOpaque(false);

    right.add(subLabel);

    JLabel logo = loadLogoLabel(18);

    if (logo != null) right.add(logo);

        header.add(left, BorderLayout.WEST);

        header.add(right, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();

        body.setOpaque(false);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.setBorder(new EmptyBorder(16, 18, 8, 18));

        card.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        footer.setOpaque(false);

        footer.setBorder(new EmptyBorder(0, 18, 16, 18));

        JButton ok = primaryButton("Aceptar", base);

        footer.add(ok);

        card.add(footer, BorderLayout.SOUTH);

        container.add(card, BorderLayout.CENTER);

        // Guardar botón en client property para poder cerrarlo en showCustomDialog

        container.putClientProperty("okButton", ok);

        container.putClientProperty("footerPanel", footer);

        return container;

    }

    private static void addBody(JPanel cardContainer, JComponent bodyContent) {

        // cardContainer es el container externo; el card real es su CENTER

        JPanel card = (JPanel) ((BorderLayout) cardContainer.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel body = (JPanel) ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        body.add(bodyContent);

    }

    private static JPanel row(String label, String value) {

        JPanel row = new JPanel(new BorderLayout());

        row.setOpaque(false);

        JLabel l = new JLabel(label + ":");

        l.setFont(l.getFont().deriveFont(Font.BOLD));

        l.setForeground(TEXT_MUTED);

        JLabel v = new JLabel(value);

        v.setForeground(TEXT_DARK);

        row.add(l, BorderLayout.WEST);

        row.add(v, BorderLayout.CENTER);

        return row;

    }

    private static JPanel section(String title, String text) {

        JPanel wrap = new JPanel(new BorderLayout());

        wrap.setOpaque(false);

        JLabel t = new JLabel(title);

        t.setFont(t.getFont().deriveFont(Font.BOLD));

        t.setForeground(TEXT_DARK);

        JTextArea area = multiline(text);

        area.setBorder(new EmptyBorder(8, 10, 8, 10));

        area.setBackground(new Color(248, 250, 252));

        area.setForeground(TEXT_DARK);

        wrap.add(t, BorderLayout.NORTH);

        wrap.add(area, BorderLayout.CENTER);

        return wrap;

    }

    private static JTextArea multiline(String text) {

        JTextArea area = new JTextArea(text);

        area.setEditable(false);

        area.setLineWrap(true);

        area.setWrapStyleWord(true);

        area.setOpaque(true);

        area.setBackground(new Color(248, 250, 252));

        area.setForeground(TEXT_DARK);

        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        return area;

    }

    private static JButton primaryButton(String text, Color base) {

        JButton btn = new JButton(text);

        btn.setForeground(Color.WHITE);

        btn.setBackground(base);

        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;

    }

    private static JButton outlineButton(String text, Color base) {

        JButton btn = new JButton(text);

        btn.setForeground(base.darker());

        btn.setBackground(new Color(255, 255, 255));

        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(new Color(220, 225, 230)),

                new EmptyBorder(9, 15, 9, 15)

        ));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;

    }

    private static Component spacer(int h) { return Box.createRigidArea(new Dimension(0, h)); }

    private static void copyToClipboard(String text) {

        try {

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);

        } catch (Exception ignored) {}

    }

    private static JLabel loadLogoLabel(int height) {

        try {

            ImageIcon icon = new ImageIcon("logo.png");

            if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) return null;

            double rawScale = (double) height / icon.getIconHeight();

            double scale = Math.min(1.0, rawScale);

            int w = (int) Math.round(icon.getIconWidth() * scale);

            if (w <= 0) return null;

            Image scaled = icon.getImage().getScaledInstance(w, (int)Math.round(icon.getIconHeight()*scale), Image.SCALE_SMOOTH);

            JLabel label = new JLabel(new ImageIcon(scaled));

            return label;

        } catch (Exception e) {

            return null;

        }

    }

    // Diálogo personalizado

    private static void showCustomDialog(Component parent, JPanel cardContainer, String title) {

        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;

        final JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);

        try {

            if (owner instanceof Frame) {

                java.util.List<Image> imgs = ((Frame) owner).getIconImages();

                if (imgs != null && !imgs.isEmpty()) {

                    dialog.setIconImages(imgs);

                }

            }

        } catch (Exception ignored) {}

        dialog.getContentPane().setBackground(CANVAS_BG);

        dialog.setLayout(new BorderLayout());

        dialog.add(cardContainer, BorderLayout.CENTER);

        dialog.pack();

        // Ajustar tamaño mínimo para una card cómoda

        int minW = 520; int minH = Math.max(260, cardContainer.getPreferredSize().height);

        dialog.setSize(new Dimension(Math.max(minW, cardContainer.getPreferredSize().width + 28), minH + 28));

        dialog.setResizable(false);

        dialog.setLocationRelativeTo(owner);

        Object okObj = cardContainer.getClientProperty("okButton");

        if (okObj instanceof JButton) {

            ((JButton) okObj).addActionListener(e -> dialog.dispose());

        }

        dialog.setVisible(true);

    }

    private static void showToast(Component parent, String message) {

        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;

        final JWindow toast = new JWindow(owner);

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(new EmptyBorder(8, 14, 8, 14));

        panel.setBackground(new Color(50, 50, 50, 230));

        JLabel label = new JLabel(message);

        label.setForeground(Color.WHITE);

        panel.add(label, BorderLayout.CENTER);

        toast.add(panel);

        toast.pack();

        Point loc;

        if (owner != null) {

            int x = owner.getX() + (owner.getWidth() - toast.getWidth()) / 2;

            int y = owner.getY() + owner.getHeight() - toast.getHeight() - 60;

            loc = new Point(x, y);

        } else {

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

            loc = new Point((screen.width - toast.getWidth()) / 2, screen.height - toast.getHeight() - 60);

        }

        toast.setLocation(loc);

        // Mostrar y ocultar automáticamente

        Timer t = new Timer(1500, e -> toast.dispose());

        t.setRepeats(false);

        toast.setVisible(true);

        t.start();

    }

    // Componentes de soporte visual

    private static class RoundedCard extends JPanel {

        private final int arc;

    RoundedCard(int arc, Color base) { this.arc = arc; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Pseudo sombra

            for (int i = 6; i >= 1; i--) {

                g2.setColor(new Color(0, 0, 0, 10 - i));

                g2.fillRoundRect(i, i, getWidth() - 2 * i, getHeight() - 2 * i, arc + 10, arc + 10);

            }

            // Fondo

            GradientPaint paint = new GradientPaint(0, 0, new Color(255,255,255), 0, getHeight(), new Color(252, 253, 255));

            g2.setPaint(paint);

            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

            // Borde tenue

            g2.setColor(BORDER_SOFT);

            g2.setStroke(new BasicStroke(1.2f));

            g2.drawRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

            g2.dispose();

            super.paintComponent(g);

        }

    }

    private static class GradientHeader extends JPanel {

        private final Color base;

        GradientHeader(Color base) { this.base = base; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c1 = base;

            Color c2 = new Color(Math.min(255, base.getRed() + 30), Math.min(255, base.getGreen() + 30), Math.min(255, base.getBlue() + 30));

            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);

            g2.setPaint(gp);

            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();

            super.paintComponent(g);

        }

    }

    private static class BadgeIcon extends JComponent {

        private final String text;

        private final Color base;

        BadgeIcon(String text, Color base) { this.text = text; this.base = base; setPreferredSize(new Dimension(28, 28)); }

        @Override protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(255,255,255,230));

            g2.fillOval(0, 0, getWidth(), getHeight());

            g2.setColor(base.darker());

            g2.setStroke(new BasicStroke(1.2f));

            g2.drawOval(0, 0, getWidth()-1, getHeight()-1);

            // Texto centrado

            Font f = getFont().deriveFont(Font.BOLD, 14f);

            g2.setFont(f);

            FontMetrics fm = g2.getFontMetrics();

            int tx = (getWidth() - fm.stringWidth(text)) / 2;

            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(text, tx, ty);

            g2.dispose();

        }

    }

}

