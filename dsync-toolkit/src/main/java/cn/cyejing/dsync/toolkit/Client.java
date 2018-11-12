package cn.cyejing.dsync.toolkit;

import cn.cyejing.dsync.common.handler.ResponseMessageToMessage;
import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;
import cn.cyejing.dsync.common.model.Steps;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-12 15:52
 **/
@Slf4j
public class Client {

    private String host;
    private int port;

    private EventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap;
    private Channel channel;



    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        bootstrap = new Bootstrap().group(group)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
//                                    new LoggingHandler(LogLevel.INFO),
                                new IdleStateHandler(0, 0, 5),
                                new StringEncoder(),
                                new JsonObjectDecoder(),
                                new ResponseMessageToMessage(),
                                new LockHandler());

                    }
                });
        doConnect();
    }


    protected void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        log.info("Connect to server: {}:{}",host,port);
        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener((ChannelFutureListener) futureListener -> {
            if (futureListener.isSuccess()) {
                channel = futureListener.channel();
                Request define = new Request(-1L, -1L, Steps.Connect, null);
                channel.writeAndFlush(JSON.toJSONString(define));
                log.info("Connect to server successfully!");
            } else {
                log.warn("Failed to connect to server, try connect after 10s");
                futureListener.channel().eventLoop().schedule(() -> doConnect(), 10, TimeUnit.SECONDS);
            }
        });
    }

    public void request(Request request) {
        channel.writeAndFlush(JSON.toJSONString(request));
    }


    class LockHandler extends SimpleChannelInboundHandler<Response> {

        private DLock lock = DLock.getInstance();

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
