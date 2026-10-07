import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.util.function.Consumer;

/**
 * Cliente Swing: tela inicial com Login / Cadastro, telas proprias para cada um e painel do token.
 * A janela pode ser redimensionada ou maximizada; fontes e espacamentos acompanham o tamanho.
 * Uso: java Cliente [host]
 */
public class Cliente extends JFrame {
    static final Color NAVY = new Color(0x0B2545);
    static final Color NAVY_HOVER = new Color(0x1A3F70);
    static final Color MUTED = new Color(0x4A6285);
    static final Color LINE = new Color(0xC3D3E6);
    static final Color AMBER = new Color(0xFFB547);
    static final Color AMBER_HOVER = new Color(0xF5A01F);
    static final Color CLARO = new Color(0xE6EEF7);
    static final Color CLARO_HOVER = new Color(0xD3E2F2);
    static final Color OK = new Color(0x15803D);
    static final Color ERR = new Color(0xB91C1C);
    static final String FAMILIA = "Segoe UI";
    private static final int INTERVALO_TOKEN_S = 10;

    private final Comunicador com;
    private final InetAddress servidor;

    private final CardLayout raiz = new CardLayout();
    private final JPanel corpo = new Fundo(raiz);

    private final Campo campoNome = new Campo();
    private final Campo campoEmailCad = new Campo();
    private final CampoSenha campoSenhaCad = new CampoSenha();
    private final Campo campoEmailLogin = new Campo();
    private final CampoSenha campoSenhaLogin = new CampoSenha();
    private final Botao btnHomeLogin = new Botao("Login", Botao.AMBAR, 19);
    private final Botao btnHomeCadastro = new Botao("Cadastro", Botao.MARINHO, 19);
    private final Botao btnCriar = new Botao("Criar conta", Botao.AMBAR, 15);
    private final Botao btnEntrar = new Botao("Entrar", Botao.AMBAR, 15);
    private final Botao btnVoltarLogin = new Botao("Voltar", Botao.LEVE, 15);
    private final Botao btnVoltarCad = new Botao("Voltar", Botao.LEVE, 15);
    private final Rotulo statusLogin = new Rotulo(" ", 14, Font.PLAIN, MUTED, SwingConstants.LEFT);
    private final Rotulo statusCad = new Rotulo(" ", 14, Font.PLAIN, MUTED, SwingConstants.LEFT);

    private final Rotulo lblOla = new Rotulo(" ", 26, Font.BOLD, NAVY, SwingConstants.CENTER);
    private final Relogio relogio = new Relogio();
    private final Botao btnCopiar = new Botao("Copiar token", Botao.AMBAR, 15);
    private final Botao btnSair = new Botao("Sair", Botao.MARINHO, 15);

    private final Timer timerToken = new Timer(INTERVALO_TOKEN_S * 1000, e -> solicitarToken());
    private final Timer timerRelogio = new Timer(50, e -> tique());
    private String emailLogado;
    private boolean aguardandoToken;
    private float escala = 1f;

    public Cliente(String host) throws Exception {
        super("Token Seguro");
        this.servidor = InetAddress.getByName(host);
        this.com = new Comunicador();

        corpo.add(telaInicio(), "inicio");
        corpo.add(telaLogin(), "login");
        corpo.add(telaCadastro(), "cadastro");
        corpo.add(telaPainel(), "painel");
        setContentPane(corpo);

        btnHomeLogin.addActionListener(e -> ir("login"));
        btnHomeCadastro.addActionListener(e -> ir("cadastro"));
        btnVoltarLogin.addActionListener(e -> ir("inicio"));
        btnVoltarCad.addActionListener(e -> ir("inicio"));
        btnCriar.addActionListener(e -> cadastrar());
        btnEntrar.addActionListener(e -> entrar());
        btnCopiar.addActionListener(e -> copiar());
        btnSair.addActionListener(e -> sair());

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { aplicarEscala(); }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 520));
        setSize(960, 780);
        setLocationRelativeTo(null);
    }

    // ---------- escala ----------

    private void aplicarEscala() {
        float s = Math.max(0.8f, Math.min(2.4f, Math.min(getWidth() / 900f, getHeight() / 760f)));
        if (Math.abs(s - escala) < 0.01f) return;
        escala = s;
        escalar(corpo, s);
        corpo.revalidate();
        corpo.repaint();
    }

    private static void escalar(Container c, float s) {
        for (Component k : c.getComponents()) {
            if (k instanceof Escalavel e) e.escalar(s);
            if (k instanceof Container cc) escalar(cc, s);
        }
    }

    // ---------- telas ----------

    private JComponent telaInicio() {
        Cartao tela = new Cartao(null, false, 48);
        tela.setLayout(new BorderLayout());

        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);
        JPanel texto = pilha();
        texto.add(centrado(new Rotulo("AUTENTICAÇÃO POR TOKEN", 14, Font.BOLD, MUTED, SwingConstants.CENTER)));
        texto.add(new Espaco(10));
        texto.add(centrado(new Rotulo("Token Seguro", 60, Font.BOLD, NAVY, SwingConstants.CENTER)));
        texto.add(new Espaco(10));
        texto.add(centrado(new Rotulo("Chaves temporárias de 60 segundos, geradas pelo servidor via UDP.",
                19, Font.PLAIN, MUTED, SwingConstants.CENTER)));
        hero.add(texto);
        tela.add(hero, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new GridLayout(1, 2, 18, 0));
        botoes.setOpaque(false);
        botoes.add(btnHomeLogin);
        botoes.add(btnHomeCadastro);
        tela.add(botoes, BorderLayout.SOUTH);
        return tela;
    }

    private JComponent telaLogin() {
        Cartao c = cartaoCentral();
        empilhar(c, new Rotulo("Entrar", 30, Font.BOLD, NAVY, SwingConstants.LEFT), 0);
        empilhar(c, new Rotulo("Use seu e-mail e senha. O token aparece na tela seguinte.", 14, Font.PLAIN, MUTED, SwingConstants.LEFT), 2);
        empilhar(c, rotulo("E-mail"), 20);
        empilhar(c, campoEmailLogin, 4);
        empilhar(c, rotulo("Senha"), 12);
        empilhar(c, campoSenhaLogin, 4);
        empilhar(c, btnEntrar, 18);
        empilhar(c, btnVoltarLogin, 8);
        empilhar(c, statusLogin, 12);
        return centralizar(c);
    }

    private JComponent telaCadastro() {
        Cartao c = cartaoCentral();
        empilhar(c, new Rotulo("Criar conta", 30, Font.BOLD, NAVY, SwingConstants.LEFT), 0);
        empilhar(c, new Rotulo("Informe seu nome completo e e-mail.", 14, Font.PLAIN, MUTED, SwingConstants.LEFT), 2);
        empilhar(c, rotulo("Nome completo"), 20);
        empilhar(c, campoNome, 4);
        empilhar(c, rotulo("E-mail"), 12);
        empilhar(c, campoEmailCad, 4);
        empilhar(c, rotulo("Senha (mínimo 4 caracteres)"), 12);
        empilhar(c, campoSenhaCad, 4);
        empilhar(c, btnCriar, 18);
        empilhar(c, btnVoltarCad, 8);
        empilhar(c, statusCad, 12);
        return centralizar(c);
    }

    private JComponent telaPainel() {
        Cartao c = new Cartao(null, false, 10);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(centrado(lblOla));
        c.add(new Espaco(20));
        c.add(centrado(relogio));
        c.add(new Espaco(22));
        JPanel linha = new JPanel(new GridLayout(1, 2, 12, 0));
        linha.setOpaque(false);
        linha.add(btnCopiar);
        linha.add(btnSair);
        c.add(centrado(linha));
        return centralizar(c);
    }

    private static Cartao cartaoCentral() {
        Cartao c = new Cartao(Color.WHITE, true, 30);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        return c;
    }

    private static JComponent centralizar(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private static JPanel pilha() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static <T extends JComponent> T centrado(T c) {
        c.setAlignmentX(CENTER_ALIGNMENT);
        return c;
    }

    private static void empilhar(JPanel p, JComponent c, int espacoAcima) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        if (espacoAcima > 0) p.add(new Espaco(espacoAcima));
        p.add(c);
    }

    private static Rotulo rotulo(String t) {
        return new Rotulo(t, 12, Font.BOLD, MUTED, SwingConstants.LEFT);
    }

    // ---------- comportamento ----------

    void mostrar(String tela) {
        raiz.show(corpo, tela);
        getRootPane().setDefaultButton(switch (tela) {
            case "login" -> btnEntrar;
            case "cadastro" -> btnCriar;
            default -> null;
        });
    }

    private void ir(String tela) {
        statusLogin.setText(" ");
        statusCad.setText(" ");
        mostrar(tela);
    }

    private void msg(Rotulo alvo, String texto, boolean ok) {
        alvo.setForeground(ok ? OK : ERR);
        alvo.setText("<html><div style='width:" + Math.round(330 * escala) + "px'>" + texto);
    }

    /** Envia e aguarda a resposta fora da thread de eventos. */
    private void requisitar(String msg, Consumer<String> aoResponder) {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                synchronized (com) {
                    com.enviar(msg, servidor, Servidor.PORTA);
                    Comunicador.Mensagem r = com.receber(3000);
                    return r == null ? "ERRO;Servidor não respondeu" : r.texto();
                }
            }
            @Override protected void done() {
                try { aoResponder.accept(get()); }
                catch (Exception ex) { aoResponder.accept("ERRO;" + ex.getMessage()); }
            }
        }.execute();
    }

    private static String detalhe(String resposta) {
        String[] c = resposta.split(";", 2);
        return c.length > 1 ? c[1] : resposta;
    }

    private void cadastrar() {
        String nome = campoNome.getText().trim(), email = campoEmailCad.getText().trim();
        String senha = new String(campoSenhaCad.getPassword());
        if (nome.isEmpty() || email.isEmpty() || nome.contains(";") || email.contains(";")) {
            msg(statusCad, "Informe nome e e-mail válidos (sem ';').", false);
            return;
        }
        if (senha.length() < 4 || senha.contains(";")) {
            msg(statusCad, "A senha precisa ter ao menos 4 caracteres e não pode ter ';'.", false);
            return;
        }
        requisitar("CADASTRO;" + nome + ";" + email + ";" + senha, r -> {
            if (r.startsWith("OK")) {
                campoNome.setText("");
                campoEmailCad.setText("");
                campoSenhaCad.setText("");
                iniciarSessao(email, nome);
            } else {
                msg(statusCad, detalhe(r), false);
            }
        });
    }

    private void entrar() {
        String email = campoEmailLogin.getText().trim();
        String senha = new String(campoSenhaLogin.getPassword());
        if (email.isEmpty() || senha.isEmpty() || email.contains(";") || senha.contains(";")) {
            msg(statusLogin, "Informe e-mail e senha.", false);
            return;
        }
        requisitar("LOGIN;" + email + ";" + senha, r -> {
            if (r.startsWith("OK")) {
                campoSenhaLogin.setText("");
                iniciarSessao(email, detalhe(r));
            } else {
                msg(statusLogin, detalhe(r), false);
            }
        });
    }

    /** Cadastro ou login concluido: abre a tela do token (relogio de 60 s). */
    private void iniciarSessao(String email, String nome) {
        emailLogado = email;
        aguardandoToken = false;
        lblOla.setText("Olá, " + nome + "!");
        relogio.limpar();
        mostrar("painel");
        solicitarToken();
        timerToken.start();
        timerRelogio.start();
    }

    private void sair() {
        timerToken.stop();
        timerRelogio.stop();
        emailLogado = null;
        campoSenhaLogin.setText("");
        ir("inicio");
    }

    private void solicitarToken() {
        if (emailLogado == null) return;
        aguardandoToken = true;
        requisitar("TOKEN;" + emailLogado, r -> {
            aguardandoToken = false;
            String[] c = r.split(";");
            if (emailLogado == null) return;
            if (c[0].equals("TOKEN") && c.length == 3) {
                relogio.definir(c[1], Integer.parseInt(c[2]) * 1000L);
            } else {
                relogio.avisar(detalhe(r));
            }
        });
    }

    /** Anima o relogio e busca o proximo token assim que o atual expira. */
    private void tique() {
        relogio.repaint();
        if (relogio.expirou() && !aguardandoToken) solicitarToken();
    }

    private void copiar() {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(relogio.getToken()), null);
        btnCopiar.setText("Copiado!");
        Timer t = new Timer(1500, e -> btnCopiar.setText("Copiar token"));
        t.setRepeats(false);
        t.start();
    }

    // ---------- componentes visuais (todos acompanham a escala da janela) ----------

    interface Escalavel { void escalar(float s); }

    static int px(float base, float s) { return Math.round(base * s); }

    static class Rotulo extends JLabel implements Escalavel {
        private final float base;
        private final int estilo;
        private final String familia;

        Rotulo(String texto, float base, int estilo, Color cor, int alinhamento, String familia) {
            super(texto, alinhamento);
            this.base = base;
            this.estilo = estilo;
            this.familia = familia;
            setForeground(cor);
            escalar(1f);
        }

        Rotulo(String texto, float base, int estilo, Color cor, int alinhamento) {
            this(texto, base, estilo, cor, alinhamento, FAMILIA);
        }

        @Override public void escalar(float s) { setFont(new Font(familia, estilo, px(base, s))); }
    }

    static class Espaco extends Box.Filler implements Escalavel {
        private final int base;

        Espaco(int base) {
            super(new Dimension(0, base), new Dimension(0, base), new Dimension(0, base));
            this.base = base;
        }

        @Override public void escalar(float s) {
            Dimension d = new Dimension(0, px(base, s));
            changeShape(d, d, d);
        }
    }

    /** Aplica o visual padrao (borda que destaca no foco, fonte escalavel) a um campo de texto. */
    static class EstiloCampo {
        private final JTextComponent campo;
        private boolean foco;
        private float s = 1f;

        EstiloCampo(JTextComponent campo) {
            this.campo = campo;
            campo.setForeground(NAVY);
            campo.setCaretColor(NAVY);
            campo.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { foco = true; aplicar(); }
                @Override public void focusLost(FocusEvent e) { foco = false; aplicar(); }
            });
            escalar(1f);
        }

        private void aplicar() {
            int extra = foco ? 1 : 0;
            campo.setBorder(new CompoundBorder(new LineBorder(foco ? NAVY : LINE, foco ? 2 : 1, true),
                    new EmptyBorder(px(9, s) - extra, px(12, s) - extra, px(9, s) - extra, px(12, s) - extra)));
        }

        void escalar(float s) {
            this.s = s;
            campo.setFont(new Font(FAMILIA, Font.PLAIN, px(15, s)));
            aplicar();
        }
    }

    static class Campo extends JTextField implements Escalavel {
        private final EstiloCampo estilo;

        Campo() {
            super(24);
            estilo = new EstiloCampo(this);
        }

        @Override public void escalar(float s) { estilo.escalar(s); }
    }

    static class CampoSenha extends JPasswordField implements Escalavel {
        private final EstiloCampo estilo;

        CampoSenha() {
            super(24);
            estilo = new EstiloCampo(this);
        }

        @Override public void escalar(float s) { estilo.escalar(s); }
    }

    static class Botao extends JButton implements Escalavel {
        static final int AMBAR = 0, MARINHO = 1, LEVE = 2;
        private final int estilo;
        private final float base;
        private float s = 1f;
        private boolean hover;

        Botao(String texto, int estilo, float base) {
            super(texto);
            this.estilo = estilo;
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(estilo == MARINHO ? Color.WHITE : NAVY);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
            escalar(1f);
        }

        @Override public void escalar(float s) {
            this.s = s;
            setFont(new Font(FAMILIA, Font.BOLD, px(base, s)));
            setBorder(new EmptyBorder(px(base * 0.8f, s), px(22, s), px(base * 0.8f, s), px(22, s)));
        }

        @Override public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fundo = switch (estilo) {
                case AMBAR -> hover ? AMBER_HOVER : AMBER;
                case MARINHO -> hover ? NAVY_HOVER : NAVY;
                default -> hover ? CLARO_HOVER : CLARO;
            };
            g.setColor(fundo);
            int arco = px(16, s);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), arco, arco);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static class Cartao extends JPanel implements Escalavel {
        private final Color fundo;
        private final boolean borda;
        private final int paddingBase;
        private float s = 1f;

        Cartao(Color fundo, boolean borda, int paddingBase) {
            this.fundo = fundo;
            this.borda = borda;
            this.paddingBase = paddingBase;
            setOpaque(false);
            escalar(1f);
        }

        @Override public void escalar(float s) {
            this.s = s;
            int p = px(paddingBase, s);
            setBorder(new EmptyBorder(p, p, p, p));
        }

        @Override protected void paintComponent(Graphics g0) {
            if (fundo == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arco = px(24, s);
            g.setColor(fundo);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            if (borda) {
                g.setColor(LINE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            }
            g.dispose();
        }
    }

    /** Relogio de 60 s: anel que se esvazia, token no centro e segundos restantes. */
    static class Relogio extends JComponent implements Escalavel {
        private static final double TOTAL_MS = 60_000;
        private String token;
        private String aviso;
        private long expiraEm;

        Relogio() {
            setOpaque(false);
            escalar(1f);
        }

        void limpar() { token = null; aviso = null; expiraEm = 0; repaint(); }

        void definir(String t, long restanteMs) {
            token = t;
            aviso = null;
            expiraEm = System.currentTimeMillis() + restanteMs;
            repaint();
        }

        void avisar(String a) { aviso = a; repaint(); }

        String getToken() { return token == null ? "" : token; }

        boolean expirou() { return token != null && System.currentTimeMillis() >= expiraEm; }

        @Override public void escalar(float s) {
            Dimension d = new Dimension(px(340, s), px(340, s));
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        private static void centro(Graphics2D g, String txt, int cx, int cy, Font f, Color cor) {
            g.setFont(f);
            g.setColor(cor);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
            int cx = getWidth() / 2, cy = getHeight() / 2;

            long restMs = token == null ? 0 : Math.max(0, expiraEm - System.currentTimeMillis());
            double frac = Math.min(1.0, restMs / TOTAL_MS);

            g.setColor(NAVY);
            g.fillOval(x, y, d, d);

            float larg = d * 0.035f;
            double inset = d * 0.06;
            double anel = d - 2 * inset;
            g.setStroke(new BasicStroke(larg));
            g.setColor(new Color(0x2F4B73));
            g.draw(new java.awt.geom.Ellipse2D.Double(x + inset, y + inset, anel, anel));

            // marcas dos 60 segundos, por dentro do anel
            double rExt = anel / 2 - larg * 1.4;
            for (int i = 0; i < 60; i++) {
                double ang = Math.toRadians(90 - i * 6);
                double rInt = rExt - d * (i % 5 == 0 ? 0.035 : 0.018);
                g.setStroke(new BasicStroke(Math.max(1f, d * (i % 5 == 0 ? 0.006f : 0.003f))));
                g.setColor(i % 5 == 0 ? new Color(0x6F8DB5) : new Color(0x3F5F8C));
                g.drawLine((int) (cx + rExt * Math.cos(ang)), (int) (cy - rExt * Math.sin(ang)),
                        (int) (cx + rInt * Math.cos(ang)), (int) (cy - rInt * Math.sin(ang)));
            }

            // anel de progresso (comeca no topo e gira no sentido anti-horario ao esvaziar)
            g.setStroke(new BasicStroke(larg, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(restMs <= 10_000 && token != null ? new Color(0xFF6B5A) : AMBER);
            if (frac > 0) {
                g.draw(new java.awt.geom.Arc2D.Double(x + inset, y + inset, anel, anel, 90, -360 * frac,
                        java.awt.geom.Arc2D.OPEN));
            }

            centro(g, "SEU TOKEN", cx, cy - (int) (d * 0.14), new Font(FAMILIA, Font.BOLD, Math.max(9, (int) (d * 0.034))),
                    new Color(0x9DB7D5));
            centro(g, token == null ? "···" : token, cx, cy,
                    new Font(Font.MONOSPACED, Font.BOLD, Math.max(10, (int) (d * 0.08))), Color.WHITE);
            if (aviso != null) {
                centro(g, aviso, cx, cy + (int) (d * 0.15), new Font(FAMILIA, Font.PLAIN, Math.max(9, (int) (d * 0.035))),
                        new Color(0xFFB4AA));
            } else {
                String seg = token == null ? "" : String.valueOf((restMs + 999) / 1000);
                centro(g, seg, cx, cy + (int) (d * 0.15), new Font(FAMILIA, Font.BOLD, Math.max(10, (int) (d * 0.09))), AMBER);
                centro(g, token == null ? "gerando..." : "segundos", cx, cy + (int) (d * 0.25),
                        new Font(FAMILIA, Font.PLAIN, Math.max(9, (int) (d * 0.032))), new Color(0x9DB7D5));
            }
            g.dispose();
        }
    }

    /** Fundo em degrade azul claro com circulos translucidos. */
    static class Fundo extends JPanel {
        Fundo(LayoutManager layout) { super(layout); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(0xE6F1FB), 0, h, new Color(0xB5D3EF)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 80));
            g.fillOval(-h / 5, -h / 4, (int) (h * 0.8), (int) (h * 0.8));
            g.setColor(new Color(11, 37, 69, 22));
            g.fillOval((int) (w * 0.68), (int) (h * 0.45), (int) (h * 0.9), (int) (h * 0.9));
            g.dispose();
        }
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        SwingUtilities.invokeLater(() -> {
            try { new Cliente(host).setVisible(true); }
            catch (Exception e) { JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage()); }
        });
    }
}
