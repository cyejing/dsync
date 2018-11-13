package cn.cyejing.dsync.common.handler;

import cn.cyejing.dsync.common.model.Response;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Born
 */
@Slf4j
public class ResponseMessageToMessage extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List out) throws Exception {
        Response o = JSON.parseObject(ByteBufUtil.getBytes(msg), Response.class);
        log.debug("receive response:{}", o);
        out.add(o);
    }
}
