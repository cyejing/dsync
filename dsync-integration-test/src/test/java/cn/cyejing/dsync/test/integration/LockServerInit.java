package cn.cyejing.dsync.test.integration;

import cn.cyejing.dsync.dominate.LockServer;
import org.junit.Before;

/**
 * @author Born
 */
public class LockServerInit {

    public void startServer(int port) throws InterruptedException {
        LockServer lockServer = new LockServer();
        new Thread(() -> {
            try {
                lockServer.start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



}
