package org.jmqtt.broker.remoting.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyConnectHandler extends ChannelDuplexHandler {

    private static final Logger log = JmqttLogger.remotingLog;

    private NettyEventExecutor eventExecutor;

    public NettyConnectHandler(NettyEventExecutor nettyEventExecutor){
        this.eventExecutor = nettyEventExecutor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
        LogUtil.debug(log,"[ChannelActive] -> addr = {}",remoteAddr);
        this.eventExecutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.CONNECT,ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
        LogUtil.debug(log,"[ChannelInactive] -> addr = {}",remoteAddr);
        this.eventExecutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.CLOSE,ctx.channel()));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
                LogUtil.warn(log,"[HEART_BEAT] -> IDLE exception, addr = {}",remoteAddr);
                RemotingHelper.closeChannel(ctx.channel());
                this.eventExecutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.IDLE,ctx.channel()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
        LogUtil.warn(log,"Channel caught Exception remotingAddr:{},cause:{}", remoteAddr,cause);
        RemotingHelper.closeChannel(ctx.channel());
        this.eventExecutor.putNettyEvent(new NettyEvent(remoteAddr,NettyEventType.EXCEPTION,ctx.channel()));
    }
}
