package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.store.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出栈消息接收到qos2第一阶段的的pubRec报文: 不做任何处理，直接返回pubRel报文
 */
public class PubRecProcessor implements RequestProcessor {

    private Logger       log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private SessionStore sessionStore;

    public PubRecProcessor(SessionStore sessionStore){
        this.sessionStore = sessionStore;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        log.debug("[PubRec] -> Receive PubRec message,clientId={},msgId={}",clientId,messageId);
        //if(!sessionStore.containOutflowMsg(clientId,messageId)){
        //    log.warn("[PubRec] -> The message is not cached in Flow,clientId={},msgId={}",clientId,messageId);
        //}
        MqttMessage pubRelMessage = MessageUtil.getPubRelMessage(messageId);
        ctx.writeAndFlush(pubRelMessage);
    }
}
