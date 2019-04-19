package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.test.integration.util.MyThreadFactory;
import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-28 22:13
 **/
@Slf4j
public class Lock2Test extends LockServerInit{

    private int i = 0;

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    ExecutorService executorService2 = Executors.newFixedThreadPool(10, new MyThreadFactory("shoutdown"));
    ExecutorService executorService3 = Executors.newFixedThreadPool(10, new MyThreadFactory("shoutdown2"));


    @Test
    public void testLock() throws Exception {
        DLock lock = DSync.create(new Config().host("localhost").port(4843)).getLock();

        DLock lock2 = DSync.create(new Config().host("localhost").port(4843)).getLock();

        /**
         * 5c 1000n 3462ms
         * 10c 1000n 3536ms
         * 20c 1000n 3918ms
         * 10c 5000n 15413ms
         * 10c 10000n 29734ms
         */

        int count = 5000;
        int count2 = 3000;
        int count3 = 1000;
        CountDownLatch latch1 = new CountDownLatch(count);
        CountDownLatch latch2 = new CountDownLatch(count2);
        CountDownLatch latch3 = new CountDownLatch(count3);


        long start = System.currentTimeMillis();
        log.info("begin 1:{}, 2:{}, 3,{}",latch1.getCount(),latch2.getCount(),latch3.getCount());
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                lock.lock("adder");
                i++;

                latch1.countDown();
                lock.unlock();
                if (latch1.getCount() == 100) { //开启第2个
                    log.info("begin2 1:{}, 2:{}, 3,{}",latch1.getCount(),latch2.getCount(),latch3.getCount());
                    for (int m = 0; m < count2; m++) {
                        executorService2.submit(() -> {
                            lock2.lock("adder");
                            i++;

                            latch2.countDown();
                            lock2.unlock();
                            if (latch2.getCount() == 100) { //开启第3个
                                log.info("begin3 1:{}, 2:{}, 3,{}",latch1.getCount(),latch2.getCount(),latch3.getCount());
                                DLock lock3 = DSync.create(new Config().host("localhost").port(4843)).getLock();
                                for (int k = 0; k < count3; k++) {
                                    executorService3.submit(() -> {
                                        lock3.lock("adder");
                                        i++;

                                        latch3.countDown();
                                        lock3.unlock();
                                        if (latch3.getCount() == 0) {
                                            log.info("shutdown3 1:{}, 2:{}, 3,{}",latch1.getCount(),latch2.getCount(),latch3.getCount());

                                            lock3.shutdown();
                                        }
                                    });
                                }
                            }
                            if (latch2.getCount() == 0) {
                                log.info("shutdown2 1:{}, 2:{}, 3,{}",latch1.getCount(),latch2.getCount(),latch3.getCount());
                                lock2.shutdown();
                            }
                        });
                    }
                }
            });
        }



        latch1.await();
        latch2.await();
        latch3.await();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        executorService2.shutdown();
        executorService2.awaitTermination(1, TimeUnit.DAYS);
        executorService3.shutdown();
        executorService3.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count+count2+count3, i);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }
}
