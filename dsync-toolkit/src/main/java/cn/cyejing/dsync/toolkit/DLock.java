package cn.cyejing.dsync.toolkit;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-12 16:57
 **/
public interface DLock {

    void lock(String resource);

    void unlock();

}
