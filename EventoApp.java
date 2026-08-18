/**
 * Modulo 1 - Exercicio 1: Sistema de Caixa Centralizado de Evento
 *
 * 5 caixas (threads) vendem fichas simultaneamente, todos atualizando
 * o mesmo saldo bancario centralizado do evento.
 *
 * Cada caixa vende 1.000 fichas de R$ 10,00 -> saldo final esperado: R$ 50.000,00
 *
 * Avalia: uso de ReentrantLock (ou synchronized) para garantir exclusao
 * mutua sobre o recurso compartilhado (saldoCentral), evitando condicao de corrida.
 */

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CaixaCentral {
    private double saldoCentral = 0.0;
    private final Lock lock = new ReentrantLock();

    public void venderFichas(int quantidade, double precoFicha) {
        for (int i = 0; i < quantidade; i++) {
            lock.lock();
            try {
                saldoCentral += precoFicha;
            } finally {
                lock.unlock(); // sempre libera o lock, mesmo se der excecao
            }
        }
    }

    public double getSaldoCentral() {
        return saldoCentral;
    }
}

class CaixaWorker implements Runnable {
    private CaixaCentral caixa;
    private int idCaixa;

    public CaixaWorker(CaixaCentral caixa, int idCaixa) {
        this.caixa = caixa;
        this.idCaixa = idCaixa;
    }

    @Override
    public void run() {
        System.out.println("[Caixa " + idCaixa + "] iniciando vendas...");
        caixa.venderFichas(1000, 10.00);
        System.out.println("[Caixa " + idCaixa + "] finalizou vendas.");
    }
}

public class EventoApp {
    public static void main(String[] args) throws InterruptedException {
        CaixaCentral caixa = new CaixaCentral();
        Thread[] threads = new Thread[5];

        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(new CaixaWorker(caixa, i + 1));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.printf("%nSaldo final do evento: R$ %,.2f%n", caixa.getSaldoCentral());

        if (caixa.getSaldoCentral() == 50000.00) {
            System.out.println("Saldo confere com o esperado (R$ 50.000,00)");
        } else {
            System.out.println("Saldo incorreto! Houve condicao de corrida.");
        }
    }
}
