package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
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
public class LockManualTest {

    private int i = 0;

    private final static ExecutorService executorService = Executors.newFixedThreadPool(10);

//    @Test
    public void test1() throws Exception {
        testLock();
    }
//    @Test
    public void test2() throws Exception {
        testLock();
    }
//    @Test
    public void test3() throws Exception {
        testLock();
    }

    public void testLock() throws Exception {
        DLock lock = DSync.create(new Config().host("localhost").port(4843)).getLock();
        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */
        int count = 5000;
        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                try {
                    lock.lock("adder");
                    int temp = i;
                    Thread.sleep(10);
                    i = temp + 1;
                    lock.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count, i);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }


}
