package cn.cyejing.dsync.dominate.handler;

import cn.cyejing.dsync.common.handler.ProtocolMessageToMessage;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-16 16:24
 **/
public final class ServerChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
//                new LoggingHandler(LogLevel.DEBUG),
                new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS),
                new JsonObjectDecoder(),
                new StringEncoder(),
                new ProtocolMessageToMessage(),
                new ProtocolHandler()
        );
    }
}
