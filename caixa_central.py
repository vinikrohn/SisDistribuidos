"""
Modulo 1 - Exercicio 1: Sistema de Caixa Centralizado de Evento

5 caixas (threads) vendem fichas simultaneamente, todos atualizando
o mesmo saldo bancario centralizado do evento.

Cada caixa vende 1.000 fichas de R$ 10,00 -> saldo final esperado: R$ 50.000,00

Avalia: uso de threading.Lock para garantir exclusao mutua sobre o
recurso compartilhado (saldo_central), evitando condicao de corrida.
"""

import threading


class CaixaCentral:
    def __init__(self):
        self.saldo_central = 0.0
        self.lock = threading.Lock()

    def vender_fichas(self, quantidade, preco_ficha):
        """Cada thread (caixa) chama este metodo para registrar suas vendas."""
        for _ in range(quantidade):
            with self.lock:  # exclusao mutua: so uma thread por vez altera o saldo
                self.saldo_central += preco_ficha


def caixa_worker(caixa, id_caixa):
    print(f"[Caixa {id_caixa}] iniciando vendas...")
    caixa.vender_fichas(quantidade=1000, preco_ficha=10.00)
    print(f"[Caixa {id_caixa}] finalizou vendas.")


if __name__ == "__main__":
    caixa = CaixaCentral()
    threads = []

    # Instancia as 5 threads (caixas)
    for i in range(1, 6):
        t = threading.Thread(target=caixa_worker, args=(caixa, i))
        threads.append(t)
        t.start()

    # Aguarda todas terminarem
    for t in threads:
        t.join()

    print(f"\nSaldo final do evento: R$ {caixa.saldo_central:,.2f}")
    assert caixa.saldo_central == 50000.00, "Saldo incorreto! Houve condicao de corrida."
    print("Saldo confere com o esperado (R$ 50.000,00)")
