package cn.cyejing.dsync.toolkit;

import cn.cyejing.dsync.common.handler.ResponseMessageToMessage;
import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Steps;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DLock {

    private volatile long processId;
    private Channel channel;
    private ConcurrentHashMap<String, CountDownLatch> localLock = new ConcurrentHashMap<>();

    private ThreadLocal<Request> threadLocal = new ThreadLocal<>();

    private AtomicLong lockIdAdder = new AtomicLong(1);

    private static DLock ourInstance = new DLock();

    private CountDownLatch initProcessLatch = new CountDownLatch(1);

    public static DLock getInstance() {
        return ourInstance;
    }

    private DLock() {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .group(group)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
//                                    new LoggingHandler(LogLevel.DEBUG),
                                    new StringEncoder(),
                                    new JsonObjectDecoder(),
                                    new ResponseMessageToMessage(),
                                    new LockHandler());

                        }
                    })
                    .connect("localhost", 8080)
                    .addListener((ChannelFutureListener) future -> {
                        Channel channel = future.channel();
                        Request define = new Request(-1L, -1L, Steps.Connect, null);
                        channel.writeAndFlush(JSON.toJSONString(define));
                        log.debug("connect to server");
                    });

//            channelFuture.sync();
            channel = channelFuture.channel();

//            Request define = new Request(-1L, -1L, Steps.Connect, null);
//            channel.writeAndFlush(JSON.toJSONString(define));
//            log.debug("connect to server");
        } catch (Exception e) {
            log.error("connect error", e);
        }

    }

    public void revisionProcessId(long serverProcessId) {
        long localProcessId = this.processId;
        if (localProcessId == 0 || serverProcessId < localProcessId) {
            this.processId = serverProcessId;
            initProcessLatch.countDown();
            log.info("revision processId:{}", this.processId);
        }
    }

    public long createLockId() {
        return lockIdAdder.getAndIncrement();
    }

    public void lock(String resource) {
        if (threadLocal.get() != null) {
            return;
        }
        long processId = syncGetProcessId();
        long lockId = createLockId();
        String key = lockId + "-" + resource;
        log.debug("put latch of key:{}", key);
        CountDownLatch latch = localLock.putIfAbsent(key, new CountDownLatch(1));
        if (latch == null) {
            latch = localLock.get(key);
        }
        Request request = new Request(processId, lockId, Steps.Lock, resource);
        threadLocal.set(request);
        channel.writeAndFlush(JSON.toJSONString(request));
        log.debug("lock key:{}", key);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("lock is interrupted:", e);
        }
    }

    private long syncGetProcessId() {
        try {
            if (this.processId == 0) {
                log.error("waiting for server response process  id");
                initProcessLatch.await();
            }
        } catch (InterruptedException e) {
            log.error("waiting for process  is interrupted", e);
        }
        return this.processId;
    }

    public void unLock() {
        Request request = threadLocal.get();
        request.setOperate(Steps.Unlock);
        log.debug("unlock request:{}", request);
        channel.writeAndFlush(JSON.toJSONString(request));
        threadLocal.remove();
    }

    void countDown(long lockId, String resource) {
        String key = lockId + "-" + resource;
        CountDownLatch latch = localLock.get(key);
        log.debug("count down key:{}, count:{}", key, latch);
        if (latch != null) {
            latch.countDown();
            if (latch.getCount() <= 0) {
                localLock.remove(key);
            }
        }
    }

}
