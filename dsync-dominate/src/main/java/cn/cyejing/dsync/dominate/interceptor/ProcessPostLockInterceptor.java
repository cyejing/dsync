package cn.cyejing.dsync.dominate.interceptor;

import cn.cyejing.dsync.dominate.domain.Operate;
import cn.cyejing.dsync.dominate.domain.Process;
import cn.cyejing.dsync.dominate.domain.ProcessCarrier;
import java.util.List;

/**
 *
 * @author Born
 */
public class ProcessPostLockInterceptor implements LockInterceptor {

    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();

    @Override
    public void acquire(Operate operate, boolean lock) {
        if (lock) {
            processCarrier.addProcessLockOperate(operate);
        }
        processCarrier.addProcessOperate(operate);
    }

    @Override
    public void release(Operate currentOperate, Operate nextOperate) {
        processCarrier.removeProcessOperate(currentOperate);
        processCarrier.removeProcessLockOperate(currentOperate);
        processCarrier.addProcessLockOperate(nextOperate);
    }

    @Override
    public void processDown(Process process, List<Operate> operates) {
        processCarrier.removeProcess(process);
    }

    @Override
    public void tryAcquire(Operate operate, boolean lock) {
        if (lock) {
            processCarrier.addProcessLockOperate(operate);
        }
        processCarrier.addProcessOperate(operate);
    }
}
