package cn.cyejing.dsync.toolkit;

import java.time.Duration;

/**
 *
 * @author Born
 */
public interface DLock {

    /**
     * 尝试获取锁，返回是否获取成功
     * @param resource
     * @return
     */
    boolean tryLock(String resource);
    /**
     * 对指定资源加锁,如果抢占不到锁会阻塞
     * @param resource
     */
    void lock(String resource);

    /**
     * 额外设置获取锁超时时间
     * @param resource
     * @param duration
     */
    void lock(String resource, Duration duration);

    /**
     * 解锁操作
     */
    void unlock();

    /**
     * 关机,关闭连接资源
     */
    void shutdown();
}
