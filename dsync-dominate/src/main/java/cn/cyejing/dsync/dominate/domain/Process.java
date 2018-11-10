package cn.cyejing.dsync.dominate.domain;

import io.netty.channel.Channel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-27 13:27
 **/
@Data
public class Process {

    private long processId;
    private List<Operate> operates = new LinkedList<>();
    private List<Operate> lockOperates = new LinkedList<>();
    private Channel channel;
    private boolean active;

    public Process(Channel channel) {
        this.channel = channel;
        this.processId = ProcessCarrier.getInstance().createProcessId();
        this.active = true;
    }

    public void addLockOperate(Operate operate) {
        lockOperates.add(operate);
    }

    public void removeLockOperate(Operate operate) {
        lockOperates.remove(operate);
    }

    public void addOperate(Operate operate) {
        operates.add(operate);
    }
    public void removeOperate(Operate operate){
        operates.remove(operate);
    }

    public void Inactive() {
        this.active = false;
        operates.forEach(Operate::Inactive);
    }
}
