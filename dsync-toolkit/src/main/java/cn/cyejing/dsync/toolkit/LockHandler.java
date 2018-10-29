package cn.cyejing.dsync.toolkit;

import cn.cyejing.dsync.common.model.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-17 18:57
 **/
@Slf4j
public class LockHandler extends SimpleChannelInboundHandler<Response> {

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
}
