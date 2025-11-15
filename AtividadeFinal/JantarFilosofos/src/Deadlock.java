import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Deadlock {
    private static final Object lockA = new Object();
    private static final Object lockB = new Object();
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                log("Thread 1 adquire lock A");

                dormir(100);
                log("Thread 1 tenta adquirir lock B");

                synchronized (lockB) {
                    log("T1: obteve A e em seguida B");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                log("Thread 2 adquire lock B");

                dormir(100);
                log("Thread 2 tenta adquirir lock A");

                synchronized (lockA) {
                    log("T2: obteve B e em seguida A");
                }
            }
        });

        t1.start();
        t2.start();
    }

    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static void log(String msg) {
        String time = LocalTime.now().format(fmt);
        System.out.println("[" + time + "] " + msg);
    }
}
