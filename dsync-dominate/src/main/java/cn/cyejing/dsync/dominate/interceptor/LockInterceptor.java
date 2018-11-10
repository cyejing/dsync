package cn.cyejing.dsync.dominate.interceptor;

import cn.cyejing.dsync.dominate.domain.Operate;
import cn.cyejing.dsync.dominate.domain.Process;
import java.util.List;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-06 11:18
 **/
public interface LockInterceptor {

    void acquire(Operate operate,boolean lock);

    void release(Operate currentOperate,Operate nextOperate);

    void processDown(Process process, List<Operate> operates);
}
