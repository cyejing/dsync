package cn.cyejing.dsync.example;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;

/**
 *
 * JAVA代码方式使用
 * @author Born
 */
public class LockExample {

    private static int i = 0;

    public static void main(String[] args) {
        Config config = Config.config().host("localhost").port(4843);
        DSync dSync = DSync.create(config);
        DLock lock = dSync.getLock();

        try {
            lock.lock("adder1");
            int temp = i;
            Thread.sleep(10);
            i = temp + i;
            //Do Something
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        //关闭netty线程,进程退出
        lock.shutdown();

    }

}
