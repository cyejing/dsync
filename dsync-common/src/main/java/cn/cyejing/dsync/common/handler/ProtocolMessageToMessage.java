package cn.cyejing.dsync.common.handler;

import cn.cyejing.dsync.common.model.Request;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-17 15:06
 **/
@Slf4j
public class ProtocolMessageToMessage extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List out) throws Exception {
        Request req = JSON.parseObject(ByteBufUtil.getBytes(msg), Request.class);
        log.debug("receive protocol define:{}", req);
        out.add(req);
    }
}
