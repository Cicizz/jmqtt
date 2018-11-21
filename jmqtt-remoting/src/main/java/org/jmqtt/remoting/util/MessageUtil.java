package org.jmqtt.remoting.util;

import io.netty.handler.codec.mqtt.*;
import org.jmqtt.remoting.netty.Message;

/**
 * transfer message from Message and MqttMessage
 */
public class MessageUtil {

    public static Message getMessage(MqttMessage mqttMessage){
        Message message = new Message();
        message.setPayload(mqttMessage);
        MqttFixedHeader mqttFixedHead = mqttMessage.fixedHeader();
        message.setType(Message.Type.valueOf(mqttFixedHead.messageType().value()));
        return message;
    }

    public static MqttConnAckMessage getConnectAckMessage(MqttConnectReturnCode returnCode,boolean sessionPresent){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader variableHeade = new MqttConnAckVariableHeader(returnCode,sessionPresent);
        return new MqttConnAckMessage(fixedHeader,variableHeade);
    }
}
