package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;


/**
 * 客户端主动发起断开连接：正常断连
 */
public class DisconnectProcessor implements RequestProcessor {

    private static final Logger              log = JmqttLogger.mqttLog;

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        String clientId = MqttNettyUtils.clientID(ctx.channel());
        if (mqttConnection == null) {
            LogUtil.error(log,"[DISCONNECT] -> {} hasn't connect before",clientId );
            return;
        }
        mqttConnection.processDisconnect();
        ctx.close();
    }

}
