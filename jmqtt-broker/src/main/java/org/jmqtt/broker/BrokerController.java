package org.jmqtt.broker;

import io.netty.handler.codec.mqtt.MqttMessageType;
import org.jmqtt.broker.acl.ConnectPermission;
import org.jmqtt.broker.acl.PubSubPermission;
import org.jmqtt.broker.acl.impl.DefaultConnectPermission;
import org.jmqtt.broker.acl.impl.DefaultPubSubPermission;
import org.jmqtt.broker.client.ClientLifeCycleHookService;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.config.NettyConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.helper.RejectHandler;
import org.jmqtt.broker.common.helper.ThreadFactoryImpl;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.dispatcher.DefaultDispatcherMessage;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.processor.protocol.*;
import org.jmqtt.broker.recover.ReSendMessageService;
import org.jmqtt.broker.remoting.netty.ChannelEventListener;
import org.jmqtt.broker.remoting.netty.NettyRemotingServer;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 具体控制类：负责加载配置文件，初始化环境，启动服务等
 */
public class BrokerController {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER);

    private BrokerConfig brokerConfig;
    private NettyConfig nettyConfig;

    private ExecutorService connectExecutor;
    private ExecutorService pubExecutor;
    private ExecutorService subExecutor;
    private ExecutorService pingExecutor;

    private LinkedBlockingQueue<Runnable> connectQueue;
    private LinkedBlockingQueue<Runnable> pubQueue;
    private LinkedBlockingQueue<Runnable> subQueue;
    private LinkedBlockingQueue<Runnable> pingQueue;

    private ChannelEventListener channelEventListener;
    private NettyRemotingServer remotingServer;

    private MessageDispatcher    messageDispatcher;
    private SubscriptionMatcher  subscriptionMatcher;
    private ConnectPermission    connectPermission;
    private PubSubPermission     pubSubPermission;
    private ReSendMessageService reSendMessageService;
    private SessionStore         sessionStore;
    private MessageStore         messageStore;


    public BrokerController(BrokerConfig brokerConfig, NettyConfig nettyConfig) {
        this.brokerConfig = brokerConfig;
        this.nettyConfig = nettyConfig;

        this.connectQueue = new LinkedBlockingQueue<>(100000);
        this.pubQueue = new LinkedBlockingQueue<>(100000);
        this.subQueue = new LinkedBlockingQueue<>(100000);
        this.pingQueue = new LinkedBlockingQueue<>(10000);

        {
            // 会话状态，消息存储加载，可自己实现相关的类
            // TODO 插件化改造，改造成反射加载具体实现类
            //this.sessionStore = new DefaultSessionStore();
            //this.messageStore = new DefaultMessageStore();

        }

        {
            // 设备连接，发布，订阅消息权限控制
            // TODO 插件化改造，反射加载具体实现类
            this.connectPermission = new DefaultConnectPermission();
            this.pubSubPermission = new DefaultPubSubPermission();
        }

        this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
        this.messageDispatcher = new DefaultDispatcherMessage(brokerConfig.getPollThreadNum(), subscriptionMatcher, sessionStore);

        this.channelEventListener = new ClientLifeCycleHookService(messageStore,messageDispatcher);
        this.remotingServer = new NettyRemotingServer(brokerConfig, nettyConfig, channelEventListener);
        this.reSendMessageService = new ReSendMessageService(messageStore, sessionStore);

        int coreThreadNum = Runtime.getRuntime().availableProcessors();
        this.connectExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                connectQueue,
                new ThreadFactoryImpl("ConnectThread"),
                new RejectHandler("connect", 100000));
        this.pubExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                pubQueue,
                new ThreadFactoryImpl("PubThread"),
                new RejectHandler("pub", 100000));
        this.subExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
                coreThreadNum * 2,
                60000,
                TimeUnit.MILLISECONDS,
                subQueue,
                new ThreadFactoryImpl("SubThread"),
                new RejectHandler("sub", 100000));
        this.pingExecutor = new ThreadPoolExecutor(coreThreadNum,
                coreThreadNum,
                60000,
                TimeUnit.MILLISECONDS,
                pingQueue,
                new ThreadFactoryImpl("PingThread"),
                new RejectHandler("heartbeat", 100000));


        {

        }

    }



    public void start() {

        MixAll.printProperties(log, brokerConfig);
        MixAll.printProperties(log, nettyConfig);

        {
            //init and register mqtt protocol processor
            RequestProcessor connectProcessor = new ConnectProcessor(this);
            RequestProcessor disconnectProcessor = new DisconnectProcessor(this);
            RequestProcessor pingProcessor = new PingProcessor();
            RequestProcessor publishProcessor = new PublishProcessor(this);
            RequestProcessor pubRelProcessor = new PubRelProcessor(this);
            RequestProcessor subscribeProcessor = new SubscribeProcessor(this);
            RequestProcessor unSubscribeProcessor = new UnSubscribeProcessor(subscriptionMatcher, sessionStore);
            RequestProcessor pubRecProcessor = new PubRecProcessor(sessionStore);
            RequestProcessor pubAckProcessor = new PubAckProcessor(sessionStore);
            RequestProcessor pubCompProcessor = new PubCompProcessor(sessionStore);

            this.remotingServer.registerProcessor(MqttMessageType.CONNECT, connectProcessor, connectExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.DISCONNECT, disconnectProcessor, connectExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.PINGREQ, pingProcessor, pingExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.PUBLISH, publishProcessor, pubExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.PUBACK, pubAckProcessor, pubExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.PUBREL, pubRelProcessor, pubExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.SUBSCRIBE, subscribeProcessor, subExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.UNSUBSCRIBE, unSubscribeProcessor, subExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.PUBREC, pubRecProcessor, subExecutor);
            this.remotingServer.registerProcessor(MqttMessageType.PUBCOMP, pubCompProcessor, subExecutor);
        }

        if (this.messageDispatcher != null) {
            this.messageDispatcher.start();
        }
        if (this.reSendMessageService != null) {
            this.reSendMessageService.start();
        }
        if (this.remotingServer != null) {
            this.remotingServer.start();
        }
        log.info("JMqtt Server start success and version = {}", brokerConfig.getVersion());
    }

    public void shutdown() {
        if (this.remotingServer != null) {
            this.remotingServer.shutdown();
        }
        if (this.connectExecutor != null) {
            this.connectExecutor.shutdown();
        }
        if (this.pubExecutor != null) {
            this.pubExecutor.shutdown();
        }
        if (this.subExecutor != null) {
            this.subExecutor.shutdown();
        }
        if (this.pingExecutor != null) {
            this.pingExecutor.shutdown();
        }
        if (this.messageDispatcher != null) {
            this.messageDispatcher.shutdown();
        }
        if (this.reSendMessageService != null) {
            this.reSendMessageService.shutdown();
        }
    }

    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    public NettyConfig getNettyConfig() {
        return nettyConfig;
    }

    public ExecutorService getConnectExecutor() {
        return connectExecutor;
    }

    public ExecutorService getPubExecutor() {
        return pubExecutor;
    }

    public ExecutorService getSubExecutor() {
        return subExecutor;
    }

    public ExecutorService getPingExecutor() {
        return pingExecutor;
    }

    public LinkedBlockingQueue<Runnable> getConnectQueue() {
        return connectQueue;
    }

    public LinkedBlockingQueue<Runnable> getPubQueue() {
        return pubQueue;
    }

    public LinkedBlockingQueue<Runnable> getSubQueue() {
        return subQueue;
    }

    public LinkedBlockingQueue<Runnable> getPingQueue() {
        return pingQueue;
    }

    public NettyRemotingServer getRemotingServer() {
        return remotingServer;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public SubscriptionMatcher getSubscriptionMatcher() {
        return subscriptionMatcher;
    }


    public ConnectPermission getConnectPermission() {
        return connectPermission;
    }

    public PubSubPermission getPubSubPermission() {
        return pubSubPermission;
    }

    public ReSendMessageService getReSendMessageService() {
        return reSendMessageService;
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }

    public ChannelEventListener getChannelEventListener() {
        return channelEventListener;
    }

}
