/**
 * Modulo 2 - Exercicio 2: Processamento de Relatorio de Vendas por Filial
 *
 * 4 filiais (threads) calculam seu proprio faturamento a partir de listas
 * locais e isoladas (nenhum acesso a variavel global durante a execucao).
 * A thread principal aguarda todas e soma os resultados finais.
 *
 * Avalia: conceito de Fork-Join e isolamento, usando Callable/Future.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

class FilialTask implements Callable<Double> {
    private List<Double> vendasFilial; // dado local, isolado -- sem variavel global

    public FilialTask(List<Double> vendasFilial) {
        this.vendasFilial = vendasFilial;
    }

    @Override
    public Double call() {
        double soma = 0;
        for (double venda : vendasFilial) {
            soma += venda;
        }
        return soma;
    }
}

public class RelatorioApp {

    private static List<Double> gerarVendasFilial(int qtdRegistros) {
        List<Double> vendas = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < qtdRegistros; i++) {
            vendas.add(10 + random.nextDouble() * 490); // valores entre 10 e 500
        }
        return vendas;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int numFiliais = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numFiliais);
        List<Future<Double>> futures = new ArrayList<>();

        // Fork: dispara 4 tarefas, cada uma com sua lista isolada
        for (int i = 0; i < numFiliais; i++) {
            List<Double> vendasFilial = gerarVendasFilial(10000);
            futures.add(executor.submit(new FilialTask(vendasFilial)));
        }

        // Join: aguarda cada Future e coleta o resultado
        double faturamentoTotal = 0;
        for (int i = 0; i < futures.size(); i++) {
            double resultado = futures.get(i).get(); // bloqueia ate a thread terminar
            System.out.printf("Faturamento Filial %d: R$ %,.2f%n", i + 1, resultado);
            faturamentoTotal += resultado;
        }

        System.out.printf("%nFaturamento total da franquia: R$ %,.2f%n", faturamentoTotal);

        executor.shutdown();
    }
}
