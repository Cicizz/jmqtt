package org.jmqtt.mqtt.utils;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.jmqtt.bus.enums.MessageSourceEnum;
import org.jmqtt.bus.model.DeviceMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MqttMsgHeader {

    public static final String QOS  = "qos";

    public static final String RETAIN = "retain";

    public static final String DUP = "dup";

    public static final String CLEAN_SESSION = "cleansession";

    public static final DeviceMessage buildDeviceMessage(boolean retain,int qos,String topic,byte[] content){
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setId(null);
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
                mqttPublishMessage.variableHeader().topicName(),MqttMessageUtil.readBytesFromByteBuf(mqttPublishMessage.content()));
    }

}
