package org.jmqtt.group.remoting;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.common.config.ClusterConfig;
import org.jmqtt.common.helper.MixAll;
import org.jmqtt.common.helper.ThreadFactoryImpl;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.group.ClusterRemotingClient;
import org.jmqtt.group.common.InvokeCallback;
import org.jmqtt.group.protocol.ClusterRemotingCommand;
import org.jmqtt.group.protocol.MessageFlag;
import org.jmqtt.group.remoting.codec.NettyClusterDecoder;
import org.jmqtt.group.remoting.codec.NettyClusterEncoder;
import org.jmqtt.remoting.exception.RemotingConnectException;
import org.jmqtt.remoting.exception.RemotingSendRequestException;
import org.jmqtt.remoting.netty.NettyConnectHandler;
import org.jmqtt.remoting.netty.NettyEventExcutor;
import org.jmqtt.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NettyClusterRemotingClient extends AbstractNettyCluster implements ClusterRemotingClient {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    private ClusterConfig clusterConfig;
    private EventLoopGroup ioGroup;
    private Class<? extends SocketChannel> clazz;
    private NettyEventExcutor nettyEventExcutor;
    private Bootstrap bootstrap;
    private Lock lockChannelTable = new ReentrantLock();
    private ScheduledExecutorService schudure = new ScheduledThreadPoolExecutor(1,new ThreadFactoryImpl("ScanResponseTableThread"));
    private final int LOCK_CHANNEL_TIMEOUT = 3000;
    private final ConcurrentMap<String,Channel> channelTable = new ConcurrentHashMap<>();

    public NettyClusterRemotingClient(ClusterConfig clusterConfig){
        this.clusterConfig = clusterConfig;
        if(!clusterConfig.isGroupUseEpoll()){
            ioGroup = new NioEventLoopGroup(clusterConfig.getGroupIoThreadNum(),
                    new ThreadFactoryImpl("GroupClientWorkEventGroup"));
            clazz = NioSocketChannel.class;
        }else{
            ioGroup = new EpollEventLoopGroup(clusterConfig.getGroupIoThreadNum(),
                    new ThreadFactoryImpl("GroupClientWorkEventGroup"));
            clazz = EpollSocketChannel.class;
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // connect timeout
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
        this.resendService.start();
        log.info("init cluster client success");
        schudure.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                NettyClusterRemotingClient.this.scanResponseTable();
            }
        },3000,1000,TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        if(ioGroup != null ){
            ioGroup.shutdownGracefully();
        }
        this.schudure.shutdown();
        this.resendService.shutdown();
        log.info("shutdown cluster client success");
    }

    @Override
    public void invokeAsync(String addr, ClusterRemotingCommand command, long timeoutMills, InvokeCallback callback) throws RemotingSendRequestException, RemotingConnectException {
        final Channel channel = getOrCreateChannel(addr);
        if(channel != null){
            byte[] body = command.getBody();
            try{
                if(body.length > clusterConfig.getCompressMaxSize()){
                    body = MixAll.compress(body,5);
                    command.setBody(body);
                    command.setFlag(MessageFlag.COMPRESSED_FLAG);
                }
                this.invokeAsyncImpl(channel,command,timeoutMills,callback);
            }catch(Exception ex){
                throw new RemotingSendRequestException(ex.getMessage());
            }
        }else{
            log.warn("connect this addr failure,addr={}",addr);
            throw new RemotingConnectException(addr);
        }
    }


    private Channel getOrCreateChannel(String addr){
        Channel channel = this.channelTable.get(addr);
        if(channel != null && channel.isActive()){
            return channel;
        }
        return createChannel(addr);
    }

    private Channel createChannel(String addr){
        // new connect
        try {
            if(lockChannelTable.tryLock(LOCK_CHANNEL_TIMEOUT, TimeUnit.MILLISECONDS)){
                Channel channel = this.channelTable.get(addr);
                if(channel != null){
                    channel.close();
                    this.channelTable.remove(addr);
                }
                ChannelFuture channelFuture = this.bootstrap.connect(RemotingHelper.string2SocketAddress(addr));
                Channel newChannel = channelFuture.channel();
                this.channelTable.put(addr,newChannel);
                return newChannel;
            }else{
                log.warn("try lock channel table to cache channel failure,addr:{}",addr);
            }
        } catch (InterruptedException e) {
            log.warn("create new channel failure,remote address={},ex={}",addr,e);
        }finally {
            lockChannelTable.unlock();
        }
        return null;
    }

    private class NettyClientHandler extends SimpleChannelInboundHandler<ClusterRemotingCommand>{
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClusterRemotingCommand clusterRemotingCommand) throws Exception {
            processMessageReceived(channelHandlerContext, clusterRemotingCommand);
        }
    }
}
