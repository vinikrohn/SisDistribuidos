/**
 * Exercicio 2 - Filtro de Dados Independente / Map (padrao MVC)
 *
 * Divide uma lista de 5.000 nomes de usuarios em 2 blocos.
 * Cada thread limpa sua propria sublista (trim + upper), isolada da outra.
 * A thread principal aguarda ambas e junta as listas resultantes.
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

// ==================== MODEL ====================
class LimpezaModel {
    private List<String> nomes;
    private List<String> resultadoA = Collections.synchronizedList(new ArrayList<>());
    private List<String> resultadoB = Collections.synchronizedList(new ArrayList<>());

    public LimpezaModel(List<String> nomes) {
        this.nomes = nomes;
    }

    private void limpar(List<String> sublista, List<String> destino) {
        for (String nome : sublista) {
            destino.add(nome.trim().toUpperCase());
        }
    }

    public List<String> processarParalelo() throws InterruptedException {
        int meio = nomes.size() / 2;
        List<String> blocoA = nomes.subList(0, meio);
        List<String> blocoB = nomes.subList(meio, nomes.size());

        Thread threadA = new Thread(() -> limpar(blocoA, resultadoA));
        Thread threadB = new Thread(() -> limpar(blocoB, resultadoB));

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        List<String> listaFinal = new ArrayList<>(resultadoA);
        listaFinal.addAll(resultadoB);
        return listaFinal;
    }
}

// ==================== VIEW ====================
class LimpezaView {
    public void exibirResultado(List<String> listaFinal) {
        System.out.println("Total de nomes processados: " + listaFinal.size());
        System.out.println("Primeiros 10 resultados:");
        for (int i = 0; i < Math.min(10, listaFinal.size()); i++) {
            System.out.println(" - " + listaFinal.get(i));
        }
    }
}

// ==================== CONTROLLER ====================
class LimpezaController {
    private LimpezaModel model;
    private LimpezaView view;

    public LimpezaController(List<String> nomes) {
        this.model = new LimpezaModel(nomes);
        this.view = new LimpezaView();
    }

    public void executar() throws InterruptedException {
        List<String> listaFinal = model.processarParalelo();
        view.exibirResultado(listaFinal);
    }
}

// ==================== MAIN ====================
public class LimpezaApp {
    public static void main(String[] args) throws InterruptedException {
        List<String> nomesExemplo = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            nomesExemplo.add("  usuario_" + i + "  ");
        }
        new LimpezaController(nomesExemplo).executar();
    }
}
