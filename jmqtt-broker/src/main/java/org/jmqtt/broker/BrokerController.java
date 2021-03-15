package org.jmqtt.broker;

import io.netty.handler.codec.mqtt.MqttMessageType;
import org.jmqtt.broker.acl.AuthValid;
import org.jmqtt.broker.client.ClientLifeCycleHookService;
import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.config.NettyConfig;
import org.jmqtt.broker.common.helper.MixAll;
import org.jmqtt.broker.common.helper.RejectHandler;
import org.jmqtt.broker.common.helper.ThreadFactoryImpl;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.processor.dispatcher.ClusterEventHandler;
import org.jmqtt.broker.processor.dispatcher.DefaultDispatcherInnerMessage;
import org.jmqtt.broker.processor.dispatcher.EventConsumeHandler;
import org.jmqtt.broker.processor.dispatcher.InnerMessageDispatcher;
import org.jmqtt.broker.processor.protocol.*;
import org.jmqtt.broker.processor.recover.ReSendMessageService;
import org.jmqtt.broker.remoting.netty.ChannelEventListener;
import org.jmqtt.broker.remoting.netty.NettyRemotingServer;
import org.jmqtt.broker.store.MessageStore;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.store.highperformance.InflowMessageHandler;
import org.jmqtt.broker.store.highperformance.OutflowMessageHandler;
import org.jmqtt.broker.store.highperformance.OutflowSecMessageHandler;
import org.jmqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 具体控制类：负责加载配置文件，初始化环境，启动服务等
 */
public class BrokerController {

    private static final Logger log = JmqttLogger.brokerlog;

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

    private InnerMessageDispatcher innerMessageDispatcher;
    private SubscriptionMatcher    subscriptionMatcher;
    private AuthValid              authValid;
    private ReSendMessageService   reSendMessageService;
    private SessionStore           sessionStore;
    private MessageStore           messageStore;
    private ClusterEventHandler clusterEventHandler;
    private EventConsumeHandler eventConsumeHandler;
    private String currentIp;

    // high performance message handle
    private InflowMessageHandler     inflowMessageHandler;
    private OutflowMessageHandler    outflowMessageHandler;
    private OutflowSecMessageHandler outflowSecMessageHandler;

    public BrokerController(BrokerConfig brokerConfig, NettyConfig nettyConfig) {
        this.brokerConfig = brokerConfig;
        this.nettyConfig = nettyConfig;

        this.connectQueue = new LinkedBlockingQueue<>(100000);
        this.pubQueue = new LinkedBlockingQueue<>(100000);
        this.subQueue = new LinkedBlockingQueue<>(100000);
        this.pingQueue = new LinkedBlockingQueue<>(10000);
        this.currentIp = MixAll.getLocalIp();

        {
            // 会话状态，消息存储加载，可自己实现相关的类
            this.sessionStore = MixAll.pluginInit(brokerConfig.getSessionStoreClass());
            this.messageStore = MixAll.pluginInit(brokerConfig.getMessageStoreClass());

            // 设备连接，发布，订阅消息权限控制
            this.authValid = MixAll.pluginInit(brokerConfig.getAuthValidClass());

            // 集群事件转发加载
            this.clusterEventHandler = MixAll.pluginInit(brokerConfig.getClusterEventHandlerClass());
        }

        // high performance message handler
        this.inflowMessageHandler = new InflowMessageHandler();
        this.outflowMessageHandler = new OutflowMessageHandler();
        this.outflowSecMessageHandler = new OutflowSecMessageHandler();

        this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
        this.innerMessageDispatcher = new DefaultDispatcherInnerMessage(this);
        this.eventConsumeHandler = new EventConsumeHandler(this);

        this.channelEventListener = new ClientLifeCycleHookService(messageStore, innerMessageDispatcher);
        this.remotingServer = new NettyRemotingServer(brokerConfig, nettyConfig, channelEventListener);
        this.reSendMessageService = new ReSendMessageService(this);

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

    }



    public void start() {

        MixAll.printProperties(log, brokerConfig);
        MixAll.printProperties(log, nettyConfig);

        // 1. start store
        this.sessionStore.start(brokerConfig);
        this.messageStore.start(brokerConfig);

        // 2. start cluster
        this.eventConsumeHandler.start();
        this.clusterEventHandler.start(brokerConfig);

        // 3. start message service
        if (this.innerMessageDispatcher != null) {
            this.innerMessageDispatcher.start();
        }
        if (this.reSendMessageService != null) {
            this.reSendMessageService.start();
        }

        {
            // 4. init and register mqtt protocol processor
            RequestProcessor connectProcessor = new ConnectProcessor(this);
            RequestProcessor disconnectProcessor = new DisconnectProcessor(this);
            RequestProcessor pingProcessor = new PingProcessor();
            RequestProcessor publishProcessor = new PublishProcessor(this);
            RequestProcessor pubRelProcessor = new PubRelProcessor(this);
            RequestProcessor subscribeProcessor = new SubscribeProcessor(this);
            RequestProcessor unSubscribeProcessor = new UnSubscribeProcessor(subscriptionMatcher, sessionStore);
            RequestProcessor pubRecProcessor = new PubRecProcessor(this);
            RequestProcessor pubAckProcessor = new PubAckProcessor(this);
            RequestProcessor pubCompProcessor = new PubCompProcessor(this);

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

        // 5. start auth
        if (this.authValid != null) {
            this.authValid.start();
        }

        // 6. start remoting
        if (this.remotingServer != null) {
            this.remotingServer.start();
        }
        LogUtil.info(log,"JMqtt Server start success and version = {}", brokerConfig.getVersion());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }));
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
        if (this.innerMessageDispatcher != null) {
            this.innerMessageDispatcher.shutdown();
        }
        if (this.reSendMessageService != null) {
            this.reSendMessageService.shutdown();
        }
        if (this.clusterEventHandler != null) {
            this.clusterEventHandler.shutdown();
        }
        if (this.eventConsumeHandler != null) {
            this.eventConsumeHandler.shutdown();
        }
        if (this.sessionStore != null) {
            this.sessionStore.shutdown();
        }
        if (this.messageStore != null) {
            this.messageStore.shutdown();
        }
        if (this.authValid != null) {
            this.authValid.shutdown();
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

    public InnerMessageDispatcher getInnerMessageDispatcher() {
        return innerMessageDispatcher;
    }

    public SubscriptionMatcher getSubscriptionMatcher() {
        return subscriptionMatcher;
    }


    public AuthValid getAuthValid() {
        return authValid;
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

    public ClusterEventHandler getClusterEventHandler() {
        return clusterEventHandler;
    }

    public void setClusterEventHandler(ClusterEventHandler clusterEventHandler) {
        this.clusterEventHandler = clusterEventHandler;
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public EventConsumeHandler getEventConsumeHandler() {
        return eventConsumeHandler;
    }

    public InflowMessageHandler getInflowMessageHandler() {
        return inflowMessageHandler;
    }

    public OutflowMessageHandler getOutflowMessageHandler() {
        return outflowMessageHandler;
    }

    public OutflowSecMessageHandler getOutflowSecMessageHandler() {
        return outflowSecMessageHandler;
    }
}
