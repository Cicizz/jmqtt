
package org.jmqtt.mqtt;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.bus.*;
import org.jmqtt.bus.enums.ClusterEventCodeEnum;
import org.jmqtt.bus.enums.DeviceOnlineStateEnum;
import org.jmqtt.bus.model.ClusterEvent;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.model.DeviceSession;
import org.jmqtt.bus.model.DeviceSubscription;
import org.jmqtt.mqtt.model.Subscription;
import org.jmqtt.mqtt.model.Topic;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.mqtt.retain.RetainMessageHandler;
import org.jmqtt.mqtt.session.MqttSession;
import org.jmqtt.mqtt.subscription.SubscriptionMatcher;
import org.jmqtt.mqtt.utils.MessageUtil;
import org.jmqtt.mqtt.utils.MqttMsgHeader;
import org.jmqtt.support.config.BrokerConfig;
import org.jmqtt.support.helper.Pair;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.jmqtt.support.remoting.RemotingHelper;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;
import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

/**
 * handle mqtt connection and process mqtt protocol
 */
public class MQTTConnection {

    private static final Logger log = JmqttLogger.mqttLog;

    private Channel                                               channel;
    private Map<Integer, Pair<RequestProcessor, ExecutorService>> processorTable;
    private BrokerConfig                                          brokerConfig;
    private MqttSession                                           bindedSession;
    private Authenticator                                         authenticator;
    private SubscriptionMatcher                                   subscriptionMatcher;

    private DeviceSessionManager      deviceSessionManager;
    private DeviceSubscriptionManager deviceSubscriptionManager;
    private DeviceMessageManager      deviceMessageManager;
    private RetainMessageHandler      retainMessageHandler;
    private ClusterEventManager clusterEventManager;

    public MQTTConnection(Channel channel, Map<Integer, Pair<RequestProcessor, ExecutorService>> processorTable,
                          BrokerConfig brokerConfig, BusController busController,SubscriptionMatcher subscriptionMatcher,RetainMessageHandler retainMessageHandler) {
        this.channel = channel;
        this.processorTable = processorTable;
        this.brokerConfig = brokerConfig;
        this.authenticator = busController.getAuthenticator();
        this.subscriptionMatcher = subscriptionMatcher;
        this.deviceSessionManager = busController.getDeviceSessionManager();
        this.deviceMessageManager = busController.getDeviceMessageManager();
        this.deviceSubscriptionManager = busController.getDeviceSubscriptionManager();
        this.retainMessageHandler = retainMessageHandler;
        this.clusterEventManager = busController.getClusterEventManager();
    }

    public void processProtocol(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        int protocolType = mqttMessage.fixedHeader().messageType().value();
        Runnable runnable = () -> processorTable.get(protocolType).getObject1().processRequest(ctx, mqttMessage);
        try {
            processorTable.get(protocolType).getObject2().submit(runnable);
        } catch (RejectedExecutionException ex) {
            LogUtil.warn(log, "Reject mqtt request,cause={}", ex.getMessage());
        }
    }

    public void processPubComp(MqttMessage mqttMessage) {
        String clientId = getClientId();
        int packetId = MessageUtil.getMessageId(mqttMessage);
        boolean flag = bindedSession.releaseQos2SecFlow(packetId);
        LogUtil.debug(log,"[PubComp] -> Receive PubCom and remove the flow message,clientId={},msgId={}",clientId,packetId);
        if(!flag){
            LogUtil.warn(log,"[PubComp] -> The message is not in Flow cache,clientId={},msgId={}",clientId,packetId);
        }
    }

    public void processPubRec(MqttMessage mqttMessage){
        String clientId = getClientId();
        int packetId = MessageUtil.getMessageId(mqttMessage);
        bindedSession.releaseOutboundFlowMessage(packetId);
        bindedSession.receivePubRec(packetId);

        LogUtil.debug(log,"[PubRec] -> Receive PubRec message,clientId={},msgId={}",clientId,packetId);
        MqttMessage pubRelMessage = MessageUtil.getPubRelMessage(packetId);
        this.channel.writeAndFlush(pubRelMessage);
    }

    public void processPubAck(MqttMessage mqttMessage){
        int packetId = MessageUtil.getMessageId(mqttMessage);
        LogUtil.info(log, "[PubAck] -> Receive PubAck message,clientId={},msgId={}", getClientId(),packetId);
        bindedSession.releaseOutboundFlowMessage(packetId);
    }

    public void processUnSubscribe(MqttUnsubscribeMessage mqttUnsubscribeMessage){
        String clientId = getClientId();
        MqttUnsubscribePayload unsubscribePayload = mqttUnsubscribeMessage.payload();
        List<String> topics = unsubscribePayload.topics();
        topics.forEach( topic -> {
            subscriptionMatcher.unSubscribe(topic,getClientId());
            deviceSubscriptionManager.deleteSubscription(clientId,topic);
        });
        MqttUnsubAckMessage unsubAckMessage = MessageUtil.getUnSubAckMessage(MessageUtil.getMessageId(mqttUnsubscribeMessage));
        this.channel.writeAndFlush(unsubAckMessage);
    }

    public void processSubscribe(MqttSubscribeMessage subscribeMessage) {

        int packetId = subscribeMessage.variableHeader().messageId();
        List<Topic> validTopicList = validTopics(subscribeMessage.payload().topicSubscriptions());
        if (validTopicList == null || validTopicList.size() == 0) {
            LogUtil.warn(log, "[Subscribe] -> Valid all subscribe topic failure,clientId:{},packetId:{}", getClientId(),packetId);
            return;
        }

        // subscribe
        for (Topic topic : validTopicList) {
            Subscription subscription = new Subscription(getClientId(), topic.getTopicName(), topic.getQos());
            boolean subRs = this.subscriptionMatcher.subscribe(subscription);
            if (!subRs) {
                LogUtil.error(log, "[SUBSCRIBE] subscribe tree handle error.");
            }
            DeviceSubscription deviceSubscription = MqttMsgHeader.convert(subscription);
            this.deviceSubscriptionManager.storeSubscription(deviceSubscription);
        }

        List<Integer> ackQos = new ArrayList<>(validTopicList.size());
        for (Topic topic : validTopicList) {
            ackQos.add(topic.getQos());
        }
        MqttMessage subAckMessage = MessageUtil.getSubAckMessage(packetId, ackQos);
        this.channel.writeAndFlush(subAckMessage).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // dispatcher retain message
                    List<DeviceMessage> retainMessages = retainMessageHandler.getAllRetatinMessage();
                    if (retainMessages != null) {
                        retainMessages.forEach(message -> {
                            // match
                            for (Topic topic : validTopicList) {
                                if (subscriptionMatcher.isMatch(message.getTopic(), topic.getTopicName())) {
                                    bindedSession.sendMessage(message);
                                }
                            }
                        });
                    }

                } else {
                    LogUtil.error(log, "[SUBSCRIBE] suback response error.");
                }
            }
        });
    }

    public void publishMessage(MqttPublishMessage mqttPublishMessage){
        this.channel.writeAndFlush(mqttPublishMessage);
    }

    /**
     * 返回校验合法的topic
     */
    private List<Topic> validTopics(List<MqttTopicSubscription> topics) {
        List<Topic> topicList = new ArrayList<>();
        for (MqttTopicSubscription subscription : topics) {
            if (!authenticator.subscribeVerify(getClientId(), subscription.topicName())) {
                LogUtil.warn(log, "[SubPermission] this clientId:{} have no permission to subscribe this topic:{}", getClientId(),
                        subscription.topicName());
                continue;
            }
            Topic topic = new Topic(subscription.topicName(), subscription.qualityOfService().value());
            topicList.add(topic);
        }
        return topicList;
    }

    public void processPublishMessage(MqttPublishMessage mqttPublishMessage) {
        MqttQoS qos = mqttPublishMessage.fixedHeader().qosLevel();
        switch (qos) {
            case AT_MOST_ONCE:
                dispatcherMessage(mqttPublishMessage);
                break;
            case AT_LEAST_ONCE:
                processQos1(mqttPublishMessage);
                break;
            case EXACTLY_ONCE:
                processQos2(mqttPublishMessage);
                break;
            default:
                LogUtil.warn(log, "[PubMessage] -> Wrong mqtt message,clientId={}", getClientId());
                break;
        }
    }

    private void processQos2(MqttPublishMessage mqttPublishMessage) {
        int originMessageId = mqttPublishMessage.variableHeader().packetId();
        LogUtil.debug(log, "[PubMessage] -> Process qos2 message,clientId={}", getClientId());
        DeviceMessage deviceMessage = MqttMsgHeader.buildDeviceMessage(mqttPublishMessage);
        bindedSession.receivedPublishQos2(originMessageId, deviceMessage);
        MqttMessage pubRecMessage = MessageUtil.getPubRecMessage(originMessageId);
        this.channel.writeAndFlush(pubRecMessage);
    }

    private void processQos1(MqttPublishMessage mqttPublishMessage) {
        int originMessageId = mqttPublishMessage.variableHeader().packetId();
        dispatcherMessage(mqttPublishMessage);
        LogUtil.info(log, "[PubMessage] -> Process qos1 message,clientId={}", getClientId());
        MqttPubAckMessage pubAckMessage = MessageUtil.getPubAckMessage(originMessageId);
        this.channel.writeAndFlush(pubAckMessage);
    }

    public void processPubRelMessage(MqttMessage mqttMessage) {
        int packetId = MessageUtil.getMessageId(mqttMessage);
        DeviceMessage deviceMessage = bindedSession.receivedPubRelQos2(packetId);
        if (deviceMessage == null) {
            LogUtil.error(log, "[PUBREL] receivedPubRelQos2 cached message is not exist,message lost !!!!!,packetId:{},clientId:{}",
                    packetId, getClientId());
        } else {
            dispatcherMessage(deviceMessage);
        }
        MqttMessage pubComMessage = MessageUtil.getPubComMessage(packetId);
        this.channel.writeAndFlush(pubComMessage);
    }

    private void dispatcherMessage(DeviceMessage deviceMessage) {
        // 1. retain消息逻辑
        boolean retain = deviceMessage.getProperty(MqttMsgHeader.RETAIN);
        int qos = deviceMessage.getProperty(MqttMsgHeader.QOS);
        if (retain) {
            //qos == 0 or payload is none,then clear previous retain message
            if (qos == 0 || deviceMessage.getContent() == null || deviceMessage.getContent().length == 0) {
                this.retainMessageHandler.clearRetainMessage(deviceMessage.getTopic());
            } else {
                this.retainMessageHandler.storeRetainMessage(deviceMessage);
            }
        }
        // 2. 向集群中分发消息：第一阶段
        this.deviceMessageManager.dispatcher(deviceMessage);
    }

    private void dispatcherMessage(MqttPublishMessage mqttPublishMessage) {
        boolean retain = mqttPublishMessage.fixedHeader().isRetain();
        int qos = mqttPublishMessage.fixedHeader().qosLevel().value();
        byte[] payload = mqttPublishMessage.payload().array();
        String topic = mqttPublishMessage.variableHeader().topicName();

        DeviceMessage deviceMessage = MqttMsgHeader.buildDeviceMessage(retain, qos, topic, payload);
        dispatcherMessage(deviceMessage);
    }

    public void handleConnectionLost() {
        String clientID = MqttNettyUtils.clientID(channel);
        if (clientID == null || clientID.isEmpty()) {
            return;
        }
        if (bindedSession.hasWill()) {
            // send will message 2 cluster
        }
        if (bindedSession.isCleanSession()) {
            clearSession();
        }
        offline();
        LogUtil.info(log, "[CONNECT INACTIVE] connect lost");
    }

    public boolean createOrReopenSession(MqttConnectMessage mqttConnectMessage) {

        int mqttVersion = mqttConnectMessage.variableHeader().version();
        String clientId = mqttConnectMessage.payload().clientIdentifier();
        boolean cleanSession = mqttConnectMessage.variableHeader().isCleanSession();

        boolean notifyClearOtherSession = true;
        this.bindedSession = new MqttSession();
        this.bindedSession.setClientId(clientId);
        this.bindedSession.setCleanSession(cleanSession);
        this.bindedSession.setMqttVersion(mqttVersion);
        this.bindedSession.setClientIp(RemotingHelper.getRemoteAddr(this.channel));
        this.bindedSession.setServerIp(RemotingHelper.getLocalAddr());

        boolean sessionPresent = false;

        // 1. 从集群/本服务器中查询是否存在该clientId的设备消息
        DeviceSession deviceSession = deviceSessionManager.getSession(clientId);

        if (deviceSession != null && deviceSession.getOnline() == DeviceOnlineStateEnum.ONLINE) {
            MQTTConnection previousClient = ConnectManager.getInstance().getClient(clientId);
            if (previousClient != null) {
                previousClient.abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
                ConnectManager.getInstance().removeClient(clientId);
                notifyClearOtherSession = false;
            }
        }
        if (deviceSession == null) {
            sessionPresent = false;
            notifyClearOtherSession = false;
        } else {
            if (cleanSession) {
                sessionPresent = false;
                notifyClearOtherSession = false;
                clearSession();
            } else {
                sessionPresent = true;
                reloadSession();
            }
        }

        // 2. 清理连接到其它节点的连接
        if (notifyClearOtherSession) {
            ClusterEvent clusterEvent = new ClusterEvent();
            clusterEvent.setClusterEventCode(ClusterEventCodeEnum.CLEAR_SESSION);
            clusterEvent.setContent(getClientId());
            clusterEvent.setGmtCreate(new Date());
            clusterEvent.setNodeIp(bindedSession.getServerIp());
        }
        // 3. 处理will 消息
        boolean willFlag = mqttConnectMessage.variableHeader().isWillFlag();
        if (willFlag) {
            boolean willRetain = mqttConnectMessage.variableHeader().isWillRetain();
            int willQos = mqttConnectMessage.variableHeader().willQos();
            String willTopic = mqttConnectMessage.payload().willTopic();
            byte[] willPayload = mqttConnectMessage.payload().willMessageInBytes();
            DeviceMessage deviceMessage = MqttMsgHeader.buildDeviceMessage(willRetain, willQos, willTopic, willPayload);
            bindedSession.setWill(deviceMessage);
        }
        return sessionPresent;
    }

    public void reSendMessage2Client() {
        int limit = 100; // per 100
        boolean hasUnAckMessages = true;
        while (hasUnAckMessages) {
            List<DeviceMessage> deviceInboxMessageList = this.deviceMessageManager.queryUnAckMessages(getClientId(),limit);
            if (deviceInboxMessageList == null) {
                return;
            }
            if (deviceInboxMessageList.size() < 100) {
                hasUnAckMessages = false;
            }

            // resendMessage
            deviceInboxMessageList.forEach(deviceMessage -> {
                bindedSession.sendMessage(deviceMessage);
            });
        }

    }

    public boolean keepAlive(int heatbeatSec) {
        int keepAlive = (int) (heatbeatSec * 1.5f);
        if (this.channel.pipeline().names().contains("idleStateHandler")) {
            this.channel.pipeline().remove("idleStateHandler");
        }
        this.channel.pipeline().addFirst("idleStateHandler", new IdleStateHandler(keepAlive, 0, 0));
        return true;
    }

    public boolean login(String clientId, String username, byte[] password) {
        return this.authenticator.login(clientId, username, password);
    }

    public boolean onBlackList(String remoteAddr, String clientId) {
        return this.authenticator.onBlackList(clientId, remoteAddr);
    }

    public boolean clientIdVerify(String clientId) {
        return this.authenticator.clientIdVerify(clientId);
    }

    public void abortConnection(MqttConnectReturnCode returnCode) {
        MqttConnAckMessage badProto = MqttMessageBuilders.connAck()
                .returnCode(returnCode)
                .sessionPresent(false).build();
        this.channel.writeAndFlush(badProto).addListener(FIRE_EXCEPTION_ON_FAILURE);
        this.channel.close().addListener(CLOSE_ON_FAILURE);
    }

    public void disconnect() {

        // 1. 判断是否清理会话
        if (bindedSession.isCleanSession()) {
            Set<Subscription> subscriptions = bindedSession.getSubscriptions();
            if (subscriptions != null) {
                for (Subscription subscription : subscriptions) {
                    this.subscriptionMatcher.unSubscribe(subscription.getTopic(), bindedSession.getClientId());
                }
            }
            clearSession();
        }

        // 3. 清理will消息
        bindedSession.clearWill();

        // 4. 下线
        offline();
    }

    // TODO can be async
    private void clearSession() {
        deviceSubscriptionManager.deleteAllSubscription(getClientId());
        deviceMessageManager.clearOfflineMessage(getClientId());
    }

    private void reloadSession() {
        Set<DeviceSubscription> deviceSubscriptions = deviceSubscriptionManager.getAllSubscription(getClientId());
        if (deviceSubscriptions != null) {
            deviceSubscriptions.forEach(item -> {
                Subscription subscription = MqttMsgHeader.convert(item);
                bindedSession.subscribe(subscription);
                subscriptionMatcher.subscribe(subscription);
            });
        }

        // TODO resendOfflineMessage
    }

    private void offline() {
        this.deviceSessionManager.offline(getClientId());
        ConnectManager.getInstance().removeClient(getClientId());
    }

    public String getClientId() {
        return MqttNettyUtils.clientID(channel);
    }
}
