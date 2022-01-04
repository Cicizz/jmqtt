package org.jmqtt.mqtt.protocol.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.mqtt.MQTTConnection;
import org.jmqtt.mqtt.netty.MqttNettyUtils;
import org.jmqtt.mqtt.protocol.RequestProcessor;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.jmqtt.support.remoting.RemotingHelper;
import org.slf4j.Logger;

/**
 * 客户端的pubRel报文：入栈消息qos2的第二阶段：
 * 1. 消息所有者转换为broker,开始分发消息
 * 2. 返回pubCom报文
 */
public class PubRelProcessor implements RequestProcessor {

    private static final Logger log = JmqttLogger.messageTraceLog;

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MQTTConnection mqttConnection = MqttNettyUtils.mqttConnection(ctx.channel());
        if(mqttConnection != null){
            mqttConnection.processPubRelMessage(mqttMessage);
        }else{
            LogUtil.warn(log,"[PubRelMessage] -> the client：{} disconnect to this server.",mqttConnection.getClientId());
            RemotingHelper.closeChannel(ctx.channel());
        }
    }
}
