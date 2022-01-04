package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.mqtt.utils.MqttMessageUtil;

/**
 * 心跳
 */
public class PingProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessage pingRespMessage = MqttMessageUtil.getPingRespMessage();
        ctx.writeAndFlush(pingRespMessage);
    }
}
