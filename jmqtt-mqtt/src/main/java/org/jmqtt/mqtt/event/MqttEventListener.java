package org.jmqtt.mqtt.event;

import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import org.jmqtt.bus.DeviceMessageManager;
import org.jmqtt.bus.enums.ClusterEventCodeEnum;
import org.jmqtt.bus.enums.MessageAckEnum;
import org.jmqtt.bus.event.GatewayListener;
import org.jmqtt.bus.model.ClusterEvent;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.model.DeviceSubscription;
import org.jmqtt.bus.subscription.model.Subscription;
import org.jmqtt.mqtt.ConnectManager;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.utils.MqttMessageUtil;
import org.jmqtt.mqtt.utils.MqttMsgHeader;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.jmqtt.support.remoting.RemotingHelper;
import org.slf4j.Logger;

import java.util.Map;

public class MqttEventListener implements GatewayListener {

    private static final Logger log = JmqttLogger.mqttLog;

    private static final String        currentIp  = RemotingHelper.getLocalAddr();

    private DeviceMessageManager deviceMessageManager;

    public MqttEventListener(DeviceMessageManager deviceMessageManager) {
        this.deviceMessageManager = deviceMessageManager;
    }

    /**
     * 分发到网关的消息，bus上检查设备是在线的，若不在线，说明是断开了
     * mqtt网关处理两类事件：
     * 1. 集群消息：分发给设备
     * 2. 清理session：设备连接到其它服务器了，需要清理当前服务器上该设备的连接session（clientId 保持唯一）
     * @param clusterEvent
     */
    @Override
    @Subscribe
    public void consume(ClusterEvent clusterEvent) {
        ClusterEventCodeEnum eventCode = clusterEvent.getClusterEventCode();
        switch (eventCode){
            case MQTT_CLEAR_SESSION:
                clearCurrentNodeSession(clusterEvent);
                break;
            case DISPATCHER_CLIENT_MESSAGE:
                sendMessage2Client(clusterEvent);
                break;
            default:
                LogUtil.warn(log,"Mqtt gateway not support this event:{}",eventCode);
                break;
        }
    }


    private void sendMessage2Client(ClusterEvent clusterEvent) {
        DeviceMessage deviceMessage = JSONObject.parseObject(clusterEvent.getContent(),DeviceMessage.class);
        Subscription subscription = clusterEvent.getSubscription();
        if (deviceMessage == null ||subscription == null) {
            LogUtil.error(log,"[EVENT] Send event error, message or subscription is null,message:{},subscription:{}",deviceMessage,subscription);
            return;
        }
        MQTTConnection mqttConnection = ConnectManager.getInstance().getClient(subscription.getClientId());

        Integer subQos = subscription.getProperty(MqttMsgHeader.QOS);
        Integer msgQos = deviceMessage.getProperty(MqttMsgHeader.QOS);
        if (subQos == null) {
            subQos = 1;
        }
        if (msgQos == null) {
            msgQos = 1;
        }
        int qos = MqttMessageUtil.getMinQos(subQos,msgQos);

        // add to client inbox
        if (qos == 0) {
            this.deviceMessageManager.addClientInBoxMsg(subscription.getClientId(),deviceMessage.getId(), MessageAckEnum.ACK);
        } else {
            this.deviceMessageManager.addClientInBoxMsg(subscription.getClientId(),deviceMessage.getId(),MessageAckEnum.UN_ACK);
        }

        // offline can be optimize ,can carry to bus
        if (mqttConnection == null) {
            LogUtil.warn(log,"[EVENT] connection is not exist.");
            return;
        }
        mqttConnection.getBindedSession().sendMessage(deviceMessage);
    }

    private void clearCurrentNodeSession(ClusterEvent clusterEvent){
        if (currentIp.equals(clusterEvent.getNodeIp())) {
            return;
        }

        String clientId = clusterEvent.getContent();
        MQTTConnection mqttConnection = ConnectManager.getInstance().getClient(clientId);
        if (mqttConnection == null) {
            LogUtil.info(log,"MQTT connection is not exist.{}",clientId);
            return;
        }
        // 1. clear subscription tree
        Map<String,DeviceSubscription> subscriptionSet = mqttConnection.getBindedSession().getSubscriptions();
        if (subscriptionSet != null && subscriptionSet.size() > 0) {
            for (String topic:subscriptionSet.keySet()) {
                mqttConnection.getDeviceSubscriptionManager().onlyUnUnSubscribeFromTree(clientId,topic);
                LogUtil.debug(log,"[CLUSTER SESSION] clear other node's session.un subscribe clientId:{},topic:{}",clientId,topic);
            }
        }

        // 2. close channel
        mqttConnection.abortConnection(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);

        // 3. remove connection in cache
        ConnectManager.getInstance().removeClient(clientId);
    }
}
