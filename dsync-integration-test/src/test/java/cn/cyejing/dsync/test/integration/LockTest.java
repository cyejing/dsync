package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
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
public class LockTest {

    private int i = 0;

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Test
    public void testLock() throws Exception {
        DSync dSync = DSync.create(new Config().host("172.31.9.150"));
        DLock lock = dSync.getLock();
        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */
        int count = 1000;
        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                lock.lock("adder");
                i++;
                lock.unlock();
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count, i);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }
}
