package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.support.log.JmqttLogger;
import org.slf4j.Logger;

/**
 * 出栈消息接收到的客户端 pubAck报文：释放缓存的出栈消息
 */
public class PubAckProcessor implements RequestProcessor {

    private static final Logger log = JmqttLogger.messageTraceLog;

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        mqttConnection.processPubAck(mqttMessage);
    }
}
