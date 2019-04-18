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
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Born
 */
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
            case TryLock:{
                Channel channel = ctx.channel();
                Operate operate = new Operate(req.getProcessId(), req.getLockId(), req.getResource(), channel);
                boolean tryAcquire = lockCarrier.tryAcquire(operate);
                Response response = new Response(Steps.TryLock, operate.getProcessId(), operate.getLockId(),
                        operate.getResource(), tryAcquire ? ResponseCode.Ok : ResponseCode.Fail);
                channel.writeAndFlush(JSON.toJSONString(response));
                break;
            }
            case Lock: {
                Channel channel = ctx.channel();
                Operate operate = new Operate(req.getProcessId(), req.getLockId(), req.getResource(), channel);
                if (lockCarrier.acquire(operate)) {
                    writeUnlock(operate);
                }
                break;
            }
            case Unlock: {
                Channel channel = ctx.channel();
                Operate operate = new Operate(req.getProcessId(), req.getLockId(), req.getResource(), channel);
                Operate nextOperate = lockCarrier.release(operate);
                writeUnlock(nextOperate);
                break;
            }
            case Close:
                break;
            default: {
                log.info("ignore unknown operate:{}", req.getOperate());
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
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelRegistered:{}", ctx.channel());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channelUnregistered:{}", ctx.channel());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive:{}", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("channelInactive:{}", ctx.channel());
        Channel channel = ctx.channel();
        Process process = processCarrier.get(channel);//TODO null
        List<Operate> operates = lockCarrier.processRelease(process);
        operates.forEach(o -> writeUnlock(o));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        log.info("userEventTriggered:{},event:{}", ctx.channel(), evt);
        if (evt instanceof IdleStateEvent) {
            return;
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("exceptionCaught:{}", ctx.channel(), cause);
    }
}
