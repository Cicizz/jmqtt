package org.jmqtt.mqtt.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     * ps：阅读mqtt协议代码从这里的实现开始
     */
    void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage);
}
