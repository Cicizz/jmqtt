package org.jmqtt.mqtt.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.bus.BusController;
import org.jmqtt.mqtt.MQTTConnectionFactory;
import org.jmqtt.mqtt.codec.ByteBuf2WebSocketEncoder;
import org.jmqtt.mqtt.codec.WebSocket2ByteBufDecoder;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.config.NettyConfig;
import org.jmqtt.support.helper.MixAll;
import org.jmqtt.support.helper.ThreadFactoryImpl;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.jmqtt.support.remoting.RemotingService;
import org.slf4j.Logger;



public class MqttRemotingServer implements RemotingService {

    int coreThreadNum = Runtime.getRuntime().availableProcessors();
    private static final Logger                         log = JmqttLogger.remotingLog;
    private              NettyConfig                    nettyConfig;
    private              EventLoopGroup                 selectorGroup;
    private              EventLoopGroup                 ioGroup;
    private              Class<? extends ServerChannel> clazz;
    private              BrokerConfig                   brokerConfig;
    private              MQTTConnectionFactory          mqttConnectionFactory;


    public MqttRemotingServer(BrokerConfig brokerConfig, NettyConfig nettyConfig, BusController busController) {
        this.nettyConfig = nettyConfig;
        this.brokerConfig = brokerConfig;
        this.mqttConnectionFactory = new MQTTConnectionFactory(brokerConfig,busController);

        if (!nettyConfig.getUseEpoll()) {
            selectorGroup = new NioEventLoopGroup(coreThreadNum,
                    new ThreadFactoryImpl("SelectorEventGroup"));
            ioGroup = new NioEventLoopGroup(coreThreadNum * 2,
                    new ThreadFactoryImpl("IOEventGroup"));
            clazz = NioServerSocketChannel.class;
        } else {
            selectorGroup = new EpollEventLoopGroup(coreThreadNum,
                    new ThreadFactoryImpl("SelectorEventGroup"));
            ioGroup = new EpollEventLoopGroup(coreThreadNum * 2,
                    new ThreadFactoryImpl("IOEventGroup"));
            clazz = EpollServerSocketChannel.class;
        }
    }


    @Override
    public void start() {
        // start TCP server
        if (nettyConfig.getStartTcp()) {
            startTcpServer(false, nettyConfig.getTcpPort());
        }

        if (nettyConfig.getStartSslTcp()) {
            startTcpServer(true, nettyConfig.getSslTcpPort());
        }

        // start Websocket server
        if (nettyConfig.getStartWebsocket()) {
            startWebsocketServer(false, nettyConfig.getWebsocketPort());
        }

        if (nettyConfig.getStartSslWebsocket()) {
            startWebsocketServer(true, nettyConfig.getSslWebsocketPort());
        }
    }

    private void startWebsocketServer(boolean useSsl, Integer port) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(selectorGroup, ioGroup)
                .channel(clazz)
                .option(ChannelOption.SO_BACKLOG, nettyConfig.getTcpBackLog())
                .childOption(ChannelOption.TCP_NODELAY, nettyConfig.getTcpNoDelay())
                .childOption(ChannelOption.SO_SNDBUF, nettyConfig.getTcpSndBuf())
                .option(ChannelOption.SO_RCVBUF, nettyConfig.getTcpRcvBuf())
                .option(ChannelOption.SO_REUSEADDR, nettyConfig.getTcpReuseAddr())
                .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.getTcpKeepAlive())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (useSsl) {
                            pipeline.addLast("ssl", NettySslHandler.getSslHandler(
                                    socketChannel,
                                    nettyConfig.getUseClientCA(),
                                    nettyConfig.getSslKeyStoreType(),
                                    brokerConfig.getJmqttHome() + nettyConfig.getSslKeyFilePath(),
                                    nettyConfig.getSslManagerPwd(),
                                    nettyConfig.getSslStorePwd()
                            ));
                        }
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, 60))
                                .addLast("httpCodec", new HttpServerCodec())
                                .addLast("aggregator", new HttpObjectAggregator(65535))
                                .addLast("compressor ", new HttpContentCompressor())
                                .addLast("webSocketHandler", new WebSocketServerProtocolHandler("/mqtt", MixAll.MQTT_VERSION_SUPPORT, false,65536))
                                .addLast("webSocket2ByteBufDecoder", new WebSocket2ByteBufDecoder())
                                .addLast("byteBuf2WebSocketEncoder", new ByteBuf2WebSocketEncoder())
                                .addLast("mqttDecoder", new MqttDecoder(nettyConfig.getMaxMsgSize()))
                                .addLast("mqttEncoder", MqttEncoder.INSTANCE)
                                .addLast("nettyMqttHandler", new NettyMqttHandler(mqttConnectionFactory));
                    }
                });
        if (nettyConfig.getPooledByteBufAllocatorEnable()) {
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            LogUtil.info(log,"Start webSocket server {}  success,port = {}", useSsl ? "with ssl" : "", port);
        } catch (InterruptedException ex) {
            LogUtil.error(log,"Start webSocket server {} failure.cause={}", useSsl ? "with ssl" : "", ex);
        }
    }

    private void startTcpServer(boolean useSsl, Integer port) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(selectorGroup, ioGroup)
                .channel(clazz)
                .option(ChannelOption.SO_BACKLOG, nettyConfig.getTcpBackLog())
                .childOption(ChannelOption.TCP_NODELAY, nettyConfig.getTcpNoDelay())
                .childOption(ChannelOption.SO_SNDBUF, nettyConfig.getTcpSndBuf())
                .option(ChannelOption.SO_RCVBUF, nettyConfig.getTcpRcvBuf())
                .option(ChannelOption.SO_REUSEADDR, nettyConfig.getTcpReuseAddr())
                .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.getTcpKeepAlive())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (useSsl) {
                            pipeline.addLast("ssl", NettySslHandler.getSslHandler(
                                    socketChannel,
                                    nettyConfig.getUseClientCA(),
                                    nettyConfig.getSslKeyStoreType(),
                                    brokerConfig.getJmqttHome() + nettyConfig.getSslKeyFilePath(),
                                    nettyConfig.getSslManagerPwd(),
                                    nettyConfig.getSslStorePwd()
                            ));
                        }
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0))
                                .addLast("mqttEncoder", MqttEncoder.INSTANCE)
                                .addLast("mqttDecoder", new MqttDecoder(nettyConfig.getMaxMsgSize()))
                                .addLast("nettyMqttHandler", new NettyMqttHandler(mqttConnectionFactory));
                    }
                });
        if (nettyConfig.getPooledByteBufAllocatorEnable()) {
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            LogUtil.info(log,"Start tcp server {} success,port = {}", useSsl ? "with ssl" : "", port);
        } catch (InterruptedException ex) {
            LogUtil.error(log,"Start tcp server {} failure.cause={}", useSsl ? "with ssl" : "", ex);
        }
    }


    @Override
    public void shutdown() {

        this.mqttConnectionFactory.shutdown();

        if (selectorGroup != null) {
            selectorGroup.shutdownGracefully();
        }
        if (ioGroup != null) {
            ioGroup.shutdownGracefully();
        }
    }
}
