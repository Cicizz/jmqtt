package org.jmqtt.remoting.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.NettyConfig;
import org.jmqtt.common.helper.Pair;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.RemotingServer;
import org.jmqtt.remoting.session.WillMessageManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;


public class NettyRemotingServer implements RemotingServer {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING);
    private NettyConfig nettyConfig;
    private EventLoopGroup selectorGroup;
    private EventLoopGroup ioGroup;
    private Class<? extends ServerChannel> clazz;
    private Map<Message.Type, Pair<RequestProcessor, ExecutorService>> processorTable;

    public NettyRemotingServer(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
        this.processorTable = new HashMap();

        if(!nettyConfig.isUseEpoll()){
            selectorGroup = new NioEventLoopGroup(nettyConfig.getIoThreadNum(),
                    new ThreadFactoryImpl("SelectorEventGroup"));
            ioGroup = new NioEventLoopGroup(nettyConfig.getIoThreadNum(),
                    new ThreadFactoryImpl("IOEventGroup"));
            clazz = NioServerSocketChannel.class;
        }else{
            selectorGroup = new EpollEventLoopGroup(nettyConfig.getIoThreadNum(),
                    new ThreadFactoryImpl("SelectorEventGroup"));
            ioGroup = new EpollEventLoopGroup(nettyConfig.getIoThreadNum(),
                    new ThreadFactoryImpl("IOEventGroup"));
            clazz = EpollServerSocketChannel.class;
        }
    }


    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(selectorGroup)
                .group(ioGroup)
                .channel(clazz)
                .option(ChannelOption.SO_BACKLOG, nettyConfig.getTcpBackLog())
                .option(ChannelOption.TCP_NODELAY, nettyConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, nettyConfig.getTcpSndBuf())
                .option(ChannelOption.SO_RCVBUF, nettyConfig.getTcpRcvBuf())
                .option(ChannelOption.SO_REUSEADDR, nettyConfig.isTcpReuseAddr())
                .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isTcpKeepAlive())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("mqttEncoder", MqttEncoder.INSTANCE)
                                .addLast("mqttDecoder", new MqttDecoder(nettyConfig.getMaxMsgSize()))
                                .addLast("idleStateHandler", new IdleStateHandler(0, 0, 60))
                                .addLast("nettyConnectionManager", new NettyConnectManager())
                                .addLast("nettyMqttHandler", new NettyMqttHandler());
                    }
                });
        if(nettyConfig.isPooledByteBufAllocatorEnable()){
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            ChannelFuture future = bootstrap.bind(nettyConfig.getTcpPort()).sync();
            future.channel().closeFuture().sync();
        }catch (InterruptedException ex){
            log.error("Start tcp server failure.cause={}",ex);
        }
    }


    @Override
    public void shutdown() {
        if (selectorGroup != null) {
            selectorGroup.shutdownGracefully();
        }
        if (ioGroup != null) {
            ioGroup.shutdownGracefully();
        }
    }

    public void registerProcessor(Message.Type mqttType,RequestProcessor processor,ExecutorService executorService){
        this.processorTable.put(mqttType,new Pair<>(processor,executorService));
    }

    class NettyMqttHandler extends SimpleChannelInboundHandler<MqttMessage> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MqttMessage mqttMessage) throws Exception {
            if(mqttMessage != null && mqttMessage.decoderResult().isSuccess()){
                Message message = MessageUtil.getMessage(mqttMessage);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        processorTable.get(message.getType()).getObject1().processRequest(ctx,message);
                    }
                };
                try{
                    processorTable.get(message.getType()).getObject2().submit(runnable);
                }catch (RejectedExecutionException ex){
                    log.warn("Reject mqtt request,cause={}",ex.getMessage());
                }
            }else{
                ctx.close();
            }
        }

    }

    class NettyConnectManager extends ChannelDuplexHandler {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
            log.info("[channelInactive] -> addr = {}",remoteAddr);
            String clientId = NettyUtil.getClientId(ctx.channel());
            WillMessageManager.getInstance().pubWillMessage(clientId);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateHandler){
                IdleStateEvent event = (IdleStateEvent) evt;
                if(event.state().equals(IdleState.ALL_IDLE)){
                    final String remoteAddr = RemotingHelper.getRemoteAddr(ctx.channel());
                    log.warn("[HEART_BEAT] -> IDLE exception, addr = {}",remoteAddr);
                    RemotingHelper.closeChannel(ctx.channel());
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.warn("Channel caught Exception remotingAddr = {}", RemotingHelper.getRemoteAddr(ctx.channel()));
            log.warn("Channel caught Exception,cause = {}", cause);
            RemotingHelper.closeChannel(ctx.channel());
        }
    }
}
