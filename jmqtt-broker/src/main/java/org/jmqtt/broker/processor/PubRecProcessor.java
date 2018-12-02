package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.dispatcher.FlowMessage;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubRecProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private FlowMessage flowMessage;

    public PubRecProcessor(FlowMessage flowMessage){
        this.flowMessage = flowMessage;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        log.debug("[PubComp] -> Recieve PubRec message,clientId={},msgId={}",clientId,messageId);
        if(!flowMessage.containSendMsg(clientId,messageId)){
            log.warn("[PubRec] -> The message is not cached in Flow,clientId={},msgId={}",clientId,messageId);
        }
        MqttMessage pubRelMessage = MessageUtil.getPubRelMessage(messageId);
        ctx.writeAndFlush(pubRelMessage);
    }
}
