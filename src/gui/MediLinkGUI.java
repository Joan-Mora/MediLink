package gui;

import gui.models.ApiClient;

import gui.views.*;

import gui.components.Notificaciones;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.util.List;

import java.util.Map;

import java.util.HashMap;

public class MediLinkGUI extends JFrame {

    private static final Color LIGHT_BLUE = new Color(245, 250, 255);

    private static final Color CARD_WHITE = new Color(255, 255, 255);

    // Sidebar oscuro (estilo original)

    private static final Color SIDEBAR_BG = new Color(35, 44, 58);

    private static final Color SIDEBAR_BTN_BG = new Color(52, 63, 80);

    private static final Color SIDEBAR_BTN_HOVER = new Color(66, 79, 99);

    private static final Color SIDEBAR_BTN_ACTIVE = new Color(78, 95, 120);

    private static final Color SIDEBAR_TEXT = new Color(230, 238, 247);

    private static final Color SIDEBAR_MUTED = new Color(170, 182, 196);

    private static final Color BRAND_BLUE = new Color(33, 150, 243);

    private JPanel contentContainer;

    private JLabel statusLabel;

    private JLabel userLabel;

    private String displayName = "Usuario";

    private String currentView = "dashboard";

    private String activeNav = "dashboard";

    private final Map<String, JButton> navButtons = new HashMap<>();

    private DashboardView dashboardView;

    private PacientesView pacientesView;

    private MedicosView medicosView;

    private DiagnosticosView diagnosticosView;

    private UsuariosView usuariosView;

    public MediLinkGUI() {

        this("Usuario");

    }

    public MediLinkGUI(String displayName) {

    super("MediLink " + app.AppInfo.VERSION + " - Gestión Hospitalaria");

        if (displayName != null && !displayName.trim().isEmpty()) {

            this.displayName = displayName.trim();

        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {

            java.nio.file.Path cwd = java.nio.file.Paths.get("");

            java.nio.file.Path iconIco = cwd.resolve("icon.ico");

            java.nio.file.Path iconPng = cwd.resolve("icon.png");

            java.nio.file.Path logoPng = cwd.resolve("logo.png");

            java.util.List<java.awt.Image> icons = new java.util.ArrayList<>();

            if (java.nio.file.Files.exists(iconIco)) {

                try { icons.addAll(gui.util.IcoUtil.readIcoAll(iconIco)); } catch (Throwable t) { }

                if (icons.isEmpty()) {

                    try { icons.add(new javax.swing.ImageIcon(iconIco.toString()).getImage()); } catch (Throwable t) {}

                }

            }

            if (icons.isEmpty() && java.nio.file.Files.exists(iconPng)) {

                try { icons.add(new javax.swing.ImageIcon(iconPng.toString()).getImage()); } catch (Throwable t) {}

            }

            if (icons.isEmpty() && java.nio.file.Files.exists(logoPng)) {

                try { icons.add(new javax.swing.ImageIcon(logoPng.toString()).getImage()); } catch (Throwable t) {}

            }

            if (!icons.isEmpty()) {

                try {

                    java.util.Collections.sort(icons, new java.util.Comparator<java.awt.Image>() {

                        public int compare(java.awt.Image a, java.awt.Image b) {

                            int aw = a.getWidth(null), ah = a.getHeight(null);

                            int bw = b.getWidth(null), bh = b.getHeight(null);

                            int aarea = (aw <= 0 || ah <= 0) ? 0 : aw * ah;

                            int barea = (bw <= 0 || bh <= 0) ? 0 : bw * bh;

                            return Integer.compare(barea, aarea);

                        }

                    });

                } catch (Throwable ignore) {}

                setIconImages(icons);

            }

        } catch (Exception ignore) { }

    setSize(1100, 700);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        contentContainer = new JPanel(new BorderLayout());

        contentContainer.setBackground(LIGHT_BLUE);

        add(contentContainer, BorderLayout.CENTER);

    add(buildStatusBar(), BorderLayout.SOUTH);

        showDashboard();

        setActiveNav("dashboard");

        loadDashboardStats();

    }

    private JComponent buildSidebar() {

        JPanel sidebarContent = new ScrollableSidebarPanel();

        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));

        sidebarContent.setBackground(SIDEBAR_BG);

    sidebarContent.setBorder(new EmptyBorder(12, 12, 12, 12));

        sidebarContent.add(createSidebarLogoPanel());

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 14)));

        sidebarContent.add(createSectionLabel("PANEL PRINCIPAL"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 6)));

        sidebarContent.add(createSidebarButton("Dashboard", "dashboard"));

        sidebarContent.add(createSidebarButton("Estadísticas", "stats"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 14)));

        sidebarContent.add(createSectionLabel("GESTIÓN DE DATOS"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 6)));

        sidebarContent.add(createSidebarButton("Pacientes", "pacientes"));

        sidebarContent.add(createSidebarButton("Médicos", "medicos"));

        sidebarContent.add(createSidebarButton("Diagnósticos", "diagnosticos"));

        sidebarContent.add(createSidebarButton("Usuarios", "usuarios"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 14)));

        sidebarContent.add(createSectionLabel("ACCIONES RÁPIDAS"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 6)));

        sidebarContent.add(createSidebarButton("Nuevo Paciente", "add_paciente"));

        sidebarContent.add(createSidebarButton("Nuevo Médico", "add_medico"));

        sidebarContent.add(createSidebarButton("Nuevo Diagnóstico", "add_diagnostico"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 14)));

        sidebarContent.add(createSectionLabel("GESTIÓN DE ESTADO"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 6)));

        sidebarContent.add(createSidebarButton("Activar/Desactivar Médicos", "toggle_medicos"));

        sidebarContent.add(createSidebarButton("Activar/Desactivar Pacientes", "toggle_pacientes"));

        sidebarContent.add(createSidebarButton("Eliminar Diagnósticos", "delete_diagnosticos"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 14)));

        sidebarContent.add(createSectionLabel("SISTEMA"));

        sidebarContent.add(Box.createRigidArea(new Dimension(0, 6)));

        sidebarContent.add(createSidebarButton("Salud del Sistema", "health"));

        sidebarContent.add(createSidebarButton("© Créditos", "creditos"));

        JScrollPane scroll = new JScrollPane(sidebarContent, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(55, 66, 82)));

        scroll.getViewport().setBackground(SIDEBAR_BG);

        scroll.setWheelScrollingEnabled(true);

        scroll.getVerticalScrollBar().setUnitIncrement(18);

        return scroll;

    }

    private static class ScrollableSidebarPanel extends JPanel implements Scrollable {

        @Override

        public Dimension getPreferredScrollableViewportSize() {

            return getPreferredSize();

        }

        @Override

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {

            return 18;

        }

        @Override

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {

            return visibleRect.height - 36;

        }

        @Override

        public boolean getScrollableTracksViewportWidth() {

            return true; // que se ajuste al ancho del viewport

        }

        @Override

        public boolean getScrollableTracksViewportHeight() {

            return false; // permitir scroll vertical

        }

    }

    private JComponent createSidebarLogoPanel() {

        JPanel wrapper = new JPanel(new GridBagLayout());

        wrapper.setOpaque(false);

        wrapper.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setOpaque(false);

        ImageIcon icon = new ImageIcon("logo.png");

        int maxW = 200, maxH = 90;

        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {

            double rawScale = Math.min((double) maxW / icon.getIconWidth(), (double) maxH / icon.getIconHeight());

            double scale = Math.min(1.0, rawScale);

            int w = (int) Math.round(icon.getIconWidth() * scale);

            int h = (int) Math.round(icon.getIconHeight() * scale);

            if (w > 0 && h > 0) {

                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);

                icon = new ImageIcon(scaled);

            }

        }

        JLabel logo = new JLabel(icon);

        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        logo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("Sistema Hospitalario");

        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));

        subtitle.setForeground(SIDEBAR_MUTED);

        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitle.setBorder(new EmptyBorder(8, 0, 0, 0));

        panel.add(logo);

        panel.add(subtitle);

        wrapper.add(panel, new GridBagConstraints());

        return wrapper;

    }

    private JLabel createSectionLabel(String text) {

        JLabel lbl = new JLabel(text);

        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

        lbl.setForeground(SIDEBAR_MUTED);

        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        lbl.setMaximumSize(new Dimension(230, 22));

        lbl.setBorder(new EmptyBorder(6, 8, 6, 8));

        return lbl;

    }

    private JButton createSidebarButton(String text, String action) {

        JButton btn = new JButton(text);

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.setHorizontalAlignment(SwingConstants.CENTER);

        btn.setMaximumSize(new Dimension(230, 40));

        btn.setFocusPainted(false);

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBackground(SIDEBAR_BTN_BG);

        btn.setOpaque(true);

        btn.setForeground(SIDEBAR_TEXT);

        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.setBorder(BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(new Color(64, 78, 98), 1),

                new EmptyBorder(8, 12, 8, 12)

        ));

        btn.addActionListener(e -> handleMenuAction(action));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override public void mouseEntered(java.awt.event.MouseEvent e) {

                if (!action.equals(activeNav)) btn.setBackground(SIDEBAR_BTN_HOVER);

            }

            @Override public void mouseExited(java.awt.event.MouseEvent e) {

                if (!action.equals(activeNav)) btn.setBackground(SIDEBAR_BTN_BG);

            }

        });

        navButtons.put(action, btn);

        styleSidebarButton(btn, action.equals(activeNav));

        return btn;

    }

    private void styleSidebarButton(JButton btn, boolean active) {

        if (active) {

            btn.setBackground(SIDEBAR_BTN_ACTIVE);

            btn.setBorder(BorderFactory.createCompoundBorder(

                    BorderFactory.createMatteBorder(0, 4, 0, 0, BRAND_BLUE),

                    new EmptyBorder(8, 10, 8, 10)

            ));

            btn.setForeground(Color.WHITE);

        } else {

            btn.setBackground(SIDEBAR_BTN_BG);

            btn.setBorder(BorderFactory.createCompoundBorder(

                    BorderFactory.createLineBorder(new Color(64, 78, 98), 1),

                    new EmptyBorder(8, 12, 8, 12)

            ));

            btn.setForeground(SIDEBAR_TEXT);

        }

    }

    private void setActiveNav(String action) {

        activeNav = action;

        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {

            styleSidebarButton(e.getValue(), e.getKey().equals(action));

        }

    }

    private JPanel buildStatusBar() {

        JPanel status = new JPanel(new BorderLayout());

        status.setBackground(SIDEBAR_BG);

        status.setBorder(new EmptyBorder(6, 12, 6, 12));

        statusLabel = new JLabel("Listo");

        statusLabel.setForeground(SIDEBAR_TEXT);

        userLabel = new JLabel("Conectado: " + displayName, SwingConstants.CENTER);

        userLabel.setForeground(SIDEBAR_TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        right.setOpaque(false);

        JButton logout = new JButton("Cerrar sesión");

        logout.setBackground(SIDEBAR_BTN_BG);

        logout.setForeground(SIDEBAR_TEXT);

        logout.setFocusPainted(false);

        logout.setBorder(new EmptyBorder(4, 10, 4, 10));

        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logout.addActionListener(e -> {

            dispose();

            SwingUtilities.invokeLater(() -> new gui.views.LoginFrame().setVisible(true));

        });

    JLabel copy = new JLabel("MediLink " + app.AppInfo.VERSION + " " + app.AppInfo.COPYRIGHT);

        copy.setForeground(SIDEBAR_TEXT);

        right.add(copy);

        right.add(logout);

        status.add(statusLabel, BorderLayout.WEST);

        status.add(userLabel, BorderLayout.CENTER);

        status.add(right, BorderLayout.EAST);

        return status;

    }

    private void handleMenuAction(String action) {

        setActiveNav(action);

        switch (action) {

            case "dashboard":

                showDashboard();

                break;

            case "stats":

                showStats();

                break;

            case "pacientes":

                showPacientes();

                break;

            case "medicos":

                showMedicos();

                break;

            case "diagnosticos":

                showDiagnosticos();

                break;

            case "usuarios":

                showUsuarios();

                break;

            case "add_paciente":

                showAddPacienteForm();

                break;

            case "add_medico":

                showAddMedicoForm();

                break;

            case "add_diagnostico":

                showAddDiagnosticoForm();

                break;

            case "toggle_medicos":

                showToggleMedicos();

                break;

            case "toggle_pacientes":

                showTogglePacientes();

                break;

            case "delete_diagnosticos":

                showDeleteDiagnosticos();

                break;

            case "health":

                checkConnection();

                break;

            case "creditos":

                showCreditos();

                break;

        }

    }

    private void setContent(Component comp) {

        contentContainer.removeAll();

        contentContainer.add(comp, BorderLayout.CENTER);

        contentContainer.revalidate();

        contentContainer.repaint();

    }

    private void showDashboard() {

        currentView = "dashboard";

        if (dashboardView == null) {

            dashboardView = new DashboardView(this::handleMenuAction);

        }

        setContent(dashboardView);

    }

    private void showStats() {

        try {

            currentView = "stats";

            int pacientes = countObjects(ApiClient.get("/pacientes"));

            String medicosJson = ApiClient.get("/medicos");

            int medicos = countObjects(medicosJson);

            int[] medicosAI = countActivosInactivos(medicosJson, "activo");

            int diagnosticos = countObjects(ApiClient.get("/diagnosticos"));

            int usuarios = countObjects(ApiClient.get("/usuarios"));

            EstadisticasView view = new EstadisticasView();

            view.render(pacientes, medicos, medicosAI[0], medicosAI[1], diagnosticos, usuarios);

            setContent(view);

            statusLabel.setText("📈 Estadísticas cargadas");

        } catch (Exception ex) {

            showTextContent("Error cargando estadísticas:\n\n" + ex.getMessage());

        }

    }

    private void showPacientes() {

        try {

            currentView = "pacientes";

            String json = ApiClient.get("/pacientes");

            int[] ai = countActivosInactivos(json, "activo");

            if (pacientesView == null) pacientesView = new PacientesView();

            pacientesView.render(json, ai[0], ai[1]);

            setContent(pacientesView);

            statusLabel.setText("Pacientes cargados");

        } catch (Exception ex) {

            showTextContent("Error cargando pacientes:\n\n" + ex.getMessage());

        }

    }

    private void showMedicos() {

        try {

            currentView = "medicos";

            String json = ApiClient.get("/medicos");

            int[] ai = countActivosInactivos(json, "activo");

            if (medicosView == null) medicosView = new MedicosView();

            medicosView.render(json, ai[0], ai[1]);

            setContent(medicosView);

            statusLabel.setText("Médicos cargados");

        } catch (Exception ex) {

            showTextContent("❌ Error cargando médicos:\n\n" + ex.getMessage());

        }

    }

    private void showDiagnosticos() {

        try {

            currentView = "diagnosticos";

            String json = ApiClient.get("/diagnosticos");

            if (diagnosticosView == null) diagnosticosView = new DiagnosticosView();

            diagnosticosView.render(json);

            setContent(diagnosticosView);

            statusLabel.setText("Diagnósticos cargados");

        } catch (Exception ex) {

            showTextContent(" Error cargando diagnósticos:\n\n" + ex.getMessage());

        }

    }

    private void showUsuarios() {

        try {

            currentView = "usuarios";

            String json = ApiClient.get("/usuarios");

            if (usuariosView == null) usuariosView = new UsuariosView();

            usuariosView.render(json);

            setContent(usuariosView);

            statusLabel.setText("Usuarios cargados");

        } catch (Exception ex) {

            showTextContent("❌ Error cargando usuarios:\n\n" + ex.getMessage());

        }

    }

    private void showAddPacienteForm() {

        AddPacienteView dialog = new AddPacienteView(

                this,

                this::showSuccessConfirmation,

                msg -> statusLabel.setText(msg),

                this::loadDashboardStats

        );

        dialog.setVisible(true);

    }

    private void showAddMedicoForm() {

        AddMedicoView dialog = new AddMedicoView(

                this,

                this::showSuccessConfirmation,

                msg -> statusLabel.setText(msg),

                params -> {

                    try {

                        String username = (params[0].split(" ")[0] + params[1].split(" ")[0]).toLowerCase();

                        String password = generarContrasenaAleatoria();

                        String body = String.format("{\"usuario\":\"%s\",\"contrasena\":\"%s\",\"rol\":\"MEDICO\",\"medico_id\":%s}",

                                escapeJson(username), escapeJson(password), params[3]);

                        String response = ApiClient.post("/usuarios/create", body);

                        return new String[]{username, password, response};

                    } catch (Exception ex) {

                        return null;

                    }

                },

                this::loadDashboardStats

        );

        dialog.setVisible(true);

    }

    private void showAddDiagnosticoForm() {

        AddDiagnosticoView dialog = new AddDiagnosticoView(

                this,

                this::showSuccessConfirmation,

                msg -> statusLabel.setText(msg),

                this::loadDashboardStats

        );

        dialog.setVisible(true);

    }

    private void showToggleMedicos() {

        currentView = "toggle_medicos";

        try {

            String medicosResponse = ApiClient.get("/medicos");

            ToggleMedicosView view = new ToggleMedicosView(medicosResponse, this::toggleMedicoStatus);

            setContent(view);

        } catch (Exception e) {

            showTextContent("❌ ERROR CARGANDO MÉDICOS\n\n" + e.getMessage());

        }

    }

    private void showTogglePacientes() {

        currentView = "toggle_pacientes";

        try {

            String pacientesResponse = ApiClient.get("/pacientes");

            TogglePacientesView view = new TogglePacientesView(pacientesResponse, this::togglePacienteStatus);

            setContent(view);

        } catch (Exception e) {

            showTextContent("❌ ERROR CARGANDO PACIENTES\n\n" + e.getMessage());

        }

    }

    private void showDeleteDiagnosticos() {

        currentView = "delete_diagnosticos";

        try {

            String diagnosticosResponse = ApiClient.get("/diagnosticos");

            DeleteDiagnosticosView view = new DeleteDiagnosticosView(diagnosticosResponse, this::deleteDiagnosticos);

            setContent(view);

        } catch (Exception e) {

            showTextContent("❌ ERROR CARGANDO DIAGNÓSTICOS\n\n" + e.getMessage());

        }

    }

    private void checkConnection() {

        currentView = "health";

        boolean connected = ApiClient.isBackendAvailable();

        HealthView view = new HealthView(connected, () -> {

            try { return ApiClient.get("/health"); } catch (Exception ex) { return ""; }

        });

        setContent(view);

        statusLabel.setText(connected ? "✅ Sistema conectado y operativo" : "❌ Error de conexión con el backend");

    }

    private void showCreditos() {

        currentView = "creditos";

        Creditos view = new Creditos();

        setContent(view);

    }

    private void toggleMedicoStatus(List<String> medicosIds, Boolean activar) {

        try {

            int changed = 0;

            int noChange = 0;

            String lastMessage = null;

            for (String id : medicosIds) {

                String body = "{\"id_medico\":" + id + ",\"activo\":" + activar + "}";

                String resp = ApiClient.post("/medicos/toggle-status", body);

                Boolean c = extractJsonBoolean(resp, "changed");

                if (Boolean.FALSE.equals(c)) {

                    noChange++;

                } else {

                    changed++;

                }

                String msg = extractJsonString(resp, "message");

                if (msg != null && !msg.isEmpty()) lastMessage = msg;

            }

            String status = activar ? "ACTIVACIÓN" : "DESACTIVACIÓN";

            String detalle;

            if (medicosIds.size() == 1 && lastMessage != null) {

                detalle = lastMessage;

            } else {

                String ya = activar ? "ya estaban activos" : "ya estaban inactivos";

                String hechos = activar ? "activados" : "desactivados";

                detalle = hechos + ": " + changed + " • " + ya + ": " + noChange;

            }

            Notificaciones.operacionEstado(this, true, status + " COMPLETADA", detalle);

            statusLabel.setText("✅ " + status.toLowerCase() + " de médicos completada");

            loadDashboardStats();

            if ("toggle_medicos".equals(currentView)) {

                showToggleMedicos();

                setActiveNav("toggle_medicos");

            }

        } catch (Exception e) {

            Notificaciones.operacionEstado(this, false, "ERROR DURANTE LA " + (activar ? "ACTIVACIÓN" : "DESACTIVACIÓN"), e.getMessage());

            statusLabel.setText("❌ Error en " + (activar ? "activación" : "desactivación") + " de médicos");

        }

    }

    private void togglePacienteStatus(List<String> pacientesIds, Boolean activar) {

        try {

            int changed = 0;

            int noChange = 0;

            String lastMessage = null;

            for (String id : pacientesIds) {

                String body = "{\"id_paciente\":" + id + ",\"activo\":" + activar + "}";

                String resp = ApiClient.post("/pacientes/toggle-status", body);

                Boolean c = extractJsonBoolean(resp, "changed");

                if (Boolean.FALSE.equals(c)) {

                    noChange++;

                } else {

                    changed++;

                }

                String msg = extractJsonString(resp, "message");

                if (msg != null && !msg.isEmpty()) lastMessage = msg;

            }

            String status = activar ? "ACTIVACIÓN" : "DESACTIVACIÓN";

            String detalle;

            if (pacientesIds.size() == 1 && lastMessage != null) {

                detalle = lastMessage;

            } else {

                String ya = activar ? "ya estaban activos" : "ya estaban inactivos";

                String hechos = activar ? "activados" : "desactivados";

                detalle = hechos + ": " + changed + " • " + ya + ": " + noChange;

            }

            Notificaciones.operacionEstado(this, true, status + " COMPLETADA", detalle);

            statusLabel.setText("✅ " + status.toLowerCase() + " de pacientes completada");

            loadDashboardStats();

            if ("toggle_pacientes".equals(currentView)) {

                showTogglePacientes();

                setActiveNav("toggle_pacientes");

            }

        } catch (Exception e) {

            Notificaciones.operacionEstado(this, false, "ERROR DURANTE LA " + (activar ? "ACTIVACIÓN" : "DESACTIVACIÓN"), e.getMessage());

            statusLabel.setText("❌ Error en " + (activar ? "activación" : "desactivación") + " de pacientes");

        }

    }

    // Utilidades simples para extraer valores JSON sin dependencias

    private Boolean extractJsonBoolean(String json, String key) {

        try {

            String pattern = "\"" + key + "\"\\s*:\\s*(true|false)";

            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);

            java.util.regex.Matcher m = p.matcher(json);

            if (m.find()) return Boolean.valueOf(m.group(1).toLowerCase());

        } catch (Exception ignored) {}

        return null;

    }

    private String extractJsonString(String json, String key) {

        try {

            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\\\"]*)\"";

            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);

            java.util.regex.Matcher m = p.matcher(json);

            if (m.find()) return m.group(1);

        } catch (Exception ignored) {}

        return null;

    }

    private void deleteDiagnosticos(List<String> ids) {

        try {

            StringBuilder processed = new StringBuilder();

            for (String id : ids) {

                String body = "{\"id_diagnostico\":" + id + "}";

                ApiClient.delete("/diagnosticos/delete", body);

                if (processed.length() > 0) processed.append(",");

                processed.append(id);

            }

            Notificaciones.operacionEstado(this, true, "ELIMINACIÓN COMPLETADA", "Diagnóstico(s) eliminados: " + processed);

            statusLabel.setText("✅ Eliminación de diagnósticos completada");

            loadDashboardStats();

            if ("delete_diagnosticos".equals(currentView)) {

                showDeleteDiagnosticos();

            }

        } catch (Exception e) {

            Notificaciones.error(this, "ERROR DURANTE LA ELIMINACIÓN", e.getMessage());

            statusLabel.setText("❌ Error en eliminación de diagnósticos");

        }

    }

    private void showSuccessConfirmation(String[] data) {

        String tipo = data[0];

        String nombre = data[1];

        String apellidos = data[2];

        String detalles = data[3];

        String cred = data.length > 4 ? data[4] : null;

        Notificaciones.exitoRegistro(this, tipo, nombre, apellidos, detalles, cred);

    }

    private void loadDashboardStats() {

        try {

            int totalPacientes = countObjects(ApiClient.get("/pacientes"));

            String medicosJson = ApiClient.get("/medicos");

            int totalMedicos = countObjects(medicosJson);

            int totalDiagnosticos = countObjects(ApiClient.get("/diagnosticos"));

            int totalUsuarios = countObjects(ApiClient.get("/usuarios"));

            if (dashboardView != null) {

                dashboardView.setCounts(totalPacientes, totalMedicos, totalDiagnosticos, totalUsuarios);

            }

        } catch (Exception ignored) { }

    }

    private int[] countActivosInactivos(String json, String key) {

        if (json == null) return new int[]{0, 0};

        int activos = 0, inactivos = 0;

        String trimmed = json.trim();

        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return new int[]{0, 0};

        String body = trimmed.substring(1, trimmed.length() - 1).trim();

        if (body.isEmpty()) return new int[]{0, 0};

        String[] objects = body.split("\\},\\s*\\{");

        for (String obj : objects) {

            String o = obj;

            if (!o.startsWith("{")) o = "{" + o;

            if (!o.endsWith("}")) o = o + "}";

            if (o.contains("\"" + key + "\":true")) activos++;

            else if (o.contains("\"" + key + "\":false")) inactivos++;

        }

        return new int[]{activos, inactivos};

    }

    private int countObjects(String jsonArray) {

        if (jsonArray == null) return 0;

        String t = jsonArray.trim();

        if (t.length() < 2 || t.charAt(0) != '[' || t.charAt(t.length() - 1) != ']') return 0;

        if (t.equals("[]")) return 0;

        int level = 0;

        boolean inString = false;

        boolean escape = false;

        int count = 0;

        for (int i = 1; i < t.length() - 1; i++) {

            char c = t.charAt(i);

            if (inString) {

                if (escape) {

                    escape = false;

                } else if (c == '\\') {

                    escape = true;

                } else if (c == '"') {

                    inString = false;

                }

                continue;

            }

            if (c == '"') { inString = true; continue; }

            if (c == '{') {

                if (level == 0) count++; // nuevo objeto de nivel superior

                level++;

            } else if (c == '}') {

                if (level > 0) level--;

            }

        }

        return count;

    }

    private void showTextContent(String content) {

        JTextArea area = new JTextArea(content);

        area.setEditable(false);

        area.setBackground(CARD_WHITE);

        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        setContent(new JScrollPane(area));

    }

    private String escapeJson(String s) {

        if (s == null) return "";

        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");

    }

    private String generarContrasenaAleatoria() {

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

        StringBuilder sb = new StringBuilder();

        java.util.Random rnd = new java.util.Random();

        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));

        return sb.toString();

    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new gui.views.LoginFrame().setVisible(true));

    }

}

