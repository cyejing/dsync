package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;

import cn.cyejing.dsync.toolkit.exception.LockTimeoutException;
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
            String msg = "wait lock:[" + request + "] timeout:" + duration.getSeconds() + "s";
            log.error(msg);
            throw new LockTimeoutException(msg);
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
