package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;

/**
 * 出栈消息接收到qos2第一阶段的的pubRec报文: 丢弃消息，存储报文标志符
 * 发送pubRel报文
 */
public class PubRecProcessor implements RequestProcessor {


    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        mqttConnection.processPubRec(mqttMessage);
    }
}
