package cliente;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import rmi.ServicoSessoes;

/**
 * Cliente de console: conecta ao servidor via RMI e consulta
 * os dados de sessão de números de celular.
 *
 * Uso: java -cp bin cliente.Cliente [host] [porta]
 */
public class Cliente {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : ServicoSessoes.PORTA_PADRAO;

        try {
            // conecta ao servidor
            Registry registry = LocateRegistry.getRegistry(host, porta);
            ServicoSessoes servico = (ServicoSessoes) registry.lookup(ServicoSessoes.NOME_SERVICO);
            System.out.println("Conectado a " + host + ":" + porta + " — " + servico.totalSessoes() + " sessões disponíveis.");

            Scanner teclado = new Scanner(System.in);
            while (true) {
                System.out.print("\nNúmero do celular (ENTER vazio para sair): ");
                if (!teclado.hasNextLine()) break;
                String numero = teclado.nextLine().trim();
                if (numero.isEmpty()) break;

                // consulta remota
                String dados = servico.consultarSessao(numero);
                if (dados == null) {
                    System.out.println("Número de celular não encontrado.");
                } else {
                    System.out.println(dados);
                }
            }
            System.out.println("Cliente encerrado.");
        } catch (Exception e) {
            System.err.println("Não foi possível falar com o servidor em " + host + ":" + porta + " -> " + e.getMessage());
        }
    }
}
