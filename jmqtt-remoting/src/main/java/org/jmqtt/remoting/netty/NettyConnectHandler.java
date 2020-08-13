package org.jmqtt.remoting.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyConnectHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING);

    private NettyEventExcutor eventExcutor;

    public NettyConnectHandler(NettyEventExcutor nettyEventExcutor){
        this.eventExcutor = nettyEventExcutor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
        log.debug("[ChannelActive] -> addr = {}",remoteAddr);
        this.eventExcutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.CONNECT,ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
        log.debug("[ChannelInactive] -> addr = {}",remoteAddr);
        this.eventExcutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.CLOSE,ctx.channel()));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
                log.warn("[HEART_BEAT] -> IDLE exception, addr = {}",remoteAddr);
                RemotingHelper.closeChannel(ctx.channel());
                this.eventExcutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.IDLE,ctx.channel()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
        log.warn("Channel caught Exception remotingAddr:{},cause:{}", remoteAddr,cause);
        RemotingHelper.closeChannel(ctx.channel());
        this.eventExcutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.EXCEPTION,ctx.channel()));
    }
}
