package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;

/**
 * 出栈消息接收到的qos2消息的pubComp报文：清除缓存的出栈消息
 */
public class PubCompProcessor implements RequestProcessor {


    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        mqttConnection.processPubComp(mqttMessage);
    }
}
