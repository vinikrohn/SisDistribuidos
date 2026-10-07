import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/** Encapsula o envio e o recebimento de datagramas UDP. */
public class Comunicador {
    private static final int TAM_BUFFER = 1024;
    private final DatagramSocket socket;

    /** Socket em porta fixa (servidor). */
    public Comunicador(int porta) throws IOException {
        this.socket = new DatagramSocket(porta);
    }

    /** Socket em porta efemera (cliente). */
    public Comunicador() throws IOException {
        this.socket = new DatagramSocket();
    }

    public void enviar(String mensagem, InetAddress destino, int porta) throws IOException {
        byte[] dados = mensagem.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(dados, dados.length, destino, porta));
    }

    /** Bloqueia ate receber um datagrama. */
    public Mensagem receber() throws IOException {
        DatagramPacket pacote = new DatagramPacket(new byte[TAM_BUFFER], TAM_BUFFER);
        socket.receive(pacote);
        return new Mensagem(new String(pacote.getData(), 0, pacote.getLength(), StandardCharsets.UTF_8),
                pacote.getAddress(), pacote.getPort());
    }

    /** Recebe com timeout (ms); retorna null se estourar. */
    public Mensagem receber(int timeoutMs) throws IOException {
        socket.setSoTimeout(timeoutMs);
        try {
            return receber();
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    public void fechar() {
        socket.close();
    }

    public record Mensagem(String texto, InetAddress origem, int porta) {}
}
