
package org.jmqtt.mqtt.session;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.model.DeviceSubscription;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.utils.MqttMessageUtil;
import org.jmqtt.mqtt.utils.MqttMsgHeader;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * mqtt session
 */
public class MqttSession {

    private static final Logger log = JmqttLogger.mqttLog;

    /**
     * clientId uniqu in a cluseter
     */
    private           String                      clientId;
    private           boolean                     cleanSession;
    private transient AtomicInteger               lastPacketId              = new AtomicInteger(1);
    private           int                         mqttVersion;
    private           DeviceMessage               will;
    private final     Map<Integer, DeviceMessage> qos2Receiving             = new ConcurrentHashMap<>();
    private MQTTConnection                        mqttConnection;
    private final Map<Integer,DeviceMessage>      outboundFlowMessages      = new ConcurrentHashMap<>();
    private final Set<Integer>                    qos2OutboundFlowPacketIds = new CopyOnWriteArraySet<>();
    private String                                clientIp;
    private String                                serverIp;
    private Map<String, DeviceSubscription>   subscriptions             = new HashMap<>();


    public void addSubscription(DeviceSubscription deviceSubscription) {
        this.subscriptions.put(deviceSubscription.getTopic(),deviceSubscription);
    }

    public void removeSubscription(String topic) {
        this.subscriptions.remove(topic);
    }

    public void receivePubRec(int packetId){
        this.qos2OutboundFlowPacketIds.add(packetId);
    }

    public boolean releaseQos2SecFlow(int packetId){
        return this.qos2OutboundFlowPacketIds.remove(packetId);
    }

    public DeviceMessage releaseOutboundFlowMessage(int packetId){
        DeviceMessage deviceMessage = outboundFlowMessages.remove(packetId);
        if (deviceMessage == null) {
            LogUtil.error(log,"[PUBACK Or PUBREC] Release cache flow message,message is not success,packetId:{}",packetId);
        }
        return deviceMessage;
    }

    public void sendMessage(DeviceMessage deviceMessage) {
        Integer qos = deviceMessage.getProperty(MqttMsgHeader.QOS);
        MqttQoS mqttQoS = qos == null ? MqttQoS.AT_LEAST_ONCE : MqttQoS.valueOf(qos);
        switch (mqttQoS){
            case AT_MOST_ONCE:
                sendQos0Message(deviceMessage);
                break;
            case AT_LEAST_ONCE:
                sendQos1Or2Message(deviceMessage);
                break;
            case EXACTLY_ONCE:
                sendQos1Or2Message(deviceMessage);
                break;
            default:
                LogUtil.error(log,"[Outbound publish] unsupport qos.",qos);
                break;
        }
    }

    private void sendQos0Message(DeviceMessage deviceMessage) {
        MqttPublishMessage mqttPublishMessage = MqttMessageUtil.getPubMessage(deviceMessage,0);
        mqttConnection.publishMessage(mqttPublishMessage);
    }

    private void sendQos1Or2Message(DeviceMessage deviceMessage) {
        int packetId = nextPacketId();
        // cache
        outboundFlowMessages.put(packetId,deviceMessage);

        MqttPublishMessage mqttPublishMessage = MqttMessageUtil.getPubMessage(deviceMessage,packetId);
        mqttConnection.publishMessage(mqttPublishMessage);
    }


    public void receivedPublishQos2(int originPacketId,DeviceMessage deviceMessage) {
        this.qos2Receiving.put(originPacketId,deviceMessage);
    }

    public DeviceMessage receivedPubRelQos2(int packgetId) {
        return this.qos2Receiving.remove(packgetId);
    }

    public void clearWill(){
        this.will = null;
    }

    public boolean hasWill(){
        return will != null;
    }

    public int nextPacketId() {
        return lastPacketId.updateAndGet(v -> v == 65535 ? 1 : v + 1);
    }

    /** setter getter  */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getMqttVersion() {
        return mqttVersion;
    }

    public void setMqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    public DeviceMessage getWill() {
        return will;
    }

    public void setWill(DeviceMessage will) {
        this.will = will;
    }


    public MQTTConnection getMqttConnection() {
        return mqttConnection;
    }

    public void setMqttConnection(MQTTConnection mqttConnection) {
        this.mqttConnection = mqttConnection;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Map<String, DeviceSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<String, DeviceSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
