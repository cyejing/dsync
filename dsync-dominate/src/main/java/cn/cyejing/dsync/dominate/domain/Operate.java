package cn.cyejing.dsync.dominate.domain;

import io.netty.channel.Channel;
import java.util.Objects;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Born
 */
@Getter
@Setter
@ToString
public class Operate {

    private String operateId;
    private long processId;
    private long lockId;
    private String resource;
    private Channel channel;

    private boolean active = true;

    public Operate(long processId,long lockId, String resource, Channel channel) {
        this.processId = processId;
        this.lockId = lockId;
        this.operateId = processId + "-" + lockId;
        this.resource = resource;
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Operate operate = (Operate) o;
        return processId == operate.processId &&
                lockId == operate.lockId &&
                Objects.equals(operateId, operate.operateId) &&
                Objects.equals(resource, operate.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operateId, processId, lockId, resource);
    }

    public void Inactive() {
        this.active = false;
    }
}
