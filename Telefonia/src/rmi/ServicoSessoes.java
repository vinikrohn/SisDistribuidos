package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto (RMI) compartilhado entre servidor e cliente.
 * O cliente só conhece esta interface; a implementação fica no servidor.
 *
 * Os métodos trafegam apenas String/int, então a classe Sessao do professor
 * fica intacta (não precisa ser Serializable) e qualquer máquina consegue
 * conversar com qualquer servidor.
 */
public interface ServicoSessoes extends Remote {

    /** Nome com que o serviço é registrado no RMI Registry. */
    String NOME_SERVICO = "ServicoSessoes";

    /** Porta padrão do RMI Registry. */
    int PORTA_PADRAO = 1099;

    /**
     * Retorna os dados da sessão do número informado (sessao.toString()),
     * ou null se o número não existir na base.
     */
    String consultarSessao(String numeroCelular) throws RemoteException;

    /** Quantidade de sessões carregadas no HashMap do servidor. */
    int totalSessoes() throws RemoteException;
}
