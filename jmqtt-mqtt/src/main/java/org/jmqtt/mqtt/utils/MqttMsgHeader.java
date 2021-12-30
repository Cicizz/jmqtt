package org.jmqtt.mqtt.utils;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.jmqtt.bus.enums.MessageSourceEnum;
import org.jmqtt.bus.model.DeviceMessage;
import org.jmqtt.bus.model.DeviceSubscription;
import org.jmqtt.mqtt.model.Subscription;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MqttMsgHeader {

    public static final String QOS  = "qos";

    public static final String RETAIN = "retain";

    public static final String DUP = "dup";

    public static final DeviceMessage buildDeviceMessage(boolean retain,int qos,String topic,byte[] content){
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setMsgId(null);
        deviceMessage.setContent(content);
        deviceMessage.setSource(MessageSourceEnum.DEVICE);
        deviceMessage.setStoredTime(new Date());
        deviceMessage.setTopic(topic);

        Map<String,Object> properties = new HashMap<>();
        properties.put(QOS,qos);
        properties.put(RETAIN,retain);
        deviceMessage.setProperties(properties);
        return deviceMessage;
    }

    public static final DeviceMessage buildDeviceMessage(MqttPublishMessage mqttPublishMessage){
        return buildDeviceMessage(mqttPublishMessage.fixedHeader().isRetain(),mqttPublishMessage.fixedHeader().qosLevel().value(),
                mqttPublishMessage.variableHeader().topicName(),mqttPublishMessage.content().array());
    }

    public static Subscription convert(DeviceSubscription deviceSubscription) {

        Map<String,Object> properties = deviceSubscription.getProperties();
        int qos = 1;
        if (properties != null && properties.containsKey(QOS)) {
            qos = (int) properties.get(QOS);
        }

        return new Subscription(deviceSubscription.getClientId(),deviceSubscription.getTopic(),qos);
    }

    public static DeviceSubscription convert(Subscription subscription) {

        DeviceSubscription deviceSubscription = new DeviceSubscription();
        deviceSubscription.setClientId(subscription.getClientId());
        deviceSubscription.setTopic(subscription.getTopic());

        Map<String,Object> properties = new HashMap<>();
        properties.put(QOS,subscription.getQos());
        deviceSubscription.setProperties(properties);
        return deviceSubscription;
    }

}
