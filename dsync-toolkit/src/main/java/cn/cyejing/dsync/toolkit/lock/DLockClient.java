package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;
import cn.cyejing.dsync.toolkit.AbstractClient;
import cn.cyejing.dsync.toolkit.Config;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Born
 */
@Slf4j
public class DLockClient extends AbstractClient {

    private DLockImpl lock;
    private volatile ConcurrentHashMap<Long, ResponseFuture> responseMap = new ConcurrentHashMap<>();

    public DLockClient(String host, int port, DLockImpl lock) {
        super(host, port);
        this.lock = lock;
    }

    public void connect() {
        bootstrap();
        doConnect();
    }

    public DLockClient(Config config, DLockImpl lock) {
        this(config.getHost(), config.getPort(), lock);
    }

    public ResponseFuture request(Request request) {
        long lockId = request.getLockId();
        ResponseFuture responseFuture = new ResponseFuture(request);
        responseMap.put(lockId, responseFuture);
        channel.writeAndFlush(JSON.toJSONString(request));
        return responseFuture;
    }

    public void unlock(Request request) {
        channel.writeAndFlush(JSON.toJSONString(request));
    }
    @Override
    protected void initSocketChannel(SocketChannel channel) {
        channel.pipeline().addLast(new LockHandler(lock));
    }

    class LockHandler extends SimpleChannelInboundHandler<Response> {

        private DLockImpl lock;

        public LockHandler(DLockImpl lock) {
            this.lock = lock;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Response res) throws Exception {
            switch (res.getOperate()) {
                case Connect: {
                    lock.revisionProcessId(res.getProcessId());
                    break;
                }
                case Unlock: {
                    ResponseFuture responseFuture = responseMap.remove(res.getLockId());
                    responseFuture.haveResponse(res);
                    break;
                }
                case TryLock:{
                    ResponseFuture responseFuture = responseMap.remove(res.getLockId());
                    responseFuture.haveResponse(res);
                    break;
                }
                default: {
                    log.debug("ignore unknown operate:{}", res.getOperate());
                }
            }
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.info("channelRegistered:{}", ctx.channel());
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.info("channelUnregistered:{}", ctx.channel());
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("channelActive:{}", ctx.channel());
            super.channelActive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            log.info("userEventTriggered:{}", evt);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("channelInactive:{}", ctx.channel());
            super.channelInactive(ctx);
            lock.serverBreak();
            responseMap.values().forEach(v->{
                v.inactive();
            });
            doConnect();
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.error("exceptionCaught", cause);
        }
    }

}
