package org.jmqtt.remoting.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.common.model.Message;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     */
    void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage);
}
