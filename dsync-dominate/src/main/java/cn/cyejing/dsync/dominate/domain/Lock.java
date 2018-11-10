package cn.cyejing.dsync.dominate.domain;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-10 20:51
 **/
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

    public synchronized Operate release() {
        if (currentOperate == null || queueWaiter.isEmpty()) {
            currentOperate = null;
            return null;
        }
        Operate nextOperate = queueWaiter.poll();
        currentOperate = nextOperate;
        return nextOperate;
    }
}
