package org.jmqtt.mqtt.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.jmqtt.bus.model.DeviceMessage;

import java.util.List;

/**
 * transfer message from Message and MqttMessage
 */
public class MqttMessageUtil {

    public static byte[] readBytesFromByteBuf(ByteBuf byteBuf){
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }


    public static MqttUnsubAckMessage getUnSubAckMessage(int messageId){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttUnsubAckMessage(fixedHeader,idVariableHeader);
    }

    public static int getMessageId(MqttMessage mqttMessage){
        MqttMessageIdVariableHeader idVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        return idVariableHeader.messageId();
    }

    public static int getMinQos(int qos1,int qos2){
        if(qos1 < qos2){
            return qos1;
        }
        return qos2;
    }

    public static MqttMessage getPubRelMessage(int messageId){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttMessage(fixedHeader,idVariableHeader);
    }

    public static MqttPublishMessage getPubMessage(DeviceMessage message,int packetId){
        Integer qos = message.getProperty(MqttMsgHeader.QOS);
        qos = qos == null ? 1 : qos;

        Boolean dup = message.getProperty(MqttMsgHeader.DUP);
        dup = dup == null ? false : dup;

        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH,dup,MqttQoS.valueOf(qos),false,0);
        MqttPublishVariableHeader publishVariableHeader = new MqttPublishVariableHeader(message.getTopic(),packetId);
        ByteBuf heapBuf;
        if(message.getContent() == null){
            heapBuf = Unpooled.EMPTY_BUFFER;
        }else{
            heapBuf = Unpooled.wrappedBuffer(message.getContent());
        }
        return new MqttPublishMessage(fixedHeader,publishVariableHeader,heapBuf);
    }

    public static MqttMessage getSubAckMessage(int messageId, List<Integer> qos){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubAckPayload subAckPayload = new MqttSubAckPayload(qos);
        return new MqttSubAckMessage(fixedHeader,idVariableHeader,subAckPayload);
    }

    public static MqttMessage getPingRespMessage(){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader);
        return mqttMessage;
    }

    public static MqttMessage getPubComMessage(int messageId){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader,MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttMessage getPubRecMessage(int messageId){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader,MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttMessage getPubRecMessage(int messageId,boolean isDup){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,isDup,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader,MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttPubAckMessage getPubAckMessage(int messageId){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(fixedHeader,idVariableHeader);
    }

    public static MqttConnAckMessage getConnectAckMessage(MqttConnectReturnCode returnCode,boolean sessionPresent){
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader variableHeade = new MqttConnAckVariableHeader(returnCode,sessionPresent);
        return new MqttConnAckMessage(fixedHeader,variableHeade);
    }
}
