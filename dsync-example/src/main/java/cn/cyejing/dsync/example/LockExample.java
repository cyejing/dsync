package cn.cyejing.dsync.example;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;

/**
 *
 * @author Born
 */
public class LockExample {

    public static void main(String[] args) {
        Config config = Config.config().host("localhost").port(4843);
        DSync dSync = DSync.create(config);
        DLock lock = dSync.getLock();

        lock.lock("adder");
        //Do Something
        lock.unlock();

    }

}
