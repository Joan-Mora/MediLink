package gui.views;

import gui.models.ApiClient;

import javax.swing.*;

import javax.swing.border.EmptyBorder;

import java.awt.*;

public class LoginDialog extends JDialog {

    private boolean authenticated = false;

    public LoginDialog(JFrame owner) {

        super(owner, "Iniciar sesión", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setSize(420, 260);

        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new BorderLayout(0, 12));

        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("🔐 Acceso Administrativo", SwingConstants.CENTER);

        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();

        form.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();

        gc.insets = new Insets(6, 6, 6, 6);

        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usuario = new JTextField();

        JPasswordField pass = new JPasswordField();

        gc.gridx = 0; gc.gridy = 0; form.add(new JLabel("Usuario"), gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; form.add(usuario, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; form.add(new JLabel("Contraseña"), gc);

        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; form.add(pass, gc);

        JButton ingresar = new JButton("Ingresar");

        ingresar.setBackground(new Color(33, 150, 243));

        ingresar.setForeground(Color.WHITE);

        ingresar.setFocusPainted(false);

        ingresar.addActionListener(e -> {

            String u = usuario.getText().trim();

            String p = new String(pass.getPassword());

            if (u.isEmpty() || p.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Completa usuario y contraseña", "Campos requeridos", JOptionPane.WARNING_MESSAGE);

                return;

            }

            try {

                String body = "{\"nombre\":" + ApiClient.escapeJson(u) + ",\"contrasena\":" + ApiClient.escapeJson(p) + "}";

                String resp = ApiClient.post("/auth/login", body);

                if (resp.contains("\"success\":true")) {

                    authenticated = true;

                    dispose();

                } else {

                    JOptionPane.showMessageDialog(this, "Credenciales inválidas", "Acceso denegado", JOptionPane.ERROR_MESSAGE);

                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

            }

        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        south.add(ingresar);

        panel.add(form, BorderLayout.CENTER);

        panel.add(south, BorderLayout.SOUTH);

        setContentPane(panel);

    }

    public boolean showAndAuthenticate() {

        setVisible(true);

        return authenticated;

    }

}

