"""
Exercicio 2 - Filtro de Dados Independente / Map (padrao MVC)

Divide uma lista de 5.000 nomes de usuarios em 2 blocos.
Cada thread limpa sua propria sublista (strip + upper), isolada da outra.
A thread principal aguarda ambas e junta as listas resultantes.
"""

import threading


# ==================== MODEL ====================
class LimpezaModel:
    """Responsavel pelos dados e pela logica de negocio (limpeza paralela)."""

    def __init__(self, nomes):
        self.nomes = nomes
        self.resultado_a = []
        self.resultado_b = []

    def _limpar(self, sublista, destino):
        for nome in sublista:
            destino.append(nome.strip().upper())

    def processar_paralelo(self):
        meio = len(self.nomes) // 2
        bloco_a = self.nomes[:meio]
        bloco_b = self.nomes[meio:]

        thread_a = threading.Thread(target=self._limpar, args=(bloco_a, self.resultado_a))
        thread_b = threading.Thread(target=self._limpar, args=(bloco_b, self.resultado_b))

        thread_a.start()
        thread_b.start()

        thread_a.join()
        thread_b.join()

        return self.resultado_a + self.resultado_b


# ==================== VIEW ====================
class LimpezaView:
    """Responsavel apenas por exibir os dados."""

    def exibir_resultado(self, lista_final):
        print(f"Total de nomes processados: {len(lista_final)}")
        print("Primeiros 10 resultados:")
        for nome in lista_final[:10]:
            print(f" - {nome}")


# ==================== CONTROLLER ====================
class LimpezaController:
    """Faz a ponte entre Model e View."""

    def __init__(self, nomes):
        self.model = LimpezaModel(nomes)
        self.view = LimpezaView()

    def executar(self):
        lista_final = self.model.processar_paralelo()
        self.view.exibir_resultado(lista_final)


if __name__ == "__main__":
    # Simula 5.000 nomes com espacos e caixa mista
    nomes_exemplo = [f"  usuario_{i}  " for i in range(5000)]
    LimpezaController(nomes_exemplo).executar()
