package cn.cyejing.dsync.dominate.interceptor;

import cn.cyejing.dsync.dominate.domain.Operate;
import cn.cyejing.dsync.dominate.domain.Process;
import cn.cyejing.dsync.dominate.domain.ProcessCarrier;
import java.util.List;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-06 15:55
 **/
public class ProcessPostLockInterceptor implements LockInterceptor {

    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();

    @Override
    public void lock(Operate operate, boolean lock) {
        if (lock) {
            processCarrier.addProcessLockOperate(operate);
        }
        processCarrier.addProcessOperate(operate);
    }

    @Override
    public void unlock(Operate currentOperate, Operate nextOperate) {
        processCarrier.removeProcessOperate(currentOperate);
        processCarrier.removeProcessLockOperate(currentOperate);
        processCarrier.addProcessLockOperate(nextOperate);
    }

    @Override
    public void processDown(Process process, List<Operate> operates) {
        processCarrier.removeProcess(process);
    }
}
