package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.store.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出栈消息接收到的qos2消息的pubComp报文：清楚缓存的出栈消息
 */
public class PubCompProcessor implements RequestProcessor {

    private Logger       log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);
    private SessionStore sessionStore;

    public PubCompProcessor(SessionStore sessionStore){
        this.sessionStore = sessionStore;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        Message message = sessionStore.releaseOutflowMsg(clientId,messageId);
        log.debug("[PubComp] -> Receive PubCom and remove the flow message,clientId={},msgId={}",clientId,messageId);
        if(message == null){
            log.warn("[PubComp] -> The message is not in Flow cache,clientId={},msgId={}",clientId,messageId);
        }
    }
}
