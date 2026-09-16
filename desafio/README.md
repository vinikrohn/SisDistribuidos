# Cadastro Acadêmico — Cliente/Servidor TCP multithread em MVC

Versão MVC do exercício, mantendo as convenções do código base da disciplina: pacote `exemplo2_gerarEmail`, porta **50000**, `ObjectOutputStream`/`ObjectInputStream` e a classe `Pessoa`.

## Estrutura

```
src/exemplo2_gerarEmail/
 ├─ ServidorMain.java          monta o trio MVC do servidor
 ├─ ClienteMain.java           monta o trio MVC do cliente (formulário Swing)
 ├─ ClienteSimplesMain.java    cliente no estilo do ClienteTCPBasico (JOptionPane)
 │
 ├─ model/                     DADOS + REGRAS + REDE  (zero import de Swing)
 │   ├─ Pessoa.java              entidade serializável que trafega na rede
 │   ├─ Resposta.java            envelope devolvido pelo servidor
 │   ├─ GeradorEmail.java        regra primeiro.ultimo.ano@ufn.edu.br
 │   ├─ ModeloServidor.java      lista sincronizada + ServerSocket + Observer
 │   ├─ AtendimentoCliente.java  Runnable: uma thread por cliente
 │   ├─ ModeloCliente.java       comunicação do cliente
 │   └─ ServidorObserver.java    contrato Model → Controller
 │
 ├─ view/                      APENAS TELA (passiva)
 │   ├─ ServidorView.java        JTextArea de log + JList de pessoas
 │   └─ ClienteView.java         2 campos + botão Enviar + 3 campos bloqueados
 │
 ├─ controller/                LIGAÇÃO View ↔ Model
 │   ├─ ServidorController.java  observa o Model, atualiza a View na EDT
 │   └─ ClienteController.java   valida campos e chama o Model via SwingWorker
 │
 └─ teste/
     └─ TesteMVC.java          teste headless com 40 clientes simultâneos
```

## Como as camadas conversam

```
        clique no botão                      comando
  VIEW ──────────────────► CONTROLLER ──────────────────► MODEL
   ▲   (addEnviarListener)      │      (enviar / iniciar)    │
   │                            │                            │
   └────────────────────────────┘◄───────────────────────────┘
        setDadosRecebidos()            notificação (Observer)
        appendLog()                    logAtualizado()
        adicionarPessoaNaLista()       pessoaCadastrada()
```

Regras que a arquitetura respeita:

| Camada | Pode conhecer | Nunca conhece |
|---|---|---|
| **Model** | nada das outras | `javax.swing`, a View, o Controller |
| **View** | `Pessoa` (só para exibir) | `Socket`, validações, regra do e-mail |
| **Controller** | View e Model | — |

Como o Model não importa Swing, ele roda no terminal: é exatamente isso que o `TesteMVC` faz, registrando um `ServidorObserver` vazio no lugar do Controller.

## Como executar

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")

java -cp bin exemplo2_gerarEmail.ServidorMain        # 1) servidor, clique em "Iniciar"
java -cp bin exemplo2_gerarEmail.ClienteMain         # 2) cliente (pode abrir vários)
java -cp bin exemplo2_gerarEmail.ClienteSimplesMain  # cliente estilo JOptionPane
java -cp bin exemplo2_gerarEmail.teste.TesteMVC      # teste de concorrência
```

Windows: `javac -encoding UTF-8 -d bin src\exemplo2_gerarEmail\*.java src\exemplo2_gerarEmail\model\*.java src\exemplo2_gerarEmail\view\*.java src\exemplo2_gerarEmail\controller\*.java src\exemplo2_gerarEmail\teste\*.java`

## O que mudou em relação ao código base

| Código base (`ClienteTCPBasico`) | Versão MVC | Por quê |
|---|---|---|
| socket direto dentro do `main` | `ModeloCliente.enviar()` | rede é responsabilidade do Model |
| envia uma `String` com o nome | envia o objeto `Pessoa` | enunciado pede o objeto encapsulado |
| servidor devolve `null` se já existir | devolve `Resposta` (Pessoa + flag `novoCadastro`) | enunciado pede o objeto "criado/localizado"; o flag ainda avisa a duplicata |
| `JOptionPane` | `ClienteView` com `JTextField` | enunciado pede formulário com 3 campos bloqueados |
| tudo na thread principal | thread por cliente + `SwingWorker` | evita travar a GUI e atender um cliente por vez |

`ClienteSimplesMain` foi mantido justamente para mostrar que trocar a View (formulário ↔ `JOptionPane`) não exige tocar em uma linha de socket.

## Regra do e-mail

`primeiro_nome.ultimo_sobrenome.ano_nascimento@ufn.edu.br`, minúsculo e sem acentos.

| Nome | Nascimento | E-mail |
|---|---|---|
| Ana Maria de Souza | 12/03/1998 | ana.souza.1998@ufn.edu.br |
| João Carlos Pereira | 12/03/1999 | joao.pereira.1999@ufn.edu.br |
| Felipe Gonçalves Rocha | 12/03/2004 | felipe.rocha.2004@ufn.edu.br |

---

## As três perguntas do enunciado

### 1. O que a classe `Pessoa` precisa para trafegar na rede?

Implementar **`java.io.Serializable`** — interface marcadora, sem métodos; sem ela, `writeObject()` lança `NotSerializableException`. Além disso:

- **`private static final long serialVersionUID = 1L;`** fixa a versão da classe. Sem ele a JVM calcula um hash da estrutura e qualquer alteração futura quebra a desserialização (`InvalidClassException`).
- Todos os atributos precisam ser serializáveis (`String` e `LocalDate` são); o que não for, marca-se `transient`.
- A classe deve existir dos dois lados com **mesmo nome e mesmo pacote** — por isso `Pessoa` fica no `model`, compartilhado por cliente e servidor.
- `equals()`/`hashCode()` por nome + data: é o que faz o `indexOf()` do Model detectar a duplicata.

*(Arquivos: `model/Pessoa.java`, `model/Resposta.java`)*

### 2. Como impedir que a GUI congele?

`connect()`, `accept()`, `writeObject()` e `readObject()` são **bloqueantes**, e o Swing é single-threaded: tudo acontece na *Event Dispatch Thread* (EDT), que também repinta a janela.

- **Servidor** — `ModeloServidor.iniciar()` sobe o laço `accept()` em uma **thread própria** e entrega cada cliente aceito a um `ExecutorService` (`AtendimentoCliente`, uma thread por cliente). O `ServidorController` recebe as notificações vindas dessas threads e sempre envolve a atualização da View em **`SwingUtilities.invokeLater()`**, porque componentes Swing só podem ser tocados pela EDT.
- **Cliente** — o `ClienteController` usa **`SwingWorker`**: `doInBackground()` faz a conversa pelo socket em thread de fundo e `done()` retorna à EDT para preencher os três campos bloqueados. O botão fica desabilitado durante o envio e há `setSoTimeout()` para não esperar indefinidamente.

*(Arquivos: `model/ModeloServidor.java`, `controller/ServidorController.java`, `controller/ClienteController.java`)*

### 3. Como evitar race conditions na lista do servidor?

`ArrayList` não é thread-safe, mas o ponto crítico não é o `add()` isolado: é a operação composta **"verificar se existe **e então** inserir"** (*check-then-act*). Mesmo com `Collections.synchronizedList`, duas threads poderiam passar juntas pelo `indexOf()` e inserir a mesma pessoa duas vezes.

Por isso `ModeloServidor.cadastrarOuLocalizar()` é inteiramente **`synchronized`**: consulta + geração do e-mail + inserção viram uma única operação **atômica** sob o mesmo monitor. `getPessoas()` e `getTotal()` usam o mesmo lock, `getPessoas()` devolve cópia defensiva, a flag `ativo` é **`volatile`** e a lista de observadores é `CopyOnWriteArrayList` (percorrida por várias threads).

Alternativas equivalentes: `ReentrantLock`, `ConcurrentHashMap.putIfAbsent()`, ou `CopyOnWriteArrayList` com bloco `synchronized` em volta da regra composta.

*(Arquivo: `model/ModeloServidor.java`)*

## Validação executada

`teste/TesteMVC.java` — 40 clientes simultâneos (10 pessoas × 4 envios idênticos, liberados juntos por `CountDownLatch`):

```
Respostas recebidas: 40 (esperado 40)
Erros: 0
Pessoas na lista do servidor: 10 (esperado 10)
TESTE OK: sem duplicatas e sem race condition.
```

## Detalhe que costuma quebrar o trabalho

O `ObjectOutputStream` precisa ser criado **antes** do `ObjectInputStream` nos dois lados (com `flush()` logo em seguida). O construtor do `ObjectInputStream` bloqueia esperando o cabeçalho de serialização do outro lado — invertendo a ordem nos dois lados, cliente e servidor travam esperando um ao outro (deadlock silencioso, que parece "servidor não responde").
