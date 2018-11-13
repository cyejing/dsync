package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Steps;
import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DLockImpl implements DLock {

    private DLockClient client;

    private volatile long processId;
    private volatile CountDownLatch initProcessLatch = new CountDownLatch(1);

    private ConcurrentHashMap<String, CountDownLatch> localLock = new ConcurrentHashMap<>();
    private ThreadLocal<Request> threadLocal = new ThreadLocal<>();
    private AtomicLong lockIdAdder = new AtomicLong(1);

    public DLockImpl(Config config) {
        this.client = new DLockClient(config, this);
    }

    public void init() {
        client.connect();
    }

    @Override
    public void lock(String resource) {
        long processId = syncGetProcessId(); //重启也要获得新的processId
        if (threadLocal.get() != null) {
            return;
        }
        long lockId = createLockId();
        String key = lockId + "-" + resource;
        log.debug("put latch of key:{}", key);
        CountDownLatch latch = new CountDownLatch(1);
        localLock.put(key, latch);

        Request request = new Request(processId, lockId, Steps.Lock, resource);
        threadLocal.set(request);
        client.request(request);
        try {
            log.debug("lock key:{}", key);
            latch.await();
            if (this.processId == 0) {
                log.warn("channel is inactive, try connect...");
                localLock.remove(key);
                threadLocal.remove();
                lock(resource); // recycle lock ,waiting server...
            }
        } catch (InterruptedException e) {
            localLock.remove(key);
            threadLocal.remove();
            log.error("lock is interrupted", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unlock() {
        Request request = threadLocal.get();
        if (request == null) {
            log.warn("don't repeat unlock");
            return;
        }
        request.setOperate(Steps.Unlock);
        client.request(request);
        threadLocal.remove();
    }

    void revisionProcessId(long serverProcessId) {
        this.processId = serverProcessId;
        initProcessLatch.countDown();
        log.info("connection server success and revision processId:{}", this.processId);
    }

    void countDown(long lockId, String resource) {
        String key = lockId + "-" + resource;
        CountDownLatch latch = localLock.get(key);
        log.debug("count down key:{}, count:{}", key, latch);
        if (latch != null) {
            latch.countDown();
            localLock.remove(key);
        }
    }

    void serverBreak() {
        this.processId = 0;
        this.initProcessLatch = new CountDownLatch(1);
        localLock.values().forEach(latch -> latch.countDown());
        localLock.clear();
    }

    private long syncGetProcessId() {
        try {
            if (this.processId == 0) {
                log.debug("waiting for server response process  id");
                initProcessLatch.await();
            }
        } catch (InterruptedException e) {
            log.error("waiting for process  is interrupted", e);
            throw new RuntimeException(e);
        }
        return this.processId;
    }

    private long createLockId() {
        return lockIdAdder.getAndIncrement();
    }

}
