import java.util.concurrent.atomic.AtomicInteger;

public class Main{
    private static final int TOTAL_JOBS = 12000;
    private static final int PRODUCERS = 2;
    private static final int CONSUMERS = 6;

    public static void main(String[] args) throws InterruptedException{
        if (args.length > 0) {
            System.out.println("=== Bounded Bulk Test ===");
            runBulkTest(new BoundedQueue<>(100));

            System.out.println("=== Lock-Free Bulk Test ===");
            runBulkTest(new LockFreeQueue<>());

            System.out.println("=== Bounded Bulk Test ===");
            runBulkTest(new BoundedQueue<>(100));

            System.out.println("=== Lock-Free Bulk Test ===");
            runBulkTest(new LockFreeQueue<>());
        } else {
            System.out.println("=== Testing Bounded Queue ===");
            runTest(new BoundedQueue<>(100));

            System.out.println("\n=== Testing Lock-Free Queue ===");
            runTest(new LockFreeQueue<>());
        }

        System.out.println("\n");
    }

    // private static <Q> void runTest(Object queue) throws InterruptedException{
    private static void runTest(ConcurrentQueue<String> queue) throws InterruptedException{
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);

        Thread[] producers = new Thread[PRODUCERS];
        Thread[] consumers = new Thread[CONSUMERS];

        int jobsPerProducer = TOTAL_JOBS / PRODUCERS;

        long startTime = System.currentTimeMillis();

        //Start Producers
        // System.out.println("\nStarting Producers...");
        for(int i = 0; i < PRODUCERS; i++){
            final int producerId = i;
            producers[i] = new Thread(() -> {
                for(int j = 0; j < jobsPerProducer; j++){
                    String job = "Job-" + producerId + "-" + j;
                    queue.enq(job);

                    // System.out.println("Producer " + producerId + ": Produced: " + job);
                    producedCount.incrementAndGet();
                }
            });
        }        

        //Start Consumers
        // System.out.println("\nStarting Consumers...");
        for(int i = 0; i < CONSUMERS; i++){
            consumers[i] = new Thread(() -> {
                while(consumedCount.get() < TOTAL_JOBS){
                    String job = queue.deq();

                    if(job != null){
                        // System.out.println("Consumer " + Thread.currentThread().getName() + ": Consumed: " + job);
                        consumedCount.incrementAndGet();
                    }
                    else{
                        //Brief yield if lock-free queue returns null temporarily
                        // Thread.yield();
                    }
                }
            });
            //consumers[i].start();
        }

        for(Thread p : producers){
            p.start();
        }

        for(Thread c : consumers){
            c.start();
        }

        //Wait for producers to finish
        for(Thread p : producers){
            p.join();
        }
        // System.out.println("\nProducers done\n");


        //Wait for consumers to catch up
        for(Thread c : consumers){
            c.join();
        }
        // System.out.println("\nConsumers done\n");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Produced: " + producedCount.get());
        System.out.println("Consumed: " + consumedCount.get());
        System.out.println("Execution Time: " + duration + " ms");
        System.out.println("Throughput: " + (TOTAL_JOBS / (duration / 1000.0)) + " jobs/sec");

        if(producedCount.get() == consumedCount.get()){
            System.out.println("TEST PASSED SUCCESSFULLY!");
        }
        else{
            System.out.println("TEST FAILED: Mismatch between produced and consumed jobs.");
        }
    }

    private static void runBulkTest(ConcurrentQueue<String> queue) throws InterruptedException{
        long totalDuration = 0;
        int num = 50;

        for (int a = 0; a < num; a++) {
            AtomicInteger producedCount = new AtomicInteger(0);
            AtomicInteger consumedCount = new AtomicInteger(0);

            Thread[] producers = new Thread[PRODUCERS];
            Thread[] consumers = new Thread[CONSUMERS];

            int jobsPerProducer = TOTAL_JOBS / PRODUCERS;

            long startTime = System.currentTimeMillis();

            for(int i = 0; i < PRODUCERS; i++){
                final int producerId = i;
                producers[i] = new Thread(() -> {
                    for(int j = 0; j < jobsPerProducer; j++){
                        String job = "Job-" + producerId + "-" + j;
                        queue.enq(job);

                        producedCount.incrementAndGet();
                    }
                });
            }

            for(int i = 0; i < CONSUMERS; i++){
                consumers[i] = new Thread(() -> {
                    while(consumedCount.get() < TOTAL_JOBS){
                        String job = queue.deq();

                        if(job != null){
                            consumedCount.incrementAndGet();
                        }
                    }
                });
            }

            for(Thread p : producers){
                p.start();
            }

            for(Thread c : consumers){
                c.start();
            }

            for(Thread p : producers){
                p.join();
            }

            for(Thread c : consumers){
                c.join();
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            totalDuration += duration;

            if(producedCount.get() != consumedCount.get()) {
                System.out.println("TEST FAILED: Mismatch between produced and consumed jobs.");
                System.exit(0);
            }
        }

        long avgDuration = totalDuration/num;

        System.out.println("Average Execution Time: " + avgDuration + " ms");
        System.out.println("Average Throughput: " + (TOTAL_JOBS / (avgDuration / 1000.0)) + " jobs/sec");
    }
}
