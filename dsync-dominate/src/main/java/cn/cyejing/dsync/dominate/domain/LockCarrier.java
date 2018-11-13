package cn.cyejing.dsync.dominate.domain;

import cn.cyejing.dsync.dominate.interceptor.LockInterceptor;
import cn.cyejing.dsync.dominate.interceptor.ProcessPostLockInterceptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
/**
 *
 * @author Born
 */
@Slf4j
public class LockCarrier {

    private ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();

    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();

    private List<LockInterceptor> lockInterceptors = new ArrayList<>();

    private static final LockCarrier lockCarrier = new LockCarrier();

    private LockCarrier() {
        addLockInterceptor(new ProcessPostLockInterceptor());
    }

    public static LockCarrier getInstance() {
        return lockCarrier;
    }

    public boolean acquire(Operate operate) {
        String resource = operate.getResource();
        log.debug("acquire resource:{},operate:{}", resource, operate);

        Lock lock = lockMap.get(resource);
        if (lock == null) {
            lockMap.putIfAbsent(resource, new Lock());
            lock = lockMap.get(resource);
        }
        boolean acquire = lock.acquire(operate);
        lockInterceptors.forEach(i -> i.acquire(operate, acquire));
        return acquire;
    }

    public Operate release(Operate operate) {
        String resource = operate.getResource();
        log.debug("release resource:{}", resource);
        Lock lock = lockMap.get(resource);
        Operate currentOperate = lock.getCurrentOperate();
        if (currentOperate != null && currentOperate.equals(operate)) {
            Operate nextOperate = lock.release(operate);
            lockInterceptors.forEach(i -> i.release(operate, nextOperate));
            return nextOperate;
        }
        return null;
    }

    public List<Operate> processRelease(Process process) {
        log.debug("precess is down. process:{}", process);
        process.Inactive();
        List<Operate> releaseSet = new ArrayList<>();
        List<Operate> operates = process.getLockOperates();
        if (operates == null || operates.isEmpty()) {
            return releaseSet;
        }
        for (Operate operate : operates) {
            Operate nextOperate = release(operate);
            if (nextOperate != null) {
                releaseSet.add(nextOperate);
            }
        }
        lockInterceptors.forEach(i -> i.processDown(process, releaseSet));
        return releaseSet;
    }


    public void addLockInterceptor(LockInterceptor lockInterceptor) {
        this.lockInterceptors.add(lockInterceptor);
    }

    public Map<String,Lock> peekLockMap() {
        return Collections.unmodifiableMap(lockMap);
    }

}
