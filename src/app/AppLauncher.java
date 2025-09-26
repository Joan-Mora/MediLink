package app;

/**

 * Lanzador unificado: inicia el servidor HTTP y la GUI en el MISMO proceso JVM.

 * Esto hace al empaquetado más confiable (jpackage) y evita depender de 2 procesos Java.

 */

public final class AppLauncher {

    private AppLauncher() {}

    public static void main(String[] args) {

        // 1) Arrancar el servidor en segundo plano (daemon)

        Thread serverThread = new Thread(() -> {

            try {

                app.Main.main(new String[0]);

            } catch (Throwable t) {

                t.printStackTrace();

            }

        }, "medilink-server");

        serverThread.setDaemon(true); // no impide que el proceso termine cuando se cierre la GUI

        serverThread.start();

        // 2) Iniciar la GUI (Login) en EDT

        javax.swing.SwingUtilities.invokeLater(() -> {

            try {

                new gui.views.LoginFrame().setVisible(true);

            } catch (Throwable t) {

                t.printStackTrace();

                javax.swing.JOptionPane.showMessageDialog(null,

                        "Error iniciando GUI: " + t.getMessage(),

                        "MediLink",

                        javax.swing.JOptionPane.ERROR_MESSAGE);

            }

        });

    }

}

