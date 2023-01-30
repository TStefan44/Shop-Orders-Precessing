import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    static String orders_path;
    static String products_path;
    static int maxThreads;
    static BufferedReader orderBR;

    public static void main(String[] args) throws FileNotFoundException {
        // Verify arguments number
        if (args.length != 2) {
            System.err.println("Number of arguments wrong!");
            return;
        }

        // Extract files path
        orders_path = args[0] + "/orders.txt";
        products_path = args[0] + "/order_products.txt";

        // Extract max number threads
        try {
            maxThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input " + e.getMessage());
            return;
        }

        // Create buffered reader
        FileReader orderFR = new FileReader(orders_path);
        orderBR = new BufferedReader(orderFR);

        AtomicInteger inQueue = new AtomicInteger(0);
        ExecutorService tpe = Executors.newFixedThreadPool(maxThreads);

        for (int i = 0; i < maxThreads; i++) {
            inQueue.incrementAndGet();
            tpe.submit(new Level1(tpe, inQueue));
        }
    }
}
