package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.dominate.LockServer;
import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-28 22:13
 **/
public class ServerBreakTest {

    private int i = 0;

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    ExecutorService executorService2 = Executors.newFixedThreadPool(10);
    LockServer lockServer = new LockServer();


    public void startServer()  {
        new Thread(() -> {
            try {
                lockServer.start(4843);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Test
    public void testLock() throws Exception {
        startServer();
        DLock lock = DSync.create(new Config().host("localhost").port(4843)).getLock();
        DLock lock1 = DSync.create(new Config().host("localhost").port(4843)).getLock();
        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */
        int count = 200;
        CountDownLatch latch1 = new CountDownLatch(count);

        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                try {
                    lock.lock("adder");
                    i++;
                    latch1.countDown();
                    lock.unlock();
                    if (latch1.getCount() == 20 || latch1.getCount() == 60) {
                        System.out.println("server is shutdown");
                        lockServer.shutdown();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lockServer = new LockServer();
                        startServer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        for (int k = 0; k < count; k++) {
            executorService2.submit(() -> {
                try {
                    lock1.lock("adder");
                    i++;
                    Thread.sleep(100);
                    lock1.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch1.await();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count + count, i);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }
}
