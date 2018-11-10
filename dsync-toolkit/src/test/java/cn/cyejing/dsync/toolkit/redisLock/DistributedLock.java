package cn.cyejing.dsync.toolkit.redisLock;

/**
 * @author guyong
 */
public interface DistributedLock {

    /**
     * @param timeout 获得锁最大超时时间
     * @param key 加锁的Key值
     */
    WithinLock lock(int timeout, String key);

    /**
     * @param key 加锁的Key值
     */
    WithinLock lock(String key);

    @FunctionalInterface
    interface WithinLock {

        <T> T then(LockCallback<T> cb);
    }

    @FunctionalInterface
    interface LockCallback<T> {

        T execute();
    }
}
