package cn.cyejing.dsync.toolkit;

/**
 *
 * @author Born
 */
public interface DLock {

    void lock(String resource);

    void unlock();

}
