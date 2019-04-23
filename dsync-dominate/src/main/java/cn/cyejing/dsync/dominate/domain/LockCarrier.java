package cn.cyejing.dsync.dominate.domain;

import cn.cyejing.dsync.dominate.interceptor.LockInterceptor;
import cn.cyejing.dsync.dominate.interceptor.ProcessPostLockInterceptor;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * 锁资源记录表
     */
    private ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();

    /**
     * 进程载体
     */
    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();

    /**
     * 锁操作拦截器
     */
    private List<LockInterceptor> lockInterceptors = new ArrayList<>();

    /**
     * 单例
     */
    private static final LockCarrier lockCarrier = new LockCarrier();

    private LockCarrier() {
        addLockInterceptor(new ProcessPostLockInterceptor());
    }

    public static LockCarrier getInstance() {
        return lockCarrier;
    }

    /**
     * 尝试获取锁, 返回获取是否成功,不加入等待队列
     * @param operate
     * @return
     */
    public boolean tryAcquire(Operate operate) {
        log.debug("try acquire operate:{}", operate);
        String resource = operate.getResource();
        Lock lock = lockMap.get(resource);
        if (lock == null) {
            lockMap.putIfAbsent(resource, new Lock());
            lock = lockMap.get(resource);
        }
        boolean acquire = lock.tryAcquire(operate);
        lockInterceptors.forEach(i -> i.tryAcquire(operate, acquire));
        return acquire;
    }


    /**
     * 获取锁,获取不成功加入等待队列
     * @param operate
     * @return
     */
    public boolean acquire(Operate operate) {
        log.info("acquire operate:{}", operate);
        String resource = operate.getResource();

        Lock lock = lockMap.get(resource);
        if (lock == null) {
            lockMap.putIfAbsent(resource, new Lock());
            lock = lockMap.get(resource);
        }
        boolean acquire = lock.acquire(operate);
        log.debug("acquire operate is ", acquire);
        lockInterceptors.forEach(i -> i.acquire(operate, acquire));
        return acquire;
    }

    /**
     * 释放锁
     * @param operate
     * @return
     */
    public Operate release(Operate operate) {
        log.debug("release operate:{}", operate);
        String resource = operate.getResource();
        Lock lock = lockMap.get(resource);
        Operate nextOperate = lock.release(operate);
        if (nextOperate != null) {
            log.debug("next operate acquire:{}", operate);
            lockInterceptors.forEach(i -> i.release(operate, nextOperate));
            return nextOperate;
        }
        return null;
    }

    /**
     * 进程释放所有锁资源
     * @param process
     * @return
     */
    public List<Operate> processRelease(Process process) {
        log.debug("process release:{}", process);
        if (process == null) {
            return Collections.emptyList();
        }
        process.Inactive();
        List<Operate> releaseSet = new ArrayList<>();
        List<Operate> operates = process.getOperates();
        if (operates == null || operates.isEmpty()) {
            lockInterceptors.forEach(i -> i.processDown(process, releaseSet));
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

    public void clear() {
        lockMap.clear();
    }

}
