package cn.cyejing.dsync.common.model;

import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author Born
 */
@Data
public class Response implements Serializable {

    private Steps operate;
    private long processId;
    private long lockId;
    private String resource;
    private ResponseCode code;

    private String message;

    public Response() {
    }

    public Response(Steps operate, long processId, long lockId,String resource, ResponseCode code) {
        this.operate = operate;
        this.processId = processId;
        this.lockId = lockId;
        this.code = code;
        this.resource = resource;
    }
}
