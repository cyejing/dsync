package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-28 22:13
 **/
public class TryLockTest extends LockServerInit {

    private int i = 0;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Test
    public void testLock() throws Exception {
        startServer(4848);
        DSync dSync = DSync.create(new Config().host("localhost").port(4848));
        DLock lock = dSync.getLock();
        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */
        int count = 300;
        long start = System.currentTimeMillis();
        AtomicInteger ai = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(count);

        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                try {
                    boolean adder = lock.tryLock("adder");
                    if (adder) {
                        int temp = i;
                        Thread.sleep(10);
                        i = temp + 1;
                        ai.incrementAndGet();
                    }
                    latch.countDown();
                    Thread.sleep(new Random().nextInt(3)+1*100);

                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    lock.unlock();

                }

            });
        }
//        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(ai.get(), i);
        System.out.println("i:"+i+" cost:" + (System.currentTimeMillis() - start) + "ms");
    }

    private volatile int ii = 0;
    @Test
    public void testStep() throws InterruptedException {
        startServer(4849);
        DSync dSync = DSync.create(new Config().host("localhost").port(4849));
        DLock lock = dSync.getLock();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(2);
        new Thread(() -> {
            try {
                latch1.await();
                boolean lock1 = lock.tryLock("lock");
                Assert.assertTrue(lock1);
                System.out.println("lock1");
                int temp = ii;
                latch.await();

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
                latch2.await();
                boolean lock1 = lock.tryLock("lock");
                Assert.assertFalse(lock1);
                if (!lock1) {
                    System.out.println("lock failed");
                }
                latch3.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                lock.unlock();

            }
        }).start();

        latch1.countDown();
        Thread.sleep(1000);
        latch2.countDown();
        Thread.sleep(1000);
        latch.countDown();

        latch3.await();
        Assert.assertEquals(ii, 1);
    }

}
