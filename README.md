# Exercícios de Fixação — Threads

Repositório com os exercícios práticos de Threads, implementados em **Python** e **Java**, organizados por pasta.

## Estrutura

```
exercicios-threads/
├── 01-soma-sublistas-mvc/          # Divisao e conquista: soma paralela (sem compartilhamento)
│   ├── python/soma_mvc.py
│   └── java/SomaApp.java
│
├── 02-filtro-dados-mvc/            # Map paralelo: limpeza de strings (sem compartilhamento)
│   ├── python/limpeza_mvc.py
│   └── java/LimpezaApp.java
│
├── 03-caixa-central-lock/          # Modulo 1: com compartilhamento de memoria (Lock)
│   ├── python/caixa_central.py
│   └── java/EventoApp.java
│
└── 04-relatorio-filiais-fork-join/ # Modulo 2: sem compartilhamento de memoria (Fork-Join)
    ├── python/relatorio_filiais.py
    └── java/RelatorioApp.java
```

## Como rodar

### Python
```bash
python3 <caminho-do-arquivo>.py
```
Nenhuma dependência externa é necessária (usa apenas `threading`, `random` e `concurrent.futures` da biblioteca padrão).

### Java
```bash
javac <caminho-do-arquivo>.java
java <NomeDaClassePublica>
```

## Resumo dos exercícios

| Pasta | Exercício | Conceito principal |
|---|---|---|
| `01-soma-sublistas-mvc` | Soma de 10.000 números divididos em 4 threads | Isolamento de dados, sem necessidade de Lock |
| `02-filtro-dados-mvc` | Limpeza de 5.000 strings divididas em 2 threads | Isolamento de dados, sem necessidade de Lock |
| `03-caixa-central-lock` | 5 caixas somando em um saldo centralizado | **Exclusão mútua** com `Lock`/`synchronized` |
| `04-relatorio-filiais-fork-join` | 4 filiais somando faturamento local | **Fork-Join**, sem variáveis globais |

Os dois primeiros exercícios seguem o padrão **MVC** (Model-View-Controller), separando geração de dados, processamento e exibição em camadas distintas. Os dois últimos (`03` e `04`) correspondem aos **Trabalhos Avaliativos** dos Módulos 1 e 2, focando respectivamente em:

- **Módulo 1** — sincronização e exclusão mútua sobre recurso compartilhado
- **Módulo 2** — isolamento de escopo e junção de resultados (fork-join)
