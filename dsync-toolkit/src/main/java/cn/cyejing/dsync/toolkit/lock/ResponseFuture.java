package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Born
 */
@Slf4j
public class ResponseFuture {

    private final Request request;

    private final CountDownLatch latch = new CountDownLatch(1);
    private Response response;

    public ResponseFuture(Request request) {
        this.request = request;
    }

    public Response get() throws InterruptedException {
        return get(Duration.ofSeconds(10));//TODO config
    }

    public Response get(Duration duration) throws InterruptedException {
        boolean await = latch.await(duration.toNanos(), TimeUnit.NANOSECONDS);
        if (await == false) {
            log.error("wait lock timeout");
            throw new RuntimeException("wait lock timeout!");
        }
        return response;
    }

    void haveResponse(Response response) {
        this.response = response;
        latch.countDown();
    }

    public void inactive() {
        latch.countDown();
    }
}
