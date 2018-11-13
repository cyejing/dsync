package cn.cyejing.dsync.dominate;

import cn.cyejing.dsync.dominate.handler.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 * @author Born
 */
public class ServerMain {


    public static void main(String[] args) throws InterruptedException {
        int port = 4843;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(5);
        EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup(10);
        try {
            Channel channel = new ServerBootstrap()
                    .group(bossEventLoopGroup, workerEventLoopGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ServerChannelInitializer())
                    .bind(port).sync().channel();
            channel.closeFuture().sync();
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        }
    }
}
