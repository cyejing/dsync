package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
import cn.cyejing.dsync.toolkit.exception.LockTimeoutException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-28 22:13
 **/
public class LockTImeoutTest extends LockServerInit {

    private int i = 0;

    private volatile int ii = 0;

    @Test
    public void testLockTimeout() throws Exception {
        startServer(4847);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ExecutorService executorService2 = Executors.newFixedThreadPool(10);
        DLock lock = DSync.create(new Config().host("localhost").port(4847)).getLock();
        DLock lock2 = DSync.create(new Config().host("localhost").port(4847)).getLock();
        AtomicInteger ai = new AtomicInteger(0);
        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */
        int count = 100;
        CountDownLatch latch = new CountDownLatch(count);
        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                try {
                    lock.lock("adderT", Duration.ofSeconds(1));
                    int temp = i;
                    Thread.sleep(110);
                    i = temp + 1;
                } catch (LockTimeoutException e) {
                    ai.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                    lock.unlock();
                }
            });
        }
        latch.await();
        for (int j = 0; j < count; j++) {
            executorService2.submit(() -> {
                try {
                    lock2.lock("adderT",Duration.ofSeconds(1));
                    int temp = ii;
                    Thread.sleep(10);
                    ii = temp + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock2.unlock();
                }
            });
        }
        executorService.shutdown();
        executorService2.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        executorService2.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count - ai.get(), i);
        Assert.assertEquals(count, ii);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }


}
