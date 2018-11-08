package cn.cyejing.dsync.toolkit;

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

    ExecutorService executorService = Executors.newFixedThreadPool(5);


    @Test
    public void testLock() throws Exception {
        /**
         * 5c 1000n 3606ms
         * 5c 1000n 3531ms
         * 5c 1000n 4270ms
         * 5c 1000n 3887ms
         */
        DLock.setHost("192.168.9.107");
        int count = 1000;
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                DLock.getInstance().lock("adder");
                i++;
                DLock.getInstance().unLock();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count, i);
    }
}
