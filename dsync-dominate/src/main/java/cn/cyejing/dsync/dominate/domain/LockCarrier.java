package cn.cyejing.dsync.dominate.domain;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-17 16:14
 **/
@Slf4j
public class LockCarrier {

    private ConcurrentHashMap<String, Operate> lockMap = new ConcurrentHashMap<>();

    private Map<String, Deque<Operate>> conditionOperator = new HashMap<>();

    private static final LockCarrier lockCarrier = new LockCarrier();
    private LockCarrier() {
    }

    public static LockCarrier getInstance() {
        return lockCarrier;
    }

    public synchronized boolean tryLock(String resource, Operate operate) {
        log.debug("lock resource:{},operate:{}", resource, operate);
        Operate ifAbsent = lockMap.putIfAbsent(resource, operate);
        boolean lock = ifAbsent == null ? true : false;
        if (!lock) {
            log.debug("lock resource:{} fail, push to deque. operate:{}", resource, operate);
            pushChannel(resource, operate);
        }
        return lock;
    }

    public synchronized Operate unLock(String resource) {
        log.debug("unlock resource:{}", resource);
        Operate operator = popChannel(resource);
        if (operator == null) {
            lockMap.remove(resource);
        }else{
            lockMap.put(resource, operator);

        }
        return operator;
    }

    public synchronized Set<Operate> processDown(Process process) {
        log.debug("precess is down. process:{}",process);
        Set<Operate> unLockSet = new HashSet<>();
        Set<String> resources = process.getResources();
        for (String resource : resources) {
            Deque<Operate> operates = conditionOperator.get(resource);
            if (operates != null) {
                operates.removeIf(o -> o.getProcessId() == process.getProcessId());
            }

            Operate operate = lockMap.get(resource);
            if (operate != null && process.getProcessId() == operate.getProcessId()) {
                if (!process.getChannel().equals(operate.getChannel()) || process.getProcessId() != operate
                        .getProcessId()) {
                    log.error("注意检查错误情况:process:{},operate:{}", process, operate);
                }
                Operate nextOperate = unLock(resource);
                if (nextOperate != null) {
                    unLockSet.add(nextOperate);
                }
            }


        }
        return unLockSet;
    }

    private void pushChannel(String resource, Operate channel) {
        Deque<Operate> operators = conditionOperator.get(resource);
        if (operators == null) {
            operators = new LinkedList<>();
            conditionOperator.put(resource, operators);
        }
        operators.addLast(channel);
    }

    private Operate popChannel(String resource) {
        Deque<Operate> operators = conditionOperator.get(resource);
        if (operators == null || operators.size() == 0) {
            return null;
        }
        return operators.pollFirst();
    }

}
