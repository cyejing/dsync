package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;
import cn.cyejing.dsync.common.model.ResponseCode;
import cn.cyejing.dsync.common.model.Steps;
import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Born
 */
@Slf4j
public class DLockImpl implements DLock {

    private DLockClient client;

    private volatile long processId;
    private volatile CountDownLatch initProcessLatch = new CountDownLatch(1);

    private ThreadLocal<Request> threadLocal = new ThreadLocal<>();
    private AtomicLong lockIdAdder = new AtomicLong(1);

    public DLockImpl(Config config) {
        this.client = new DLockClient(config, this);
    }

    public void init() {
        client.connect();
    }

    @Override
    public boolean tryLock(String resource) {
        log.debug("try lock resource:{}", resource);
        long processId = syncGetProcessId();
        if (threadLocal.get() != null) {
            return true;
        }
        long lockId = createLockId();

        Request request = new Request(processId, lockId, Steps.TryLock, resource);
        threadLocal.set(request);
        ResponseFuture responseFuture = client.request(request);

        try {
            Response response = responseFuture.get();
            log.debug("try lock resource is:{}", response);
            if (this.processId == 0) {
                log.warn("channel is inactive, try connect...");
                threadLocal.remove();
                return false;
            }
            return ResponseCode.Ok.equals(response.getCode());
        } catch (InterruptedException e) {
            threadLocal.remove();
            log.error("lock is interrupted", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lock(String resource, Duration duration) {
        log.debug("lock resource:{}", resource);
        long processId = syncGetProcessId();
        if (threadLocal.get() != null) {
            return;
        }
        long lockId = createLockId();


        Request request = new Request(processId, lockId, Steps.Lock, resource);
        threadLocal.set(request);
        ResponseFuture responseFuture = client.request(request);
        try {
            responseFuture.get(duration);
            log.debug("get lock resource:{}", resource);
            if (this.processId == 0) {
                log.warn("channel is inactive, try connect...");
                threadLocal.remove();
                lock(resource); // recycle lock ,waiting server...
            }
        } catch (InterruptedException e) {
            unlock();
            log.error("lock is interrupted", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lock(String resource) {
        lock(resource, Duration.ofSeconds(10));
    }


    @Override
    public void unlock() {
        Request request = threadLocal.get();
        if (request == null) {
            log.warn("don't repeat unlock");
            return;
        }
        try {
            request.setOperate(Steps.Unlock);
            log.debug("unlock request:{}", request);

            client.unlock(request);
        } finally {
            threadLocal.remove();
        }
    }

    @Override
    public void shutdown() {
        client.shutdown();
        initProcessLatch.countDown();
    }

    void revisionProcessId(long serverProcessId) {
        this.processId = serverProcessId;
        initProcessLatch.countDown();
        log.info("connection server success and revision processId:{}", this.processId);
    }

    void serverBreak() {
        this.processId = 0;
        this.initProcessLatch = new CountDownLatch(1);
    }

    private long syncGetProcessId() {
        try {
            if (this.processId == 0) {
                log.debug("waiting for server response process  id");
                boolean await = initProcessLatch.await(10, TimeUnit.SECONDS);
                if (await == false) {
                    throw new RuntimeException("wait lock server timeout, check server status");
                }
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



