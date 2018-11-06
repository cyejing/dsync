package cn.cyejing.dsync.dominate.handler;

import cn.cyejing.dsync.dominate.domain.LockCarrier;
import cn.cyejing.dsync.dominate.domain.Operate;
import cn.cyejing.dsync.dominate.domain.Process;
import cn.cyejing.dsync.common.model.Request;
import cn.cyejing.dsync.common.model.Response;
import cn.cyejing.dsync.common.model.ResponseCode;
import cn.cyejing.dsync.common.model.Steps;
import cn.cyejing.dsync.dominate.domain.ProcessCarrier;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Born
 * @Create: 2018-10-16 16:36
 **/
@Slf4j
public class LockServerHandler extends SimpleChannelInboundHandler<Request> {


    private LockCarrier lockCarrier = LockCarrier.getInstance();
    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request req) throws Exception {
        switch (req.getOperate()) {
            case Connect: {
                Channel channel = ctx.channel();
                Process process = new Process(channel);
                processCarrier.addProcess(process);
                Response response = new Response(Steps.Connect, process.getProcessId(), -1L, null, ResponseCode.Ok);
                channel.writeAndFlush(JSON.toJSONString(response));
                break;
            }
            case Lock: {
                Channel channel = ctx.channel();
                Operate operate = new Operate(req.getProcessId(), req.getLockId(), req.getResource(), channel);
                if (lockCarrier.tryLock(operate)) {
                    writeUnlock(operate);
                }
                break;
            }
            case Unlock: {
                Channel channel = ctx.channel();
                Operate operate = new Operate(req.getProcessId(), req.getLockId(), req.getResource(), channel);
                Operate nextOperate = lockCarrier.unLock(operate);
                writeUnlock(nextOperate);
                break;
            }
            case Close:
                break;
            default: {
                log.debug("ignore unknown operate:{}", req.getOperate());
                Response response = new Response(req.getOperate(), -1L, -1L, null, ResponseCode.Fail);
                response.setMessage("unknown the operate:" + req.getOperate());
                ctx.writeAndFlush(JSON.toJSONString(response));
            }
        }
    }

    private void writeUnlock(Operate nextOperate) {
        log.debug("writeUnlock next operate:{}", nextOperate);
        if (nextOperate != null) {
            Channel nextOperatorChannel = nextOperate.getChannel();
            Response response = new Response(Steps.Unlock, nextOperate.getProcessId(), nextOperate.getLockId(),
                    nextOperate.getResource(), ResponseCode.Ok);
            nextOperatorChannel.writeAndFlush(JSON.toJSONString(response));
        }
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        log.debug("channelUnregistered:{}", ctx.channel());
        Channel channel = ctx.channel();
        Process process = processCarrier.get(channel);
        List<Operate> operates = lockCarrier.processDown(process);
        operates.forEach(o -> {
            writeUnlock(o);
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.debug("channelActive:{}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.debug("channelInactive:{}", ctx.channel());

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        log.debug("channelReadComplete:{}", ctx.channel());

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            return;
        }
        log.debug("userEventTriggered:{},event:{}", ctx.channel(), evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        log.debug("channelWritabilityChanged:{}", ctx.channel());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.debug("exceptionCaught:{}", ctx.channel(), cause);

    }
}
