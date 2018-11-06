package cn.cyejing.dsync.dominate.interceptor;

import cn.cyejing.dsync.dominate.domain.LockCarrier;
import cn.cyejing.dsync.dominate.domain.Operate;
import cn.cyejing.dsync.dominate.domain.Process;
import cn.cyejing.dsync.dominate.domain.ProcessCarrier;
import com.alibaba.fastjson.JSON;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-06 15:55
 **/
@Slf4j
public class TraceLockInterceptor implements LockInterceptor {

    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();
    private LockCarrier lockCarrier = LockCarrier.getInstance();

    @Override
    public void lock(Operate operate, boolean lock) {
        logLockCarrier();
    }

    private void logLockCarrier() {
        Map map = lockCarrier.peekLockMap();
        Map queue = lockCarrier.peekLockQueue();
        log.debug("map:{}", JSON.toJSONString(map));
        log.debug("queue:{}", JSON.toJSONString(queue));
    }

    @Override
    public void unlock(Operate currentOperate, Operate nextOperate) {
        logLockCarrier();

    }

    @Override
    public void processDown(Process process, List<Operate> operates) {
        logLockCarrier();
    }
}
