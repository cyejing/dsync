package cn.cyejing.dsync.toolkit.redisLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-09 15:12
 **/
public class RedissonLockTest {
    private int i = 0;

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Test
    public void testLock() throws Exception {
        /**
         * 5c 1000n 4812ms
         * 10c 1000n 4898ms
         * 10c 5000n 23445ms
         * 10c 10000n 45998ms
         */
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://172.31.9.150:6379");


        RedissonClient redisson = Redisson.create(config);
        RLock lock = redisson.getLock("anyLock");

        int count = 5000;
        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                lock.lock();
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
