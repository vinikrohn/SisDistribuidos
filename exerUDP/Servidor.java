import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor UDP.
 * Protocolo (campos separados por ';'):
 *   CADASTRO;nome;email;senha -> OK;msg | ERRO;msg
 *   TOKEN;email               -> TOKEN;valor;segundosRestantes | ERRO;msg
 *   LOGIN;email;senha         -> OK;nome | ERRO;msg
 */
public class Servidor {
    public static final int PORTA = 5000;
    private static final long VALIDADE_MS = 60_000;
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final List<Pessoa> pessoas = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    private Pessoa buscar(String email) {
        for (Pessoa p : pessoas) {
            if (p.getEmail().equalsIgnoreCase(email)) return p;
        }
        return null;
    }

    private String gerarToken() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        return sb.toString();
    }

    private boolean tokenVigente(Pessoa p, long agora) {
        return p.getToken() != null && agora - p.getTokenCriadoEm() < VALIDADE_MS;
    }

    private String processar(String msg) {
        String[] c = msg.split(";", -1);
        switch (c[0]) {
            case "CADASTRO": {
                if (c.length != 4 || c[1].isBlank() || c[2].isBlank()) return "ERRO;Dados inválidos";
                if (c[3].length() < 4) return "ERRO;Senha muito curta (mínimo 4 caracteres)";
                String nome = c[1].trim(), email = c[2].trim();
                if (buscar(email) != null) return "ERRO;E-mail já cadastrado";
                pessoas.add(new Pessoa(nome, email, c[3]));
                System.out.println("Cadastrado: " + nome + " <" + email + "> (total: " + pessoas.size() + ")");
                return "OK;Cadastro realizado";
            }
            case "TOKEN": {
                if (c.length != 2) return "ERRO;Dados inválidos";
                Pessoa p = buscar(c[1].trim());
                if (p == null) return "ERRO;Usuário não cadastrado";
                long agora = System.currentTimeMillis();
                if (!tokenVigente(p, agora)) {
                    p.setToken(gerarToken(), agora);
                    System.out.println("Novo token para " + p.getEmail() + ": " + p.getToken());
                }
                long restante = (VALIDADE_MS - (agora - p.getTokenCriadoEm()) + 999) / 1000;
                return "TOKEN;" + p.getToken() + ";" + restante;
            }
            case "LOGIN": {
                if (c.length != 3) return "ERRO;Dados inválidos";
                Pessoa p = buscar(c[1].trim());
                if (p == null || !p.senhaConfere(c[2])) return "ERRO;E-mail ou senha incorretos";
                System.out.println("Login: " + p.getEmail());
                return "OK;" + p.getNome();
            }
            default:
                return "ERRO;Comando desconhecido";
        }
    }

    public void executar() throws IOException {
        Comunicador com = new Comunicador(PORTA);
        System.out.println("Servidor UDP ouvindo na porta " + PORTA);
        while (true) {
            Comunicador.Mensagem m = com.receber();
            String resposta = processar(m.texto());
            com.enviar(resposta, m.origem(), m.porta());
        }
    }

    public static void main(String[] args) throws IOException {
        new Servidor().executar();
    }
}
