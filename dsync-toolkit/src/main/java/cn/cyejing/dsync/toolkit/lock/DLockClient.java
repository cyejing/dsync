package cn.cyejing.dsync.toolkit.lock;

import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;
import cn.cyejing.dsync.toolkit.AbstractClient;
import cn.cyejing.dsync.toolkit.Config;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-12 17:37
 **/
@Slf4j
public class DLockClient extends AbstractClient {

    private DLockImpl lock;
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

    public void request(Request request) {
        channel.writeAndFlush(JSON.toJSONString(request));
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return new ChannelHandler[]{new LockHandler(lock)};
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
                    lock.countDown(res.getLockId(), res.getResource());
                    break;
                }
                default: {
                    log.debug("ignore unknown operate:{}", res.getOperate());
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("channelInactive");
            super.channelInactive(ctx);
            lock.serverBreak();
            doConnect();
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.error("exceptionCaught", cause);
        }
    }

}
