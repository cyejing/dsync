package cn.cyejing.dsync.dominate.handler;

import cn.cyejing.dsync.common.handler.ProtocolMessageToMessage;
import cn.cyejing.dsync.dominate.domain.LockCarrier;
import cn.cyejing.dsync.dominate.interceptor.TraceLockInterceptor;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Born
 */
public final class ServerChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new LoggingHandler(LogLevel.DEBUG),
                new JsonObjectDecoder(),
                new StringEncoder(),
                new ProtocolMessageToMessage(),
                new LockServerHandler()
        );
    }
}
