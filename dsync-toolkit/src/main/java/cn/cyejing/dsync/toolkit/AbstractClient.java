package cn.cyejing.dsync.toolkit;

import cn.cyejing.dsync.common.handler.ResponseMessageToMessage;
import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Steps;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Born
 */
@Slf4j
public abstract class AbstractClient {

    protected String host;
    protected int port;

    protected EventLoopGroup group;
    protected Bootstrap bootstrap;
    protected Channel channel;

    public AbstractClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    protected void bootstrap() {
        group = new NioEventLoopGroup(10,new DefaultThreadFactory("dsync-client"));
        bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new LoggingHandler(LogLevel.DEBUG),
                                new StringEncoder(),
                                new JsonObjectDecoder(),
                                new ResponseMessageToMessage());
                        initSocketChannel(ch);
                    }
                });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(), "DSync-JVM-shutdown-hook"));
    }

    public void shutdown() {
        group.shutdownGracefully();
    }


    public AbstractClient(Config config) {
        this(config.getHost(), config.getPort());
    }

    protected void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        log.info("Connect to server: {}:{}", host, port);
        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener((ChannelFutureListener) futureListener -> {
            if (futureListener.isSuccess()) {
                channel = futureListener.channel();
                Request define = new Request(-1L, -1L, Steps.Connect, null);
                channel.writeAndFlush(JSON.toJSONString(define));
                log.info("Connect to server successfully!");
            } else {
                log.warn("Failed to connect to server, try connect after 3s");
                futureListener.channel().eventLoop().schedule(() -> doConnect(), 3, TimeUnit.SECONDS);
            }
        });
    }

    protected abstract void initSocketChannel(SocketChannel channel);

}
