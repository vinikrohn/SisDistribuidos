"""
Exercicio 1 - Divisao e Conquista: Soma de Sublistas (padrao MVC)

Divide uma lista de 10.000 numeros aleatorios em 4 partes iguais.
Cada thread soma sua propria sublista (isolamento total, sem lock).
A thread principal aguarda todas e soma os resultados parciais.
"""

import threading
import random


# ==================== MODEL ====================
class SomaModel:
    """Responsavel pelos dados e pela logica de negocio (soma paralela)."""

    def __init__(self, tamanho_lista=10000, num_threads=4):
        self.lista = [random.randint(1, 100) for _ in range(tamanho_lista)]
        self.num_threads = num_threads
        self.resultados = [0] * num_threads

    def _somar_sublista(self, sublista, indice):
        self.resultados[indice] = sum(sublista)

    def calcular_soma_paralela(self):
        tamanho_parte = len(self.lista) // self.num_threads
        threads = []

        for i in range(self.num_threads):
            inicio = i * tamanho_parte
            fim = len(self.lista) if i == self.num_threads - 1 else (i + 1) * tamanho_parte
            sublista = self.lista[inicio:fim]

            t = threading.Thread(target=self._somar_sublista, args=(sublista, i))
            threads.append(t)
            t.start()

        for t in threads:
            t.join()

        return {"parciais": self.resultados, "total": sum(self.resultados)}


# ==================== VIEW ====================
class SomaView:
    """Responsavel apenas por exibir os dados."""

    def exibir_resultado(self, dados):
        for i, parcial in enumerate(dados["parciais"], start=1):
            print(f"Soma parcial {i}: {parcial}")
        print(f"Soma total: {dados['total']}")


# ==================== CONTROLLER ====================
class SomaController:
    """Faz a ponte entre Model e View."""

    def __init__(self):
        self.model = SomaModel()
        self.view = SomaView()

    def executar(self):
        dados = self.model.calcular_soma_paralela()
        self.view.exibir_resultado(dados)


if __name__ == "__main__":
    SomaController().executar()
