package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.util.MessageUtil;

public class PingProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessage pingRespMessage = MessageUtil.getPingRespMessage();
        ctx.writeAndFlush(pingRespMessage);
    }
}
