package cn.cyejing.dsync.example;

import cn.cyejing.dsync.toolkit.DLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

/**
 * @author Born
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Service
    public class LockService {
        Logger log = LoggerFactory.getLogger(LockService.class);

        @Autowired
        private DLock dLock;

        private int i = 0;

        public void lockExample() {
            dLock.lock("adder");
            try {
                dLock.lock("adder1");
                //Do Something
                int temp = i;
                Thread.sleep(10);
                i = temp + i;
            } catch (Exception e) {
                log.error("error", e);
            } finally {
                dLock.unlock();
            }
        }
    }
}
