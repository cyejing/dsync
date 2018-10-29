package cn.cyejing.dsync.dominate.domain;

import io.netty.channel.Channel;
import java.util.HashSet;
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
    private Set<String> resources = new HashSet<>();
    private Channel channel;

    public Process(Channel channel) {
        this.channel = channel;
        this.processId = ProcessCarrier.getInstance().createProcessId();
    }

    public void addResource(String resource) {
        resources.add(resource);
    }
}
