package cn.cyejing.dsync.toolkit.redisLock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author: Born
 * @create: 2018/7/12
 **/
@Slf4j
public class RedisDistributedLock  implements DistributedLock{


    private static final String PREFIX = "RedisLock->";
    protected static final int DEFAULT_RETRY_SECONDS = 5;
    protected static final int DEFAULT_REFRESH_SECONDS = 5;
    protected static final int DEFAULT_MAX_WAIT_SECONDS = 50;
    private static final String KEY_PREFIX = "key->";
    private static final String LOCK_CLIENT_KEY = PREFIX + "lock-client-id-generate-key";
    private static final String CHANNEL = PREFIX + "unlock-notify-key";

    private int retryInterval = DEFAULT_RETRY_SECONDS;
    private int refreshInterval = DEFAULT_REFRESH_SECONDS;
    private int maxWaitSeconds = DEFAULT_MAX_WAIT_SECONDS;
    private long clientId = -1;
    private AtomicLong requestId = new AtomicLong();

    private Set<String> locks = Collections.synchronizedSet(new HashSet<>());
    private Map<String, AtomicInteger> mutexes = new ConcurrentHashMap<>();
    private ThreadLocal<Set<String>> currentKeys = ThreadLocal.withInitial(() -> new HashSet<>());

    private final StringRedisTemplate redis;

    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.redis = stringRedisTemplate;
    }


    protected int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    protected int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    protected int getMaxWaitSeconds() {
        return maxWaitSeconds;
    }

    public void setMaxWaitSeconds(int maxWaitSeconds) {
        this.maxWaitSeconds = maxWaitSeconds;
    }

    public void afterPropertiesSet() {
        clientId = redis.opsForValue().increment(LOCK_CLIENT_KEY, 1);

        Thread refresh = new Thread(() -> {
            while (true) {
                try {
                    String[] names = locks.toArray(new String[locks.size()]);
                    Arrays.stream(names).forEach(name -> redis.expire(toKey(name), refreshInterval, TimeUnit.SECONDS));

                    TimeUnit.SECONDS.sleep(refreshInterval / 2);
                } catch (Exception e) {
                    log.error("refresh lock key fail", e);
                }
            }
        });

        refresh.setDaemon(true);
        refresh.start();

        Thread subscribe = new Thread(
                        () -> redis.getConnectionFactory().getConnection().subscribe((message, pattern) -> {
                            String messageBody = new String(message.getBody());
                            log.debug("Got unlock message: {}", messageBody);
                            if (mutexes.containsKey(messageBody)) {
                                AtomicInteger ai = mutexes.get(messageBody);
                                synchronized (ai) {
                                    ai.notify();
                                }
                            }
                        }, CHANNEL.getBytes()));
        subscribe.setDaemon(true);
        subscribe.start();
    }

    @Override
    public WithinLock lock(int timeout, String key) {
        Set<String> cKeys = currentKeys.get();
        if (cKeys.contains(key)) {
            return new DoNothingWithinLock();
        }

        String id = null;
        try {
            id = tryToGetLock(key, timeout);
            cKeys.add(key);
            locks.add(key);
        } catch (Exception e) {
            if (id != null) {
                cKeys.remove(key);
                removeLock(key, id);
            }
            log.error("lock failed. please check redis status", e);
            return new DoNothingWithinLock(); // failed  continue
        }

        return new DefaultWithinLock(this, key, id);
    }

    @Override
    public WithinLock lock(String key) {
        return lock(maxWaitSeconds, key);
    }


    private String tryToGetLock(String name, int seconds) {
        log.debug("try to obtain lock for {}", name);

        long timeout = TimeUnit.SECONDS.toMillis(Long.valueOf(seconds));
        long retry = TimeUnit.SECONDS.toMillis(retryInterval);
        long start = System.currentTimeMillis();
        long duration = 0;
        String key = toKey(name);

        AtomicInteger ai = getMutex(name);
        ai.incrementAndGet();

        String id = clientId + "-" + requestId.incrementAndGet();
        while (duration < timeout) {
            boolean success = setNxAndExpire(key, id);
            if (success) {
                log.debug("Got lock for {}", key);
                decreaseMutex(name, ai);
                return id;
            }

            synchronized (ai) {
                try {
                    long delta = timeout - duration;
                    long wait = retry > delta ? (delta < 1000 ? 1000 : delta) : retry;
                    ai.wait(wait);
                } catch (InterruptedException e) {
                    decreaseMutex(name, ai);
                    log.error("Thread is interrupted while attempt to obtain lock.", e);
                    throw new RuntimeException("message.distributed.lock.fetch-lock-fail");
                }
            }

            duration = System.currentTimeMillis() - start;
        }

        decreaseMutex(name, ai);
        log.error("Can not obtain a lock after {} seconds.", seconds);
        throw new RuntimeException("message.distributed.lock.fetch-lock-timeout");
    }

    private AtomicInteger getMutex(String name) {
        if (mutexes.containsKey(name)) {
            return mutexes.get(name);
        }

        synchronized (this) {
            if (mutexes.containsKey(name)) {
                return mutexes.get(name);
            }

            AtomicInteger ai = new AtomicInteger(0);
            mutexes.put(name, ai);
            return ai;
        }
    }

    private void decreaseMutex(String name, AtomicInteger ai) {
        int count = ai.decrementAndGet();
        if (count == 0) {
            mutexes.remove(name);
        }
    }

    private boolean setNxAndExpire(String key, String id) {
        RedisConnection connection = redis.getConnectionFactory().getConnection();
        try {

            boolean success = connection.setNX(key.getBytes(), id.getBytes());
            if (success) {
                connection.expire(key.getBytes(), refreshInterval);
            }
            return success;
        } finally {
            connection.close();
        }
    }

    private void removeLock(String name, String value) {
        try {
            String key = toKey(name);
            String v = redis.opsForValue().get(key);
            if (v == null || !v.equals(value)) {
                log.error("lock value disagree! redis lock value:{}. local value:{}", v, value);
                return;
            }
            redis.delete(key);
            redis.convertAndSend(CHANNEL, name);
        } finally {
            locks.remove(name);
        }

    }

    protected static class DoNothingWithinLock implements WithinLock {

        @Override
        public <T> T then(LockCallback<T> cb) {
            return cb.execute();
        }
    }

    protected static class DefaultWithinLock implements WithinLock {

        private RedisDistributedLock parent = null;
        private String key = null;
        private String value = null;

        public DefaultWithinLock(RedisDistributedLock parent, String key, String value) {
            this.parent = parent;
            this.key = key;
            this.value = value;
        }

        @Override
        public <T> T then(LockCallback<T> cb) {
            try {
                return cb.execute();
            } finally {
                unlock();
            }
        }

        public void unlock() {
            log.debug("unlock key:{}", key);
            Set<String> cKeys = parent.currentKeys.get();
            cKeys.remove(key);
            parent.removeLock(key, value);
        }
    }


    private String toKey(String name) {
        return PREFIX + KEY_PREFIX + name;
    }
}
