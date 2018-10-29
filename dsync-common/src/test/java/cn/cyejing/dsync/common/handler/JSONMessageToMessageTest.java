package cn.cyejing.dsync.common.handler;

import static org.junit.Assert.*;

import cn.cyejing.dsync.common.model.Steps;
import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.util.CharsetUtil;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.junit.Test;

public class JSONMessageToMessageTest {

    @Test
    public void decode() {


        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(
                new JsonObjectDecoder(),
                new ProtocolMessageToMessage()
        );

        Request p1 = new Request(1L, 1L, Steps.Lock, "key", "data");
        String s = JSON.toJSONString(p1);
        System.out.println(s);
//        channel.writeInbound(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
        ByteBuf byteBuf = Unpooled.copiedBuffer(s, CharsetUtil.UTF_8);
        byteBuf.writeBytes(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
        channel.writeInbound(byteBuf.readBytes(10));
        channel.writeInbound(byteBuf.readBytes(10));
        channel.writeInbound(byteBuf);
        channel.finish();

        channel.readInbound();
        Request p = channel.readInbound();
        assertNotNull(p);
        assertEquals(p1.getOperate(),p.getOperate());
        assertEquals(p1.getResource(),p.getResource());
        assertEquals(p1.getData(),p.getData());

    }
}
