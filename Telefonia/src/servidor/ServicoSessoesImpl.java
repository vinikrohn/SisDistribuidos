package servidor;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import model.Sessao;
import model.TabelaHash;
import rmi.ServicoSessoes;

/**
 * Implementação do serviço remoto. Guarda a TabelaHash (HashMap) com
 * todas as sessões e responde às consultas dos clientes.
 */
public class ServicoSessoesImpl extends UnicastRemoteObject implements ServicoSessoes {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TabelaHash hashSessoes;

    public ServicoSessoesImpl(TabelaHash hashSessoes) throws RemoteException {
        super();
        this.hashSessoes = hashSessoes;
    }

    @Override
    public String consultarSessao(String numeroCelular) throws RemoteException {
        String numero = numeroCelular == null ? "" : numeroCelular.trim();
        Sessao sessao = hashSessoes.sessoes.get(numero);   // busca O(1) no HashMap
        log("consulta " + numero + " -> " + (sessao != null ? "encontrado" : "NÃO encontrado"));
        return sessao == null ? null : sessao.toString();
    }

    @Override
    public int totalSessoes() throws RemoteException {
        return hashSessoes.sessoes.size();
    }

    private void log(String mensagem) {
        String origem;
        try {
            origem = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            origem = "local";
        }
        System.out.println("[" + LocalTime.now().format(HORA) + "] cliente " + origem + ": " + mensagem);
    }
}
