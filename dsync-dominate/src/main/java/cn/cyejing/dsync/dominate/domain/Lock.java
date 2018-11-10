package cn.cyejing.dsync.dominate.domain;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-10 20:51
 **/
@Slf4j
public class Lock {

    private Operate currentOperate;
    private Queue<Operate> queueWaiter = new ConcurrentLinkedQueue();

    public synchronized  boolean acquire(Operate operate) {
        if (currentOperate == null) {
            currentOperate = operate;
            return true;
        } else {
            queueWaiter.add(operate);
            return false;
        }
    }

    public synchronized Operate release(Operate operate) {
        if (currentOperate == null || !currentOperate.equals(operate)) {
            log.warn("error that should not happen! currentOperate:{},operate:{}", currentOperate, operate);
            return null;
        }
        if (queueWaiter.isEmpty()) {
            currentOperate = null;
            return null;
        }
        Operate nextOperate = queueWaiter.poll();
        while (nextOperate != null && (!nextOperate.isActive() || !nextOperate.getChannel().isActive())) {
            nextOperate = queueWaiter.poll();
        }
        currentOperate = nextOperate;
        return nextOperate;
    }

    public synchronized Operate getCurrentOperate() {
        return currentOperate;
    }
}
