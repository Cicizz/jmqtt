package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;

/**
 * 订阅报文逻辑处理
 * TODO mqtt5协议支持
 */
public class SubscribeProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) mqttMessage;
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        mqttConnection.processSubscribe(subscribeMessage);
    }

}
