package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.util.ReferenceCountUtil;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

/**
 * 客户端publish消息到jmqtt broker TODO mqtt5实现,流控处理
 */
public class PublishProcessor implements RequestProcessor {

    private static final Logger log = JmqttLogger.mqttLog;

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        try {
            MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
            MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
            mqttConnection.processPublishMessage(publishMessage);
        } catch (Throwable tr) {
            LogUtil.warn(log, "[PubMessage] -> Solve mqtt pub message exception:{}", tr);
        } finally {
            ReferenceCountUtil.release(mqttMessage.payload());
        }
    }

}
