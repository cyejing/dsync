package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.dominate.LockServer;
import org.junit.Before;

/**
 * @author Born
 */
public class LockServerInit {

    @Before
    public void startServer() throws InterruptedException {
        LockServer lockServer = new LockServer();
        new Thread(() -> {
            try {
                lockServer.start(4843);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
