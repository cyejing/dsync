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
 *
 * @author Born
 */
@Slf4j
public class TraceLockInterceptor implements LockInterceptor {

    private LockCarrier lockCarrier = LockCarrier.getInstance();

    @Override
    public void acquire(Operate operate, boolean lock) {
        logLockCarrier();
    }

    private void logLockCarrier() {
        Map map = lockCarrier.peekLockMap();
        log.debug("map:{}", JSON.toJSONString(map));
    }

    @Override
    public void release(Operate currentOperate, Operate nextOperate) {
        logLockCarrier();

    }

    @Override
    public void processDown(Process process, List<Operate> operates) {
        logLockCarrier();
    }
}
