package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.jmqtt.broker.dispatcher.FlowMessage;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.bean.ClientSession;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PublishProcessor implements RequestProcessor {
    private Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);

    private MessageDispatcher messageDispatcher;
    private FlowMessage flowMessage;

    public PublishProcessor(MessageDispatcher messageDispatcher,FlowMessage flowMessage){
        this.messageDispatcher = messageDispatcher;
        this.flowMessage = flowMessage;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
        MqttQoS qos = publishMessage.fixedHeader().qosLevel();
        Message innerMsg = new Message();
        String clientId = NettyUtil.getClientId(ctx.channel());
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        innerMsg.setClientSession(clientSession);
        innerMsg.setPayload(publishMessage.payload());
        innerMsg.setType(Message.Type.valueOf(mqttMessage.fixedHeader().messageType().value()));
        Map<String,Object> headers = new HashMap<>();
        headers.put(MessageHeader.TOPIC,publishMessage.variableHeader().topicName());
        headers.put(MessageHeader.QOS,publishMessage.fixedHeader().qosLevel().value());
        headers.put(MessageHeader.RETAIN,publishMessage.fixedHeader().isRetain());
        headers.put(MessageHeader.DUP,publishMessage.fixedHeader().isDup());
        innerMsg.setHeaders(headers);
        innerMsg.setMsgId(publishMessage.variableHeader().packetId());
        switch (qos){
            case AT_MOST_ONCE:
                processMessage(innerMsg);
                break;
            case AT_LEAST_ONCE:
                processQos1(ctx,innerMsg);
                break;
            case EXACTLY_ONCE:
                break;
            default:
                log.warn("[PubMessage] -> Wrong mqtt message,clientId={}", clientId);
        }
    }

    private void processQos2(ChannelHandlerContext ctx,Message innerMsg){

    }

    private void processQos1(ChannelHandlerContext ctx,Message innerMsg){
        processMessage(innerMsg);
        log.debug("[PubMessage] -> Process qos1 message,clientId={}",innerMsg.getClientSession().getClientId());
        MqttPubAckMessage pubAckMessage = MessageUtil.getPubAckMessage(innerMsg.getMsgId());
        ctx.writeAndFlush(pubAckMessage);
    }

    private void  processMessage(Message message){
        this.messageDispatcher.appendMessage(message);
        boolean retain = (boolean) message.getHeader(MessageHeader.RETAIN);
        if(retain){
            //TODO  处理retain消息

        }
    }
}
