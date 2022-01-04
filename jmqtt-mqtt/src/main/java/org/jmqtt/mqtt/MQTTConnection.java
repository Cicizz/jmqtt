
package org.jmqtt.mqtt;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import org.jmqtt.bus.*;
import org.jmqtt.bus.enums.*;
import org.jmqtt.bus.model.ClusterEvent;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.model.DeviceSession;
import org.jmqtt.bus.model.DeviceSubscription;
import org.jmqtt.mqtt.model.MqttTopic;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.mqtt.retain.RetainMessageHandler;
import org.jmqtt.mqtt.session.MqttSession;
import org.jmqtt.mqtt.utils.MqttMessageUtil;
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

    private DeviceSessionManager      deviceSessionManager;
    private DeviceSubscriptionManager deviceSubscriptionManager;
    private DeviceMessageManager      deviceMessageManager;
    private RetainMessageHandler      retainMessageHandler;
    private ClusterEventManager clusterEventManager;
    private String clientId;

    public MQTTConnection(Channel channel, Map<Integer, Pair<RequestProcessor, ExecutorService>> processorTable,
                          BrokerConfig brokerConfig, BusController busController,RetainMessageHandler retainMessageHandler) {
        this.channel = channel;
        this.processorTable = processorTable;
        this.brokerConfig = brokerConfig;
        this.authenticator = busController.getAuthenticator();
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
        int packetId = MqttMessageUtil.getMessageId(mqttMessage);
        boolean flag = bindedSession.releaseQos2SecFlow(packetId);
        LogUtil.debug(log,"[PubComp] -> Receive PubCom and remove the flow message,clientId={},msgId={}",clientId,packetId);
        if(!flag){
            LogUtil.warn(log,"[PubComp] -> The message is not in Flow cache,clientId={},msgId={}",clientId,packetId);
        }
    }

    public void processPubRec(MqttMessage mqttMessage){
        String clientId = getClientId();
        int packetId = MqttMessageUtil.getMessageId(mqttMessage);
        DeviceMessage stayAckMsg = bindedSession.releaseOutboundFlowMessage(packetId);
        if (stayAckMsg == null) {
            LogUtil.warn(log,"[PUBREC] Stay release message is not exist,packetId:{},clientId:{}",packetId,getClientId());
        } else {
            this.deviceMessageManager.ackMessage(getClientId(),stayAckMsg.getId());
        }

        bindedSession.receivePubRec(packetId);
        LogUtil.debug(log,"[PubRec] -> Receive PubRec message,clientId={},msgId={}",clientId,packetId);
        MqttMessage pubRelMessage = MqttMessageUtil.getPubRelMessage(packetId);
        this.channel.writeAndFlush(pubRelMessage);
    }

    public void processPubAck(MqttMessage mqttMessage){
        int packetId = MqttMessageUtil.getMessageId(mqttMessage);
        LogUtil.info(log, "[PubAck] -> Receive PubAck message,clientId={},msgId={}", getClientId(),packetId);
        DeviceMessage deviceMessage = bindedSession.releaseOutboundFlowMessage(packetId);
        if (deviceMessage == null) {
            LogUtil.warn(log,"[PUBACK] Stay release message is not exist,packetId:{},clientId:{}",packetId,getClientId());
            return;
        }
        this.deviceMessageManager.ackMessage(getClientId(),deviceMessage.getId());
    }

    public void processUnSubscribe(MqttUnsubscribeMessage mqttUnsubscribeMessage){
        String clientId = getClientId();
        MqttUnsubscribePayload unsubscribePayload = mqttUnsubscribeMessage.payload();
        List<String> topics = unsubscribePayload.topics();
        topics.forEach( topic -> {
            this.bindedSession.removeSubscription(topic);
            deviceSubscriptionManager.unSubscribe(clientId,topic);
        });
        MqttUnsubAckMessage unsubAckMessage = MqttMessageUtil.getUnSubAckMessage(MqttMessageUtil.getMessageId(mqttUnsubscribeMessage));
        this.channel.writeAndFlush(unsubAckMessage);
    }

    public void processSubscribe(MqttSubscribeMessage subscribeMessage) {

        int packetId = subscribeMessage.variableHeader().messageId();
        List<MqttTopic> validTopicList = validTopics(subscribeMessage.payload().topicSubscriptions());
        if (validTopicList == null || validTopicList.size() == 0) {
            LogUtil.warn(log, "[Subscribe] -> Valid all subscribe topic failure,clientId:{},packetId:{}", getClientId(),packetId);
            return;
        }

        // subscribe
        List<Integer> ackQos = new ArrayList<>(validTopicList.size());
        for (MqttTopic topic : validTopicList) {
            DeviceSubscription deviceSubscription = new DeviceSubscription();
            deviceSubscription.setTopic(topic.getTopicName());
            deviceSubscription.setClientId(getClientId());
            deviceSubscription.setSubscribeTime(new Date());
            Map<String,Object> properties = new HashMap<>();
            properties.put(MqttMsgHeader.QOS,topic.getQos());
            deviceSubscription.setProperties(properties);
            this.bindedSession.addSubscription(deviceSubscription);
            boolean succ = this.deviceSubscriptionManager.subscribe(deviceSubscription);
            if (succ) {
                ackQos.add(topic.getQos());
            } else {
                LogUtil.error(log,"[SUBSCRIBE] subscribe error.");
            }
        }

        MqttMessage subAckMessage = MqttMessageUtil.getSubAckMessage(packetId, ackQos);
        this.channel.writeAndFlush(subAckMessage).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // dispatcher retain message
                    List<DeviceMessage> retainMessages = retainMessageHandler.getAllRetatinMessage();
                    if (retainMessages != null) {
                        retainMessages.forEach(message -> {
                            message.setSource(MessageSourceEnum.DEVICE);
                            // match
                            for (MqttTopic topic : validTopicList) {
                                if (deviceSubscriptionManager.isMatch(message.getTopic(), topic.getTopicName())) {
                                    // 1. store stay message
                                    Long msgId = deviceMessageManager.storeMessage(message);
                                    deviceMessageManager.addClientInBoxMsg(getClientId(),msgId, MessageAckEnum.UN_ACK);

                                    // 2. ack message
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
    private List<MqttTopic> validTopics(List<MqttTopicSubscription> topics) {
        List<MqttTopic> topicList = new ArrayList<>();
        for (MqttTopicSubscription subscription : topics) {
            if (!authenticator.subscribeVerify(getClientId(), subscription.topicName())) {
                LogUtil.warn(log, "[SubPermission] this clientId:{} have no permission to subscribe this topic:{}", getClientId(),
                        subscription.topicName());
                continue;
            }
            MqttTopic topic = new MqttTopic(subscription.topicName(), subscription.qualityOfService().value());
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
        MqttMessage pubRecMessage = MqttMessageUtil.getPubRecMessage(originMessageId);
        this.channel.writeAndFlush(pubRecMessage);
    }

    private void processQos1(MqttPublishMessage mqttPublishMessage) {
        int originMessageId = mqttPublishMessage.variableHeader().packetId();
        dispatcherMessage(mqttPublishMessage);
        LogUtil.info(log, "[PubMessage] -> Process qos1 message,clientId={}", getClientId());
        MqttPubAckMessage pubAckMessage = MqttMessageUtil.getPubAckMessage(originMessageId);
        this.channel.writeAndFlush(pubAckMessage);
    }

    public void processPubRelMessage(MqttMessage mqttMessage) {
        int packetId = MqttMessageUtil.getMessageId(mqttMessage);
        DeviceMessage deviceMessage = bindedSession.receivedPubRelQos2(packetId);
        if (deviceMessage == null) {
            LogUtil.error(log, "[PUBREL] receivedPubRelQos2 cached message is not exist,message lost !!!!!,packetId:{},clientId:{}",
                    packetId, getClientId());
        } else {
            dispatcherMessage(deviceMessage);
        }
        MqttMessage pubComMessage = MqttMessageUtil.getPubComMessage(packetId);
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
        byte[] payload = MqttMessageUtil.readBytesFromByteBuf(mqttPublishMessage.payload());
        String topic = mqttPublishMessage.variableHeader().topicName();

        DeviceMessage deviceMessage = MqttMsgHeader.buildDeviceMessage(retain, qos, topic, payload);
        deviceMessage.setSource(MessageSourceEnum.DEVICE);
        deviceMessage.setFromClientId(clientId);
        dispatcherMessage(deviceMessage);
    }

    public void handleConnectionLost() {
        String clientID = MqttNettyUtils.clientID(channel);
        if (clientID == null || clientID.isEmpty()) {
            return;
        }
        if (bindedSession.hasWill()) {
            dispatcherMessage(bindedSession.getWill());
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
        this.clientId = clientId;
        boolean cleanSession = mqttConnectMessage.variableHeader().isCleanSession();

        this.bindedSession = new MqttSession();
        this.bindedSession.setClientId(clientId);
        this.bindedSession.setCleanSession(cleanSession);
        this.bindedSession.setMqttVersion(mqttVersion);
        this.bindedSession.setClientIp(RemotingHelper.getRemoteAddr(this.channel));
        this.bindedSession.setServerIp(RemotingHelper.getLocalAddr());
        this.bindedSession.setMqttConnection(this);

        boolean sessionPresent = false;
        boolean notifyClearOtherSession = true;


        // 1. 从集群/本服务器中查询是否存在该clientId的设备消息
        DeviceSession deviceSession = deviceSessionManager.getSession(clientId);

        if (deviceSession != null && deviceSession.getOnline() == DeviceOnlineStateEnum.ONLINE) {
            MQTTConnection previousClient = ConnectManager.getInstance().getClient(clientId);
            if (previousClient != null) {
                //clear old session in this node
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
                clearSession();
            } else {
                sessionPresent = true;
                reloadSubscriptions();
            }
        }

        // 2. 清理连接到其它节点的连接
        if (notifyClearOtherSession) {
            ClusterEvent clusterEvent = new ClusterEvent();
            clusterEvent.setClusterEventCode(ClusterEventCodeEnum.MQTT_CLEAR_SESSION);
            clusterEvent.setContent(getClientId());
            clusterEvent.setGmtCreate(new Date());
            clusterEvent.setNodeIp(bindedSession.getServerIp());
            clusterEventManager.sendEvent(clusterEvent);
        }
        // 3. 处理will 消息
        boolean willFlag = mqttConnectMessage.variableHeader().isWillFlag();
        if (willFlag) {
            boolean willRetain = mqttConnectMessage.variableHeader().isWillRetain();
            int willQos = mqttConnectMessage.variableHeader().willQos();
            String willTopic = mqttConnectMessage.payload().willTopic();
            byte[] willPayload = mqttConnectMessage.payload().willMessageInBytes();
            DeviceMessage deviceMessage = MqttMsgHeader.buildDeviceMessage(willRetain, willQos, willTopic, willPayload);
            deviceMessage.setSource(MessageSourceEnum.DEVICE);
            deviceMessage.setFromClientId(clientId);
            bindedSession.setWill(deviceMessage);
        }
        return sessionPresent;
    }

    public void storeSession() {
        ConnectManager.getInstance().putClient(getClientId(),this);
        DeviceSession deviceSession = new DeviceSession();
        deviceSession.setTransportProtocol(TransportProtocolEnum.MQTT);
        deviceSession.setServerIp(bindedSession.getServerIp());
        deviceSession.setClientIp(bindedSession.getClientIp());
        deviceSession.setOnlineTime(new Date());
        deviceSession.setClientId(getClientId());
        deviceSession.setOnline(DeviceOnlineStateEnum.ONLINE);
        Map<String,Object> properties = new HashMap<>();
        properties.put(MqttMsgHeader.CLEAN_SESSION,bindedSession.isCleanSession());
        deviceSession.setProperties(properties);
        deviceSessionManager.storeSession(deviceSession);
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

    public void processDisconnect() {
        // 1. 清理will消息
        bindedSession.clearWill();
        // 2. 下线
        offline();
    }

    private void clearSession() {
        deviceSubscriptionManager.deleteAllSubscription(getClientId());
        deviceMessageManager.clearUnAckMessage(getClientId());
    }

    private void reloadSubscriptions() {
        Set<DeviceSubscription> deviceSubscriptions = deviceSubscriptionManager.getAllSubscription(getClientId());
        if (deviceSubscriptions != null) {
            deviceSubscriptions.forEach(item -> {
                deviceSubscriptionManager.onlySubscribe2Tree(item);
            });
        }
    }

    private void offline() {
        this.deviceSessionManager.offline(getClientId());
        ConnectManager.getInstance().removeClient(getClientId());
    }

    public String getClientId() {
        return this.clientId;
    }

    public Channel getChannel() {
        return channel;
    }

    public Map<Integer, Pair<RequestProcessor, ExecutorService>> getProcessorTable() {
        return processorTable;
    }

    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    public MqttSession getBindedSession() {
        return bindedSession;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public DeviceSessionManager getDeviceSessionManager() {
        return deviceSessionManager;
    }

    public DeviceSubscriptionManager getDeviceSubscriptionManager() {
        return deviceSubscriptionManager;
    }

    public DeviceMessageManager getDeviceMessageManager() {
        return deviceMessageManager;
    }

    public RetainMessageHandler getRetainMessageHandler() {
        return retainMessageHandler;
    }

    public ClusterEventManager getClusterEventManager() {
        return clusterEventManager;
    }
}
