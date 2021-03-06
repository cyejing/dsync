package cn.cyejing.dsync.common.model;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

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
public class Request implements Serializable {

    private Steps operate;
    private long processId;
    private long lockId;
    private String resource;
    private String data;

    public Request(long processId, long lockId, Steps operate, String resource, String data) {
        this.processId = processId;
        this.lockId = lockId;
        this.operate = operate;
        this.resource = resource;
        this.data = data;
    }

    public Request(long processId, long lockId, Steps operate, String resource) {
        this.processId = processId;
        this.lockId = lockId;
        this.operate = operate;
        this.resource = resource;
    }
}
