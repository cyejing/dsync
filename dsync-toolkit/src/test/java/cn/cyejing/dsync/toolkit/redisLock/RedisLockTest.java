package cn.cyejing.dsync.toolkit.redisLock;

import cn.cyejing.dsync.toolkit.DLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-09 15:12
 **/
public class RedisLockTest {
    private int i = 0;

    ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Test
    public void testLock() throws Exception {
        /**
         * 5c 1000n 15489ms
         * 10c 1000n 15134ms
         * 20c 1000n 16158ms
         * 50c 1000n 16837ms
         * 10c 10000n 31813ms
         */
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(new RedisStandaloneConfiguration("172.31.9.150", Protocol.DEFAULT_PORT));
        lettuceConnectionFactory.afterPropertiesSet();
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(lettuceConnectionFactory);
        stringRedisTemplate.afterPropertiesSet();
        RedisDistributedLock lock = new RedisDistributedLock(stringRedisTemplate);
        lock.afterPropertiesSet();

        int count = 1000;
        long start = System.currentTimeMillis();
        for (int j = 0; j < count; j++) {
            executorService.submit(() -> {
                lock.lock("adder").then(() -> {
                    i++;
                    return "ok";
                });
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        Assert.assertEquals(count, i);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + "ms");
    }
    private JedisClientConfiguration getJedisClientConfiguration() {
        JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(8);
        config.setMaxIdle(8);
        config.setMinIdle(0);
        return builder.usePooling().poolConfig(config).and().build();
    }
}
