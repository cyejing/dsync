package cn.cyejing.dsync.example;

import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.DSync;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-13 13:50
 **/
public class LockExample {

    public static void main(String[] args) {
        Config config = Config.config().host("localhost").port(4843);
        DSync dSync = DSync.create(config);
        DLock lock = dSync.getLock();

        lock.lock("adder");
        lock.unlock();

    }

}
