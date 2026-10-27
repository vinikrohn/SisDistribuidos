# Desafio da Telefonia — Cliente-Servidor com Java RMI

Refatoração do sistema local (PESQUISA E ORDENAÇÃO / Telefonia) para arquitetura
cliente-servidor. **Abordagem escolhida: Java RMI** (nativo do JDK, sem bibliotecas externas).

## Como funciona

```
 CLIENTE                                   SERVIDOR
 ───────                                   ────────
 Cliente / JFrameCliente                   Servidor (main)
   │ 1. lookup("ServicoSessoes") ───────►    ├─ abre telephony_sessions.txt
   │                                         ├─ carrega tudo no HashMap (TabelaHash)
   │ 2. consultarSessao("55...") ───────►    └─ ServicoSessoesImpl
   │ ◄────────── String com a sessão (ou null) ──      sessoes.get(numero)  → O(1)
```

## Estrutura

| Pacote / classe | Papel |
|---|---|
| `model.Sessao` | **Idêntica à do professor** (não é alterada) |
| `model.TabelaHash` | Igual à original (`HashMap<String, Sessao>`) |
| `utilidades.Util` | Mesma rotina de carga do arquivo para o HashMap |
| `rmi.ServicoSessoes` | **Interface remota** (contrato comum a cliente e servidor). Trafega só `String`, por isso a `Sessao` não precisa ser `Serializable` |
| `servidor.ServicoSessoesImpl` | Implementação remota: guarda o HashMap e responde às consultas |
| `servidor.Servidor` | Abre a base, popula o HashMap e publica o serviço no RMI Registry |
| `cliente.Cliente` | Cliente de console |
| `cliente.JFrameCliente` | Cliente gráfico (Swing), no estilo da tela original |

## Compilar

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
```
(No Windows: `javac -encoding UTF-8 -d bin src\model\*.java src\utilidades\*.java src\rmi\*.java src\servidor\*.java src\cliente\*.java`)

## Executar

**1. Servidor** (deixe rodando num terminal):
```bash
java -cp bin servidor.Servidor telephony_sessions.txt 1099
```

**2. Cliente** (em outro terminal ou outra máquina):
```bash
java -cp bin cliente.Cliente localhost 1099        # console
java -cp bin cliente.JFrameCliente                 # interface gráfica
```

Números para teste: `5592914942603`, `5569971971316`, `5551928538251`.

## Rodando em máquinas diferentes

- No cliente, troque `localhost` pelo IP do servidor.
- Se o servidor tiver mais de uma placa de rede, inicie-o informando o IP público dele:
  `java -Djava.rmi.server.hostname=192.168.0.10 -cp bin servidor.Servidor telephony_sessions.txt`
- Libere a porta 1099 no firewall do servidor.

## NetBeans

Crie um projeto "Java with Existing Sources", aponte para a pasta `src` e coloque o
`telephony_sessions.txt` na raiz do projeto. Rode primeiro `servidor.Servidor`, depois
`cliente.JFrameCliente` (ou `cliente.Cliente`).
