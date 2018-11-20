package org.jmqtt.remoting.util;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.common.bean.Message;

/**
 * transfer message from Message and MqttMessage
 */
public class MessageUtil {

    public static Message getMessage(MqttMessage mqttMessage){
        Message message = new Message();
        message.setPayload(mqttMessage.payload());
        MqttFixedHeader mqttFixedHead = mqttMessage.fixedHeader();
        message.setDup(mqttFixedHead.isDup());
        message.setRetain(mqttFixedHead.isRetain());
        message.setQos(mqttFixedHead.qosLevel().value());
        message.setType(Message.Type.valueOf(mqttFixedHead.messageType().value()));
        return message;
    }
}
