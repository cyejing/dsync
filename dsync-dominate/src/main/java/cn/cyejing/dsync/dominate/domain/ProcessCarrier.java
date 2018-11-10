package cn.cyejing.dsync.dominate.domain;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-27 13:31
 **/
@Slf4j
public class ProcessCarrier {

    private Map<Long, Process> processIdMap = new HashMap<>();
    private Map<Channel, Process> processChannelMap = new HashMap<>();
    private AtomicInteger processIdAdder = new AtomicInteger(1);

    private static ProcessCarrier instance = new ProcessCarrier();

    private ProcessCarrier() {

    }

    public static ProcessCarrier getInstance() {
        return instance;
    }

    public void addProcess(Process process){
        log.info("add process to carrier:{}", process);
        processIdMap.put(process.getProcessId(), process);
        processChannelMap.put(process.getChannel(), process);
    }


    public long createProcessId() {
        return processIdAdder.getAndIncrement();
    }

    public Process get(long processId) {
        return processIdMap.get(processId);
    }

    public Process get(Channel channel) {
        return processChannelMap.get(channel);
    }

    public void addProcessLockOperate(Operate operate) {
        log.debug("add resource of operate:{}", operate);
        if (operate != null) {
            Process process = processIdMap.get(operate.getProcessId());
            if (process != null) {
                process.addLockOperate(operate);
            }
        }
    }

    public void removeProcessLockOperate(Operate operate) {
        log.debug("remove resource of operate:{}", operate);
        if (operate != null) {
            Process process = processIdMap.get(operate.getProcessId());
            if (process != null) {
                process.removeLockOperate(operate);
            }
        }
    }

    public void addProcessOperate(Operate operate) {
        log.debug("add resource of operate:{}", operate);
        if (operate != null) {
            Process process = processIdMap.get(operate.getProcessId());
            if (process != null) {
                process.addOperate(operate);
            }
        }
    }

    public void removeProcessOperate(Operate operate) {
        log.debug("remove resource of operate:{}", operate);
        if (operate != null) {
            Process process = processIdMap.get(operate.getProcessId());
            if (process != null) {
                process.removeOperate(operate);
            }
        }
    }

    public void removeProcess(Process process) {
        log.debug("process have remove:{}", process);
        processIdMap.remove(process.getProcessId());
        processChannelMap.remove(process.getChannel());
    }
}
