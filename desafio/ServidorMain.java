package exemplo2_gerarEmail;

import exemplo2_gerarEmail.controller.ServidorController;
import exemplo2_gerarEmail.model.ModeloServidor;
import exemplo2_gerarEmail.view.ServidorView;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do SERVIDOR.
 *
 * O main apenas monta o trio MVC e entrega o controle ao Controller.
 * A GUI e criada dentro da Event Dispatch Thread (invokeLater), como manda a
 * regra do Swing.
 */
public class ServidorMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModeloServidor modelo = new ModeloServidor();
            ServidorView view = new ServidorView();
            ServidorController controller = new ServidorController(modelo, view);
            controller.exibir();
        });
    }
}
