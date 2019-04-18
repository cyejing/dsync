package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Born
 */
public class ResponseFuture {


    private final Long lockId;

    private final CountDownLatch latch = new CountDownLatch(1);
    private Response response;

    public ResponseFuture(Long lockId) {
        this.lockId = lockId;
    }

    public Response get() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS); //TODO config
        return response;
    }

    void haveResponse(Response response) {
        this.response = response;
        latch.countDown();
    }

}
