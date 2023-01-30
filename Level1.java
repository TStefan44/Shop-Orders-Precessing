import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Level1 implements Runnable {
    private final ExecutorService tpe;
    private final AtomicInteger inQueue;
    private static ExecutorService tpe_level2 = Executors.newFixedThreadPool(Tema2.maxThreads);
    private static final AtomicInteger inQueue_level2 = new AtomicInteger(0);
    private static final FileWriter order_out;

    static {
        try {
            order_out = new FileWriter("orders_out.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Level1(ExecutorService tpe, AtomicInteger inQueue) {
        this.tpe = tpe;
        this.inQueue = inQueue;
    }

    @Override
    public void run() {
        // Parse through order.txt
        while (true) {
            // Try to read a line from file
            String line;
            try {
                synchronized (Tema2.orderBR) {
                    line = Tema2.orderBR.readLine();
                }
                if (line == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Extract command id and produtcs number
            String[] arg = line.split(",", 2);
            String command_id = arg[0];
            AtomicInteger number_products = new AtomicInteger(Integer.parseInt(arg[1]));

            // Skip case when command has 0 products
            if (number_products.get() == 0) {
                continue;
            }

            // Ceate a fille reader for current command
            FileReader productFR;
            try {
                productFR = new FileReader(Tema2.products_path);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            BufferedReader productBR = new BufferedReader(productFR);

            // Add product number tasks in level 2 poll
            int num = Integer.parseInt(arg[1]);
            for (int i = 0; i < num; i++) {
                inQueue_level2.incrementAndGet();
                tpe_level2.submit(new Level2(command_id, number_products, productBR, inQueue_level2));
            }

            // Wait for all products to be shipped
            while (true) {
                if (number_products.get() == 0)
                    break;
            }

            // All products are shipped, write in order_out.txt
            synchronized (order_out) {
                try {
                        order_out.write(command_id + "," + num  + ",shipped\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        // Close level 1 poll
        int left = inQueue.decrementAndGet();
        if (left == 0) {

            // Wait for the level 2 poll to finish
            while (true) {
                if (inQueue_level2.get() == 0)
                    break;
            }

            // Close level 1 and level 2 pool
            tpe.shutdown();
            tpe_level2.shutdown();

            // Flush order_out fille writer
            try {
                order_out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
