package cliente;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import rmi.ServicoSessoes;

/**
 * Cliente gráfico (Swing). Mesma ideia da tela do sistema local,
 * mas a pesquisa agora é feita no servidor via RMI.
 *
 * Uso: java -cp bin cliente.JFrameCliente
 */
public class JFrameCliente extends JFrame {

    private ServicoSessoes servico;

    private final JTextField jTextFieldHost = new JTextField("localhost", 14);
    private final JTextField jTextFieldPorta = new JTextField(String.valueOf(ServicoSessoes.PORTA_PADRAO), 5);
    private final JButton jButtonConectar = new JButton("Conectar");
    private final JLabel jLabelStatus = new JLabel("Desconectado");

    private final JTextField jTextFieldNumeroCelularPesquisa = new JTextField(18);
    private final JButton jButtonPesquisar = new JButton("Pesquisar");
    private final JTextArea jTextAreaResultadoPesquisa = new JTextArea(12, 60);

    public JFrameCliente() {
        super("Sessões em Telefonia — Cliente");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // --- painel de conexão ---
        JPanel painelConexao = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelConexao.setBorder(BorderFactory.createTitledBorder("SERVIDOR"));
        painelConexao.add(new JLabel("Host:"));
        painelConexao.add(jTextFieldHost);
        painelConexao.add(new JLabel("Porta:"));
        painelConexao.add(jTextFieldPorta);
        painelConexao.add(jButtonConectar);
        painelConexao.add(jLabelStatus);

        // --- painel de pesquisa ---
        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelPesquisa.setBorder(BorderFactory.createTitledBorder("PESQUISA"));
        painelPesquisa.add(new JLabel("Número celular para pesquisa"));
        painelPesquisa.add(jTextFieldNumeroCelularPesquisa);
        painelPesquisa.add(jButtonPesquisar);

        JPanel topo = new JPanel(new GridLayout(2, 1));
        topo.add(painelConexao);
        topo.add(painelPesquisa);

        jTextAreaResultadoPesquisa.setEditable(false);
        jTextAreaResultadoPesquisa.setLineWrap(true);
        jTextAreaResultadoPesquisa.setWrapStyleWord(true);
        jTextAreaResultadoPesquisa.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(jTextAreaResultadoPesquisa);
        scroll.setBorder(BorderFactory.createTitledBorder("RESULTADO"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topo, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        setPesquisaHabilitada(false);
        jButtonConectar.addActionListener(e -> conectar());
        jButtonPesquisar.addActionListener(e -> pesquisar());
        jTextFieldNumeroCelularPesquisa.addActionListener(e -> pesquisar()); // ENTER pesquisa

        pack();
        setLocationRelativeTo(null);
    }

    private void setPesquisaHabilitada(boolean habilitada) {
        jTextFieldNumeroCelularPesquisa.setEnabled(habilitada);
        jButtonPesquisar.setEnabled(habilitada);
    }

    private void conectar() {
        try {
            String host = jTextFieldHost.getText().trim();
            int porta = Integer.parseInt(jTextFieldPorta.getText().trim());
            Registry registry = LocateRegistry.getRegistry(host, porta);
            servico = (ServicoSessoes) registry.lookup(ServicoSessoes.NOME_SERVICO);
            int total = servico.totalSessoes();
            jLabelStatus.setText("Conectado — " + total + " sessões");
            setPesquisaHabilitada(true);
            jTextFieldNumeroCelularPesquisa.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Porta inválida!");
        } catch (Exception ex) {
            servico = null;
            setPesquisaHabilitada(false);
            jLabelStatus.setText("Desconectado");
            JOptionPane.showMessageDialog(this, "Não foi possível conectar ao servidor:\n" + ex.getMessage());
        }
    }

    private void pesquisar() {
        String numero = jTextFieldNumeroCelularPesquisa.getText().trim();
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Número de celular não informado");
            jTextFieldNumeroCelularPesquisa.requestFocus();
            return;
        }
        try {
            String dados = servico.consultarSessao(numero);   // chamada remota
            if (dados == null) {
                JOptionPane.showMessageDialog(this, "Número de celular não encontrado");
                jTextFieldNumeroCelularPesquisa.selectAll();
                jTextFieldNumeroCelularPesquisa.requestFocus();
                return;
            }
            jTextAreaResultadoPesquisa.setText(dados);
        } catch (Exception ex) {
            setPesquisaHabilitada(false);
            jLabelStatus.setText("Conexão perdida");
            JOptionPane.showMessageDialog(this, "Erro na comunicação com o servidor:\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new JFrameCliente().setVisible(true));
    }
}
