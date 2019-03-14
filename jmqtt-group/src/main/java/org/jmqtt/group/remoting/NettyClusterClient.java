package org.jmqtt.group.remoting;

import io.netty.bootstrap.Bootstrap;
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
import org.jmqtt.group.ClusterClient;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.remoting.codec.NettyClusterDecoder;
import org.jmqtt.group.remoting.codec.NettyClusterEncoder;
import org.jmqtt.remoting.netty.NettyConnectHandler;
import org.jmqtt.remoting.netty.NettyEventExcutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClusterClient extends AbstractNettyClusterClient implements ClusterClient {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private ClusterConfig clusterConfig;
    private EventLoopGroup ioGroup;
    private Class<? extends ServerChannel> clazz;
    private NettyEventExcutor nettyEventExcutor;
    private Bootstrap bootstrap;

    public NettyClusterClient(ClusterConfig clusterConfig){
        this.clusterConfig = clusterConfig;
        if(!clusterConfig.isGroupUseEpoll()){
            ioGroup = new NioEventLoopGroup(clusterConfig.getGroupIoThreadNum(),
                    new ThreadFactoryImpl("GroupClientWorkEventGroup"));
            clazz = NioServerSocketChannel.class;
        }else{
            ioGroup = new EpollEventLoopGroup(clusterConfig.getGroupIoThreadNum(),
                    new ThreadFactoryImpl("GroupClientWorkEventGroup"));
            clazz = EpollServerSocketChannel.class;
        }
        this.nettyEventExcutor = new NettyEventExcutor(new ClusterServerChannelEventListener());
        this.bootstrap = new Bootstrap();
    }


    @Override
    public void start() {
        this.bootstrap.group(ioGroup)
                .channel(clazz)
                .option(ChannelOption.SO_BACKLOG, clusterConfig.getGroupTcpBackLog())
                .option(ChannelOption.SO_SNDBUF, clusterConfig.getGroupTcpSndBuf())
                .option(ChannelOption.SO_RCVBUF, clusterConfig.getGroupTcpRcvBuf())
                .option(ChannelOption.SO_REUSEADDR, clusterConfig.isGroupTcpReuseAddr())
                .option(ChannelOption.SO_KEEPALIVE, clusterConfig.isGroupTcpKeepAlive())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("groupIdleStateHandler", new IdleStateHandler(0, 0, 60))
                                .addLast("groupEncoder",new NettyClusterEncoder())
                                .addLast("groupDecoder",new NettyClusterDecoder())
                                .addLast("nettyConnectionManager", new NettyConnectHandler(nettyEventExcutor))
                                .addLast("groupHandler", new NettyClientHandler());
                    }
                });
        if(clusterConfig.isGroupPooledByteBufAllocatorEnable()){
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        log.info("init cluster client success");
    }

    @Override
    public void shutdown() {
        if(ioGroup != null ){
            ioGroup.shutdownGracefully();
        }
        log.info("shutdown cluster client success");
    }

    @Override
    public Bootstrap getBootstrap() {
        return this.bootstrap;
    }

    private class NettyClientHandler extends SimpleChannelInboundHandler<ClusterRemotingCommand>{
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClusterRemotingCommand cmd) throws Exception {
            processMessageReceived(channelHandlerContext,cmd);
        }
    }
}
