package org.jmqtt.group.remoting;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.common.config.ClusterConfig;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.ClusterRemotingServer;
import org.jmqtt.group.processor.ClusterRequestProcessor;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.remoting.codec.NettyClusterDecoder;
import org.jmqtt.group.remoting.codec.NettyClusterEncoder;
import org.jmqtt.remoting.netty.NettyConnectHandler;
import org.jmqtt.remoting.netty.NettyEventExcutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyClusterRemotingServer extends AbstractNettyCluster implements ClusterRemotingServer {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private ClusterConfig clusterConfig;
    private EventLoopGroup selectorGroup;
    private EventLoopGroup ioGroup;
    private Class<? extends ServerChannel> clazz;
    private NettyEventExcutor nettyEventExcutor;
    private ServerBootstrap serverBootstrap;
    private ScheduledExecutorService schudure = new ScheduledThreadPoolExecutor(1,new ThreadFactoryImpl("ScanResponseTableThread"));


    public NettyClusterRemotingServer(ClusterConfig clusterConfig){
        this.clusterConfig = clusterConfig;
        if(!clusterConfig.isGroupUseEpoll()){
            selectorGroup = new NioEventLoopGroup(clusterConfig.getGroupSelectorThreadNum(),
                    new ThreadFactoryImpl("GroupSelectorEventGroup"));
            ioGroup = new NioEventLoopGroup(clusterConfig.getGroupIoThreadNum(),
                    new ThreadFactoryImpl("GroupIOEventGroup"));
            clazz = NioServerSocketChannel.class;
        }else{
            selectorGroup = new EpollEventLoopGroup(clusterConfig.getGroupSelectorThreadNum(),
                    new ThreadFactoryImpl("GroupSelectorEventGroup"));
            ioGroup = new EpollEventLoopGroup(clusterConfig.getGroupIoThreadNum(),
                    new ThreadFactoryImpl("GroupIOEventGroup"));
            clazz = EpollServerSocketChannel.class;
        }
        this.nettyEventExcutor = new NettyEventExcutor(new ClusterServerChannelEventListener());
        this.serverBootstrap = new ServerBootstrap();
    }


    @Override
    public void start() {
        this.serverBootstrap.group(selectorGroup,ioGroup)
                .channel(clazz)
                .option(ChannelOption.SO_BACKLOG, clusterConfig.getGroupTcpBackLog())
                .childOption(ChannelOption.TCP_NODELAY, clusterConfig.isGroupTcpNoDelay())
                .childOption(ChannelOption.SO_SNDBUF, clusterConfig.getGroupTcpSndBuf())
                .option(ChannelOption.SO_RCVBUF, clusterConfig.getGroupTcpRcvBuf())
                .option(ChannelOption.SO_REUSEADDR, clusterConfig.isGroupTcpReuseAddr())
                .childOption(ChannelOption.SO_KEEPALIVE, clusterConfig.isGroupTcpKeepAlive())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("groupEncoder",new NettyClusterEncoder())
                                .addLast("groupDecoder",new NettyClusterDecoder())
                                .addLast("groupIdleStateHandler", new IdleStateHandler(0, 0, 60))
                                .addLast("nettyConnectionManager", new NettyConnectHandler(nettyEventExcutor))
                                .addLast("groupServerHandler", new NettyServerHandler());
                    }
                });
        if(clusterConfig.isGroupPooledByteBufAllocatorEnable()){
            this.serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        this.resendService.start();
        try {
            ChannelFuture future = this.serverBootstrap.bind(clusterConfig.getGroupServerPort()).sync();
            log.info("Start cluster server success,port = {}", clusterConfig.getGroupServerPort());
        }catch (InterruptedException ex){
            log.error("Start cluster server failure.cause={}",ex);
        }

        schudure.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                NettyClusterRemotingServer.this.scanResponseTable();
            }
        },3000,1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        if(selectorGroup != null){
            selectorGroup.shutdownGracefully();
        }
        if(ioGroup != null ){
            ioGroup.shutdownGracefully();
        }
        this.schudure.shutdown();
        this.resendService.shutdown();
        log.info("shutdown cluster server success");
    }

    public void registerClusterProcessor(int code, ClusterRequestProcessor processor, ExecutorService executorService){
        registerProcessor(code,processor,executorService);
    }

    private class NettyServerHandler extends SimpleChannelInboundHandler<ClusterRemotingCommand>{

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClusterRemotingCommand clusterRemotingCommand) throws Exception {
            processMessageReceived(channelHandlerContext,clusterRemotingCommand);
        }
    }

}
