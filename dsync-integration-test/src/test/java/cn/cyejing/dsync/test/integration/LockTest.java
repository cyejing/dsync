package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
import cn.cyejing.dsync.toolkit.exception.LockTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-28 22:13
 **/
public class LockTest extends LockServerInit {

    private int i = 0;

    @Test
    public void testLock() throws Exception {
        startServer(4885);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        DLock lock = DSync.create(new Config().host("localhost").port(4885)).getLock();
        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */
        int count = 200;
        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                try {
                    lock.lock("adder");
                    int temp = i;
                    Thread.sleep(10);
                    i = temp + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count, i);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }


    private volatile int ii = 0;

    @Test
    public void testStep() throws InterruptedException {
        startServer(4886);

        DLock lock = DSync.create(new Config().host("localhost").port(4886)).getLock();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(2);
        CountDownLatch latch3 = new CountDownLatch(2);
        new Thread(() -> {
            try {
                latch2.countDown();
                lock.lock("lock");
                System.out.println("lock1");
                latch.await();
                int temp = ii;
                latch1.await();
                ii = temp + 1;
                System.out.println("lock1,await" + temp);

                latch3.countDown();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }).start();
        new Thread(() -> {
            try {
                latch2.countDown();
                lock.lock("lock");
                System.out.println("lock2");
                latch.await();
                int temp = ii;
                latch1.await();
                ii = temp + 1;
                System.out.println("lock2,await" + temp);

                latch3.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }).start();

        latch2.await();
        latch.countDown();
        Thread.sleep(1000);
        latch1.countDown();
        latch3.await();
        Assert.assertEquals(ii, 2);
    }
}
