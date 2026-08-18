/**
 * Exercicio 1 - Divisao e Conquista: Soma de Sublistas (padrao MVC)
 *
 * Divide uma lista de 10.000 numeros aleatorios em 4 partes iguais.
 * Cada thread soma sua propria sublista (isolamento total, sem lock).
 * A thread principal aguarda todas e soma os resultados parciais.
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

// ==================== MODEL ====================
class SomaModel {
    private List<Integer> lista;
    private int numThreads;
    private long[] resultados;

    public SomaModel(int tamanhoLista, int numThreads) {
        this.numThreads = numThreads;
        this.resultados = new long[numThreads];
        this.lista = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < tamanhoLista; i++) {
            lista.add(random.nextInt(100) + 1);
        }
    }

    private void somarSublista(List<Integer> sublista, int indice) {
        long soma = 0;
        for (int valor : sublista) {
            soma += valor;
        }
        resultados[indice] = soma;
    }

    public long[] calcularSomaParalela() throws InterruptedException {
        int tamanhoParte = lista.size() / numThreads;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            int inicio = i * tamanhoParte;
            int fim = (i == numThreads - 1) ? lista.size() : (i + 1) * tamanhoParte;
            List<Integer> sublista = lista.subList(inicio, fim);
            final int indice = i;

            threads[i] = new Thread(() -> somarSublista(sublista, indice));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        return resultados;
    }

    public long getSomaTotal() {
        long total = 0;
        for (long parcial : resultados) total += parcial;
        return total;
    }
}

// ==================== VIEW ====================
class SomaView {
    public void exibirResultado(long[] parciais, long total) {
        for (int i = 0; i < parciais.length; i++) {
            System.out.println("Soma parcial " + (i + 1) + ": " + parciais[i]);
        }
        System.out.println("Soma total: " + total);
    }
}

// ==================== CONTROLLER ====================
class SomaController {
    private SomaModel model;
    private SomaView view;

    public SomaController() {
        this.model = new SomaModel(10000, 4);
        this.view = new SomaView();
    }

    public void executar() throws InterruptedException {
        long[] parciais = model.calcularSomaParalela();
        long total = model.getSomaTotal();
        view.exibirResultado(parciais, total);
    }
}

// ==================== MAIN ====================
public class SomaApp {
    public static void main(String[] args) throws InterruptedException {
        new SomaController().executar();
    }
}
