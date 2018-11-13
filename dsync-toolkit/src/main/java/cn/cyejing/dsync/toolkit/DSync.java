package cn.cyejing.dsync.toolkit;

import cn.cyejing.dsync.toolkit.lock.DLockImpl;

/**
 *
 * @author Born
 */
public class DSync {

    private final Config config;
    private DLock lock;

    public DSync(Config config) {
        this.config = config;
    }

    public static DSync create(Config config) {
        return new DSync(config);
    }

    public DLock getLock() {
        if (lock == null) {
            synchronized (this) {
                if (lock == null) {
                    DLockImpl dLock = new DLockImpl(config);
                    dLock.init();
                    lock = dLock;
                }
            }
        }
        return lock;
    }

}
