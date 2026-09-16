package exemplo2_gerarEmail;

import exemplo2_gerarEmail.controller.ClienteController;
import exemplo2_gerarEmail.model.ModeloCliente;
import exemplo2_gerarEmail.view.ClienteView;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do CLIENTE.
 *
 * Pode ser executado varias vezes ao mesmo tempo para testar o servidor
 * multithread (varios clientes simultaneos).
 */
public class ClienteMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModeloCliente modelo = new ModeloCliente();
            ClienteView view = new ClienteView();
            ClienteController controller = new ClienteController(modelo, view);
            controller.exibir();
        });
    }
}
