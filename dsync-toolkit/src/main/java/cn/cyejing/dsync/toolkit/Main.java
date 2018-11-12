package cn.cyejing.dsync.toolkit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-17 16:50
 **/
public class Main {


    public static void main(String[] args) throws Exception {
        if (args.length > 1) {
            System.out.println(args[0]);
            DLock.setHost(args[0]);
        }
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
        scheduledExecutorService.scheduleAtFixedRate(new Worker(), 3, 20, TimeUnit.SECONDS);

    }

    @Slf4j
    public static class Worker implements Runnable {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        @Override
        public void run() {
            int count = 50000;
            final int[] i = {0};
            log.info("begin count:{}",count);

            CountDownLatch latch = new CountDownLatch(count);
            long start = System.currentTimeMillis();
            for (int j = 0; j < count; j++) {
                executorService.submit(() -> {
                    try {
                        DLock.getInstance().lock("adder");
                        i[0]++;
                        DLock.getInstance().unlock();
                    } catch (Exception e) {
                        System.out.println("error");
                        e.printStackTrace();
                    }finally {
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i[0] == count) {
                log.info("worker cost:{}ms. result:{}", (System.currentTimeMillis() - start), i[0]);
            } else {
                log.error("ERROR worker cost:{}ms. result:{}", (System.currentTimeMillis() - start), i[0]);
            }
        }
    }

}
