import java.util.concurrent.Semaphore;

public class JantarFilosofos {

    // --- Constantes e Estados ---
    private static final int N = 5; // Número de filósofos

    // Estados mapeados para inteiros para facilitar a lógica
    private static final int PENSANDO = 0;
    private static final int FAMINTO = 1;
    private static final int COMENDO = 2;

    // --- Recursos Compartilhados ---

    // Array para armazenar o estado de cada filósofo
    private static final int[] estado = new int[N];

    // Semáforo Mutex: protege a região crítica (acesso ao array 'estado')
    // Inicializado com 1 (funciona como um lock binário)
    private static final Semaphore lock = new Semaphore(1);

    // Semáforos dos Filósofos: usados para bloquear quem não pode comer
    // Inicializados com 0 (bloqueados por padrão)
    private static final Semaphore[] semaforoIndividual = new Semaphore[N];

    // --- Inicialização ---
    public static void main(String[] args) {
        // Inicializa os semáforos dos filósofos e as threads
        for (int i = 0; i < N; i++) {
            semaforoIndividual[i] = new Semaphore(0); // Começam em 0 pois ninguém pediu para comer ainda
        }

        // Cria e inicia as threads
        for (int i = 0; i < N; i++) {
            new Thread(new Filosofo(i)).start();
        }
    }

    // --- Lógica do Filósofo (Runnable) ---
    static class Filosofo implements Runnable {
        private final int id;

        public Filosofo(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    pensar();
                    pegarGarfos(id);
                    comer();
                    largarGarfos(id);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void pensar() throws InterruptedException {
            System.out.println("Filósofo " + id + " está PENSANDO.");
            Thread.sleep((long) (Math.random() * 1000));
        }

        private void comer() throws InterruptedException {
            System.out.println("Filósofo " + id + " está COMENDO.");
            Thread.sleep((long) (Math.random() * 1000));
        }
    }

    // --- Métodos de Controle (Monitor) ---

    // Tenta pegar os garfos
    private static void pegarGarfos(int i) throws InterruptedException {
        lock.acquire(); // Entra na região crítica

        estado[i] = FAMINTO;
        System.out.println("Filósofo " + i + " está FAMINTO.");

        testar(i); // Tenta comer

        lock.release(); // Sai da região crítica

        // Se testar(i) não teve sucesso, o semáforo s[i] continua em 0.
        // O filósofo trava aqui. Se teve sucesso, s[i] é 1 e ele passa direto.
        semaforoIndividual[i].acquire();
    }

    // Larga os garfos
    private static void largarGarfos(int i) throws InterruptedException {
        lock.acquire(); // Entra na região crítica

        estado[i] = PENSANDO;
        System.out.println("Filósofo " + i + " largou os garfos.");

        // Verifica se os vizinhos querem comer
        testar(esquerda(i));
        testar(direita(i));

        lock.release(); // Sai da região crítica
    }

    // Função principal de teste
    private static void testar(int i) {
        // Se o filósofo 'i' está faminto E os vizinhos NÃO estão comendo
        if (estado[i] == FAMINTO &&
                estado[esquerda(i)] != COMENDO &&
                estado[direita(i)] != COMENDO) {

            estado[i] = COMENDO;

            // Libera o filósofo para comer.
            // Isso incrementa o semáforo s[i] de 0 para 1.
            // O 'acquire()' que estava travando (ou vai travar) em pegarGarfos será liberado.
            semaforoIndividual[i].release();
        }
    }

    // Funções auxiliares de vizinhança
    private static int esquerda(int i) {
        return (i + N - 1) % N;
    }

    private static int direita(int i) {
        return (i + 1) % N;
    }
}