package gui.views;

import gui.models.ApiClient;

import java.awt.*;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    public LoginFrame() {

    super("MediLink " + app.AppInfo.VERSION + " - Iniciar sesión");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(900, 560);

        setLocationRelativeTo(null);

        // Icono de la app si está disponible, usando IcoUtil para múltiples tamaños

        try {

            java.util.List<java.awt.Image> icons = new java.util.ArrayList<>();

            Path iconIco = Paths.get("icon.ico");

            Path iconPng = Paths.get("icon.png");

            // También intentar en subcarpeta "app" (instalación jpackage)

            if (!Files.exists(iconIco)) iconIco = Paths.get("app", "icon.ico");

            if (!Files.exists(iconPng)) iconPng = Paths.get("app", "icon.png");

            if (Files.exists(iconIco)) {

                try { icons.addAll(gui.util.IcoUtil.readIcoAll(iconIco)); } catch (Throwable t) {}

                if (icons.isEmpty()) { try { icons.add(new ImageIcon(iconIco.toString()).getImage()); } catch (Throwable t) {} }

            }

            if (icons.isEmpty() && Files.exists(iconPng)) {

                try { icons.add(new ImageIcon(iconPng.toString()).getImage()); } catch (Throwable t) {}

            }

            if (!icons.isEmpty()) setIconImages(icons);

        } catch (Throwable ignored) {}

        // Fondo con gradiente

        JPanel gradient = new JPanel() {

            @Override protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                int w = getWidth(), h = getHeight();

                Color c1 = new Color(24, 44, 72);

                Color c2 = new Color(35, 63, 104);

                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);

                g2.setPaint(gp);

                g2.fillRect(0, 0, w, h);

                g2.dispose();

            }

        };

        gradient.setLayout(new GridBagLayout());

        gradient.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Tarjeta central

        JPanel card = new JPanel(new BorderLayout(0, 16));

        card.setPreferredSize(new Dimension(700, 400));

        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(new Color(33,150,243), 2),

                new EmptyBorder(28, 32, 28, 32)

        ));

        JLabel title = new JLabel("MediLink Administrativo", SwingConstants.CENTER);

        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        title.setForeground(new Color(33, 150, 243));

        // Logo centrado si existe

        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);

        try {

            Path logoPng = Paths.get("logo.png");

            if (!Files.exists(logoPng)) logoPng = Paths.get("app", "logo.png");

            if (Files.exists(logoPng)) {

                ImageIcon ic = new ImageIcon(logoPng.toString());

                Image img = ic.getImage();

                int width = img.getWidth(null);

                int height = img.getHeight(null);

                int maxWidth = 180;

                if (width > maxWidth) {

                    int newHeight = (height * maxWidth) / width;

                    java.awt.image.BufferedImage resized = new java.awt.image.BufferedImage(maxWidth, newHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = resized.createGraphics();

                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

                    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

                    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                    g2d.drawImage(img, 0, 0, maxWidth, newHeight, null);

                    g2d.dispose();

                    logoLabel.setIcon(new ImageIcon(resized));

                } else {

                    logoLabel.setIcon(ic);

                }

            } else {

                logoLabel.setText("🔷");

                logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));

            }

        } catch (Throwable ignored) { logoLabel.setText("🔷"); logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80)); }

        JPanel form = new JPanel(new GridBagLayout());

        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();

        gc.insets = new Insets(8, 8, 8, 8);

        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.weightx = 1;

        JTextField usuario = new JTextField();

        JPasswordField pass = new JPasswordField();

        JButton ingresar = new JButton("Ingresar");

        JButton salir = new JButton("Salir");

    JButton configurar = new JButton("Configurar BD");

        styleField(usuario);

        styleField(pass);

    styleButton(ingresar, new Color(33,150,243));

    styleButton(salir, new Color(158, 158, 158));

    // Botón para abrir la carpeta de instalación y guiar al usuario a ejecutar configurar-bd.bat

    styleButton(configurar, new Color(76, 175, 80)); // Verde (Material Green 500)

        int row = 0;

        addFormRow(form, gc, row++, "Usuario", usuario);

        addFormRow(form, gc, row++, "Contraseña", pass);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        actions.setOpaque(false);

    // Quitar configuración interna de BD; se usará configurar-bd.bat externo

        actions.add(configurar);

        actions.add(salir);

        actions.add(ingresar);

        JPanel center = new JPanel(new BorderLayout(0, 16));

        center.setOpaque(false);

        center.add(logoLabel, BorderLayout.NORTH);

        center.add(form, BorderLayout.CENTER);

        center.add(actions, BorderLayout.SOUTH);

        card.add(title, BorderLayout.NORTH);

        card.add(center, BorderLayout.CENTER);

        GridBagConstraints rootGc = new GridBagConstraints();

        rootGc.gridx = 0; rootGc.gridy = 0;

        rootGc.weightx = 1; rootGc.weighty = 1;

        rootGc.fill = GridBagConstraints.NONE;

        gradient.add(card, rootGc);

        setContentPane(gradient);

        getRootPane().setDefaultButton(ingresar);

        // Chequeo de salud al inicio (no bloqueante)

        new Thread(() -> {

            try { Thread.sleep(300); } catch (InterruptedException ignored) {}

            if (!gui.models.ApiClient.healthOk()) {

                SwingUtilities.invokeLater(() -> {

                    int choice = JOptionPane.showOptionDialog(

                            this,

                            "No se pudo conectar con el servicio o la base de datos.\n" +

                                    "Puedes continuar sin conexión para configurar y luego verificar.",

                            "MediLink - Conexión",

                            JOptionPane.DEFAULT_OPTION,

                            JOptionPane.WARNING_MESSAGE,

                            null,

                            new Object[]{"Continuar sin conexión", "Reintentar", "Salir"},

                            "Continuar sin conexión");

                    if (choice == 1) {

                        new Thread(() -> {

                            boolean ok = false;

                            for (int i = 0; i < 5; i++) {

                                try { Thread.sleep(800); } catch (InterruptedException ignored) {}

                                if (gui.models.ApiClient.healthOk()) { ok = true; break; }

                            }

                            if (!ok) {

                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,

                                        "Sigue sin conexión. Revisa credenciales o servicio.",

                                        "MediLink",

                                        JOptionPane.ERROR_MESSAGE));

                            }

                        }).start();

                    } else if (choice == 2) {

                        dispose();

                        System.exit(0);

                    }

                    // choice 0: continuar sin conexión (no hacemos nada)

                });

            }

        }, "health-check").start();

        salir.addActionListener(e -> System.exit(0));

        ingresar.addActionListener(e -> {

            ingresar.setEnabled(false);

            salir.setEnabled(false);

            String u = usuario.getText().trim();

            String p = new String(pass.getPassword());

            new Thread(() -> {

                try {

                    doLogin(u, p);

                } finally {

                    SwingUtilities.invokeLater(() -> { ingresar.setEnabled(true); salir.setEnabled(true); });

                }

            }, "login-request").start();

        });

        configurar.addActionListener(e -> abrirCarpetaConfigYMostrarInfo());

    }

    private void doLogin(String u, String p) {

        if (u.isEmpty() || p.isEmpty()) {

            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Completa usuario y contraseña", "Campos requeridos", JOptionPane.WARNING_MESSAGE));

            return;

        }

        // Bypass Admin offline sin tocar servidor

        String uClean = u.replaceAll("[\"' ]", "").trim();

        String pClean = p.replaceAll("[\"' ]", "").trim();

        if ("Admin".equalsIgnoreCase(uClean) && "Admin2025".equals(pClean)) {

            SwingUtilities.invokeLater(() -> {

                new gui.MediLinkGUI("Admin").setVisible(true);

                dispose();

            });

            return;

        }

        try {

            java.util.HashMap<String, String> params = new java.util.HashMap<>();

            params.put("nombre", u);

            params.put("contrasena", p);

            String body = ApiClient.buildJson(params);

            String resp = ApiClient.post("/auth/login", body);

            if (resp.contains("\"success\":true")) {

                final String displayName = extractDisplayName(resp);

                SwingUtilities.invokeLater(() -> {

                    new gui.MediLinkGUI(displayName).setVisible(true);

                    dispose();

                });

            } else {

                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Credenciales inválidas", "Acceso denegado", JOptionPane.ERROR_MESSAGE));

            }

        } catch (Exception ex) {

            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));

        }

    }

    private String extractDisplayName(String json) {

        try {

            // Si es admin

            if (json.contains("\"role\":\"ADMIN\"")) return "Admin";

            // Extraer "medico":{"nombres":"...","apellidos":"..."}

            int mIdx = json.indexOf("\"medico\"");

            if (mIdx >= 0) {

                int nIdx = json.indexOf("\"nombres\":\"", mIdx);

                int aIdx = json.indexOf("\"apellidos\":\"", mIdx);

                if (nIdx > 0) {

                    int nStart = nIdx + "\"nombres\":\"".length();

                    int nEnd = json.indexOf("\"", nStart);

                    String nombres = nEnd > nStart ? json.substring(nStart, nEnd) : "";

                    if (aIdx > 0) {

                        int aStart = aIdx + "\"apellidos\":\"".length();

                        int aEnd = json.indexOf("\"", aStart);

                        String apellidos = aEnd > aStart ? json.substring(aStart, aEnd) : "";

                        String full = (nombres + " " + apellidos).trim();

                        if (!full.isEmpty()) return full;

                    }

                    if (!nombres.isEmpty()) return nombres;

                }

            }

        } catch (Throwable ignore) {}

        // Fallback al nombre de usuario si estuviera

        try {

            int uIdx = json.indexOf("\"nombre\":\"");

            if (uIdx >= 0) {

                int uStart = uIdx + "\"nombre\":\"".length();

                int uEnd = json.indexOf("\"", uStart);

                if (uEnd > uStart) return json.substring(uStart, uEnd);

            }

        } catch (Throwable ignore) {}

        return "Usuario";

    }

    private void styleField(JComponent c) {

        c.setPreferredSize(new Dimension(320, 36));

        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));

    }

    private void styleButton(JButton b, Color color) {

        b.setBackground(color);

        b.setForeground(Color.WHITE);

        b.setFocusPainted(false);

        b.setFont(new Font("Segoe UI", Font.BOLD, 14));

        b.setBorder(new EmptyBorder(8, 16, 8, 16));

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; form.add(new JLabel(label), gc);

        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; form.add(field, gc);

    }

    // Abre la carpeta donde está MediLink.exe (o donde exista configurar-bd.bat) y muestra instrucciones

    private void abrirCarpetaConfigYMostrarInfo() {

        Path carpeta = resolverCarpetaInstalacion();

        Path script = carpeta.resolve("configurar-bd.bat");

        boolean scriptExiste = Files.exists(script);

        // Abrir carpeta de forma confiable en Windows (Explorer); fallback a Desktop.open en otros SO

        try {

            Path target = Files.isDirectory(carpeta) ? carpeta : carpeta.getParent();

            if (target == null) target = carpeta;

            String os = System.getProperty("os.name", "").toLowerCase();

            if (os.contains("win")) {

                if (scriptExiste) {

                    // Abrir Explorer seleccionando el archivo configurar-bd.bat

                    new ProcessBuilder("explorer.exe", "/select,", script.toAbsolutePath().toString()).start();

                } else {

                    // Abrir solo la carpeta

                    new ProcessBuilder("explorer.exe", target.toAbsolutePath().toString()).start();

                }

            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {

                Desktop.getDesktop().open(target.toFile());

            }

        } catch (Exception ignored) {}

        StringBuilder msg = new StringBuilder();

        msg.append("Se abrió la carpeta de MediLink.\n\n");

        if (scriptExiste) {

            msg.append("1) Ejecuta 'configurar-bd.bat' para asignar la base de datos.\n");

        } else {

            msg.append("1) Localiza y ejecuta 'configurar-bd.bat' para asignar la base de datos.\n");

        }

        msg.append("2) Cierra y vuelve a abrir MediLink para aplicar los cambios.\n\n");

        if (scriptExiste) {

            msg.append("Ruta del configurador:\n").append(script.toAbsolutePath());

        } else {

            msg.append("No se encontró 'configurar-bd.bat' en: \n").append(carpeta.toAbsolutePath());

        }

        JOptionPane.showMessageDialog(this, msg.toString(), "Configurar Base de Datos", JOptionPane.INFORMATION_MESSAGE);

    }

    // Intenta deducir la carpeta de instalación (raíz con MediLink.exe y configurar-bd.bat)

    private Path resolverCarpetaInstalacion() {

        try {

            // 1) Directorio de trabajo actual

            Path cwd = Paths.get("").toAbsolutePath().normalize();

            if (Files.exists(cwd.resolve("configurar-bd.bat"))) return cwd;

            // 2) Deducción por ubicación del código/jar (en app-image suele ser .../app/MediLink.jar)

            Path codeBase = null;

            try {

                codeBase = Paths.get(LoginFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            } catch (Exception ignored) {}

            if (codeBase != null) {

                Path base = codeBase;

                if (Files.isRegularFile(base)) base = base.getParent(); // si es un JAR, ir al directorio

                if (base != null) {

                    if (base.getFileName() != null && "app".equalsIgnoreCase(base.getFileName().toString())) {

                        Path root = base.getParent();

                        if (root != null) {

                            if (Files.exists(root.resolve("configurar-bd.bat"))) return root;

                            return root; // aunque no esté el script, abrir raíz de la app

                        }

                    } else {

                        // base directo

                        if (Files.exists(base.resolve("configurar-bd.bat"))) return base;

                        Path parent = base.getParent();

                        if (parent != null && Files.exists(parent.resolve("configurar-bd.bat"))) return parent;

                    }

                }

            }

            // 3) Como fallback, intentar abrir cwd aunque no tenga el script

            return cwd;

        } catch (Throwable t) {

            return Paths.get("").toAbsolutePath().normalize();

        }

    }

}

