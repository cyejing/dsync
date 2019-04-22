package cn.cyejing.dsync.dominate;

import cn.cyejing.dsync.dominate.domain.LockCarrier;
import cn.cyejing.dsync.dominate.domain.ProcessCarrier;
import cn.cyejing.dsync.dominate.handler.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Born
 */
@Slf4j
public class LockServer {

    EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(5);
    EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup(10,new DefaultThreadFactory("dsync-server"));

    public static void main(String[] args) throws InterruptedException {
        int port = 4843;
        List<String> argList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                argList.add(args[i]);
            }
        }
        if (argList.size() > 0) {
            port = Integer.parseInt(argList.get(0));
        }
        LockServer lockServer = new LockServer();
        lockServer.start(port);
    }

    public void start(int port) throws InterruptedException {

        try {
            Channel channel = new ServerBootstrap()
                    .group(bossEventLoopGroup, workerEventLoopGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerChannelInitializer())
                    .bind(port).sync().channel();

            channel.closeFuture().sync();
        } finally {
          shutdown();
        }
    }

    public void shutdown() {
        bossEventLoopGroup.shutdownGracefully();
        workerEventLoopGroup.shutdownGracefully();
        LockCarrier.getInstance().clear();
        ProcessCarrier.getInstance().clear();
    }
}
