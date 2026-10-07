import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Usuario cadastrado no servidor: senha (guardada com hash e sal) e token temporario. */
public class Pessoa {
    private final String nome;
    private final String email;
    private final String sal;
    private final String senhaHash;
    private String token;
    private long tokenCriadoEm;

    public Pessoa(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        byte[] s = new byte[16];
        new SecureRandom().nextBytes(s);
        this.sal = Base64.getEncoder().encodeToString(s);
        this.senhaHash = hash(sal, senha);
    }

    private static String hash(String sal, String senha) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest((sal + senha).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean senhaConfere(String senha) {
        return MessageDigest.isEqual(senhaHash.getBytes(StandardCharsets.UTF_8),
                hash(sal, senha).getBytes(StandardCharsets.UTF_8));
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public long getTokenCriadoEm() { return tokenCriadoEm; }

    public void setToken(String token, long criadoEm) {
        this.token = token;
        this.tokenCriadoEm = criadoEm;
    }

    @Override
    public String toString() { return nome + " <" + email + ">"; }
}
