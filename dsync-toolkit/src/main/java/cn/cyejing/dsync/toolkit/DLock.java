package cn.cyejing.dsync.toolkit;

/**
 *
 * @author Born
 */
public interface DLock {

    boolean tryLock(String resource);
    /**
     * 对指定资源加锁,如果抢占不到锁会阻塞
     * @param resource
     */
    void lock(String resource);

    /**
     * 解锁操作
     */
    void unlock();

    /**
     * 关机,关闭连接资源
     */
    void shutdown();
}
