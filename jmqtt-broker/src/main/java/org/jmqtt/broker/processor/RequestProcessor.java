package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     * ps：阅读代码从这里的实现开始，结合mqtt文档: http://www.mangdagou.com/links
     */
    void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage);
}
