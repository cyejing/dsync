package cn.cyejing.dsync.dominate.domain;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-27 13:29
 **/
@Data
public class Operate {

    private String operateId;
    private long processId;
    private long lockId;
    private String resource;
    private Channel channel;

    public Operate(long processId,long lockId, String resource, Channel channel) {
        this.processId = processId;
        this.lockId = lockId;
        this.operateId = processId + "-" + lockId;
        this.resource = resource;
        this.channel = channel;
    }
}
