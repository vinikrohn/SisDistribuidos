package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import model.TabelaHash;
import rmi.ServicoSessoes;
import utilidades.Util;

/**
 * Servidor de sessões de telefonia.
 *
 * 1. Abre a base de sessões (arquivo texto);
 * 2. Adiciona as sessões em um HashMap (TabelaHash);
 * 3. Publica o serviço no RMI Registry para os clientes consultarem.
 *
 * Uso: java -cp bin servidor.Servidor [arquivoBase] [porta]
 */
public class Servidor {

    public static void main(String[] args) {
        String arquivo = args.length > 0 ? args[0] : "telephony_sessions.txt";
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : ServicoSessoes.PORTA_PADRAO;

        // 1 e 2: abre a base e carrega no HashMap
        TabelaHash hashSessoes = new TabelaHash();
        StringBuilder dadosSessoes = new StringBuilder();
        String erro = Util.carregarArquivoEmHashMap(arquivo, hashSessoes.sessoes, dadosSessoes);
        if (!erro.isEmpty()) {
            System.err.println(erro);
            System.exit(1);
        }
        System.out.println("Base carregada: " + arquivo + " (" + hashSessoes.sessoes.size() + " sessões no HashMap)");

        // 3: publica o serviço via RMI
        try {
            ServicoSessoesImpl servico = new ServicoSessoesImpl(hashSessoes);
            Registry registry = LocateRegistry.createRegistry(porta);
            registry.rebind(ServicoSessoes.NOME_SERVICO, servico);
            System.out.println("Servidor RMI no ar: rmi://<ip-do-servidor>:" + porta + "/" + ServicoSessoes.NOME_SERVICO);
            System.out.println("Aguardando consultas... (Ctrl+C para encerrar)");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor RMI: " + e.getMessage());
            System.exit(1);
        }
    }
}
