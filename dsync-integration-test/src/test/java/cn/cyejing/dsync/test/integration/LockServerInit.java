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
                lockServer.start(getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public int getPort() {
        return 4843;
    }


}
