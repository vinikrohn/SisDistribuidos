package exemplo2_gerarEmail;

import exemplo2_gerarEmail.model.ModeloCliente;
import exemplo2_gerarEmail.model.ModeloServidor;
import exemplo2_gerarEmail.model.Pessoa;
import exemplo2_gerarEmail.model.Resposta;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.JOptionPane;

/**
 * Versao enxuta do cliente, no mesmo estilo do ClienteTCPBasico da disciplina
 * (JOptionPane em vez de formulario), mas ja usando o Model do MVC.
 *
 * Serve para mostrar o ganho da separacao: a regra de comunicacao esta toda em
 * ModeloCliente, entao trocar a View (formulario Swing <-> JOptionPane) nao
 * exige mexer em socket nenhum.
 *
 * O cliente "oficial" do trabalho continua sendo ClienteMain (ClienteView).
 */
public class ClienteSimplesMain {

    public static void main(String[] args) {
        try {
            String nome = JOptionPane.showInputDialog(null, "Nome completo");
            if (nome == null || nome.trim().isEmpty()) {
                return;
            }

            String dataTexto = JOptionPane.showInputDialog(null, "Data de nascimento (dd/MM/yyyy)");
            if (dataTexto == null) {
                return;
            }

            LocalDate dataNascimento;
            try {
                dataNascimento = LocalDate.parse(dataTexto.trim(), Pessoa.FORMATO_DATA);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(null, "Data invalida. Use dd/MM/yyyy.");
                return;
            }

            ModeloCliente modelo = new ModeloCliente();
            modelo.configurar("localhost", ModeloServidor.PORTA_PADRAO);

            Resposta resposta = modelo.enviar(new Pessoa(nome, dataNascimento));
            Pessoa p = resposta.getPessoa();

            JOptionPane.showMessageDialog(null,
                    resposta.getMensagem() + "\n\n"
                    + "Nome:   " + p.getNome() + "\n"
                    + "E-mail: " + p.getEmail() + "\n"
                    + "Nasc.:  " + p.getDataNascimentoFormatada());

            System.out.println("Conexao encerrada");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }
}
