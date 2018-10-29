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
        int count = 100000;
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                DLock.getInstance().lock("adder1");
                i++;
                DLock.getInstance().unLock();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count, i);
    }
}
