package cn.cyejing.dsync.dominate.domain;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
/**
 *
 * @author Born
 */
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
        if (operate == null) {
            log.error("error of operate");
            return null;
        }
        if (currentOperate == null || !currentOperate.equals(operate)) {
            queueWaiter.remove(operate);
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

    public synchronized boolean tryAcquire(Operate operate) {
        if (currentOperate == null) {
            currentOperate = operate;
            return true;
        }else{
            return false;
        }
    }
}
