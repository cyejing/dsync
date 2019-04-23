package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;
import cn.cyejing.dsync.common.model.ResponseCode;
import cn.cyejing.dsync.common.model.Steps;
import cn.cyejing.dsync.toolkit.Config;
import cn.cyejing.dsync.toolkit.DLock;
import cn.cyejing.dsync.toolkit.exception.LockTimeoutException;
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
        ResponseFuture responseFuture = client.request(request);

        try {
            Response response = responseFuture.get();
            threadLocal.set(request);
            log.info("try lock resource is:{}", response);
            if (this.processId == 0) {
                log.warn("channel is inactive, try connect...");
                threadLocal.remove();
                return false;
            }
            return ResponseCode.Ok.equals(response.getCode());
        } catch (Exception e) {
            threadLocal.remove();
            log.error("try lock resume error", e);
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
        log.info("try get lock request:{}", request);
        ResponseFuture responseFuture = client.request(request);
        try {
            responseFuture.get(duration);
            threadLocal.set(request);
            log.info("get lock request:{}", request);
            if (this.processId == 0) {
                log.warn("channel is inactive, try connect...");
                threadLocal.remove();
                lock(resource); // recycle lock ,waiting server...
            }
        } catch (LockTimeoutException e) {
            unlock(request);
            throw e;
        } catch (Exception e) {
            unlock(request);
            log.error("lock resume error", e);
            throw new RuntimeException(e);
        }
    }

    private void unlock(Request request) {
        try {
            request.setOperate(Steps.Unlock);
            log.info("unlock request:{}", request);
            client.unlock(request);
        } finally {
            threadLocal.remove();
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
            log.debug("is repeat unlock");
            return;
        }
       unlock(request);
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
                initProcessLatch.await();
//                boolean await = initProcessLatch.await(10, TimeUnit.SECONDS);
//                if (await == false) {
//                    log.error("wait lock server timeout, check server status");
//                    throw new RuntimeException("wait lock server timeout, check server status");
//                }
            }
        } catch (Exception e) {
            log.error("waiting for process  is interrupted", e);
            throw new RuntimeException(e);
        }
        return this.processId;
    }

    private long createLockId() {
        return lockIdAdder.getAndIncrement();
    }

}



