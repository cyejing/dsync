package cn.cyejing.dsync.dominate.domain;

import cn.cyejing.dsync.dominate.interceptor.LockInterceptor;
import cn.cyejing.dsync.dominate.interceptor.ProcessPostLockInterceptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-17 16:14
 **/
@Slf4j
public class LockCarrier {

    private ConcurrentHashMap<String, Operate> lockMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Queue<Operate>> conditionQueue = new ConcurrentHashMap<>();

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
        Operate ifAbsent = lockMap.putIfAbsent(resource, operate);
        boolean lock = ifAbsent == null ? true : false;
        if (!lock) {
            log.debug("acquire resource:{} fail, push to deque. operate:{}", resource, operate);
            pushChannel(resource, operate);
        }
        lockInterceptors.forEach(i -> i.acquire(operate, lock));
        return lock;
    }

    public Operate release(Operate operate) {
        String resource = operate.getResource();
        log.debug("release resource:{}", resource);
        Operate currentOperate = lockMap.get(resource);
        if (!operate.equals(currentOperate)) {
            log.error("release fail, because operate not current acquire. current:{}, release:{}", currentOperate,
                    operate);
            return null;
        }

        Operate nextOperate = popChannel(resource);
        Operate oldOperate;
        if (nextOperate == null) {
            oldOperate = lockMap.remove(resource);
        } else {
            oldOperate = lockMap.put(resource, nextOperate);
        }
        if (!currentOperate.equals(oldOperate)) {
            log.error("###Error that should not happen!!!");
        }
        lockInterceptors.forEach(i -> i.release(oldOperate, nextOperate));
        return nextOperate;
    }

    public List<Operate> processRelease(Process process) {
        log.debug("precess is down. process:{}", process);
        process.Inactive();
        List<Operate> unLockSet = new ArrayList<>();
        List<Operate> operates = process.getLockOperates();
        if (operates == null || operates.isEmpty()) {
            return unLockSet;
        }
        for (Operate operate : operates) {
            Operate currentOperate = lockMap.get(operate.getResource());
            if (currentOperate != null && process.getProcessId() == currentOperate.getProcessId()) {
                Operate nextOperate = release(operate);
                if (nextOperate != null) {
                    unLockSet.add(nextOperate);
                }
            }
        }
        lockInterceptors.forEach(i -> i.processDown(process, unLockSet));
        return unLockSet;
    }

    private void pushChannel(String resource, Operate channel) {
        Queue<Operate> operators = conditionQueue.get(resource);
        if (operators == null) {
            operators = new ConcurrentLinkedQueue<>();
            Queue<Operate> ifAbsent = conditionQueue.putIfAbsent(resource, operators);
            if (ifAbsent != null) {
                operators = ifAbsent;
            }
        }
        operators.add(channel);
    }

    private Operate popChannel(String resource) {
        Queue<Operate> operators = conditionQueue.get(resource);

        Operate poll = null;
        while (!operators.isEmpty() && poll == null) {
            poll = operators.poll();
            if (!poll.isActive() && !poll.getChannel().isActive()) {
                poll = null;
            }
        }
        return poll;
    }

    public void addLockInterceptor(LockInterceptor lockInterceptor) {
        this.lockInterceptors.add(lockInterceptor);
    }

    public Map peekLockMap() {
        return Collections.unmodifiableMap(lockMap);
    }
    public Map peekLockQueue() {
        return Collections.unmodifiableMap(conditionQueue);
    }
}
