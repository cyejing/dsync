package cn.cyejing.dsync.example;

import cn.cyejing.dsync.toolkit.DLock;
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

        @Autowired
        private DLock dLock;

        private int i = 0;

        public void lockExample() {
            dLock.lock("adder");
            i++;
            dLock.unlock();
        }
    }
}
