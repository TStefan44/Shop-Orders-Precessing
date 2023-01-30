import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Level2 implements Runnable{
    private final String id_command;
    private final AtomicInteger number_products;
    private final AtomicInteger inQueue;
    private final BufferedReader productBr;
    private static final FileWriter producer_out;

    static {
        try {
            producer_out = new FileWriter("order_products_out.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Level2(String id_command, AtomicInteger number_products, BufferedReader productBr,
                  AtomicInteger inQueue) {
        this.id_command = id_command;
        this.number_products = number_products;
        this.productBr = productBr;
        this.inQueue = inQueue;
    }


    @Override
    public void run() {
        while (true) {
            // Try to read a line from file
            String line = null;
            try {
                synchronized (productBr) {
                    line = productBr.readLine();
                }
                if (line == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Check is current line has a product from given command
            String[] arg = line.split(",", 2);
            if (arg[0].compareTo(id_command) == 0) {
                number_products.decrementAndGet();
                synchronized (producer_out) {
                    try {
                        producer_out.write(arg[0] + "," + arg[1] + ",shipped\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
        }

        // Flush order_products.txt
        int left = inQueue.decrementAndGet();
        if (left == 0) {
            try {
                producer_out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
