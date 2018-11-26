package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.jmqtt.common.bean.ClientSession;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PublishProcessor implements RequestProcessor {
    private Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);


    @Override
    public void processRequest(ChannelHandlerContext ctx, Message message) {
        MqttPublishMessage publishMessage = (MqttPublishMessage) message.getPayload();
        MqttQoS qos = publishMessage.fixedHeader().qosLevel();
        Message innerMsg = new Message();
        String clientId = NettyUtil.getClientId(ctx.channel());
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        innerMsg.setClientSession(clientSession);
        innerMsg.setPayload(publishMessage.payload());
        innerMsg.setType(message.getType());
        Map<String,Object> headers = new HashMap<>();
        headers.put(MessageHeader.TOPIC,publishMessage.variableHeader().topicName());
        headers.put(MessageHeader.QOS,publishMessage.fixedHeader().qosLevel().value());
        headers.put(MessageHeader.RETAIN,publishMessage.fixedHeader().isRetain());
        switch (qos){
            case AT_MOST_ONCE:
                break;
            case AT_LEAST_ONCE:
                break;
            case EXACTLY_ONCE:
                break;
            default:
                log.warn("[PubMessage] -> Wrong mqtt message,clientId={}", clientId);
        }
    }

    private void  processQos0(ChannelHandlerContext ctx,Message message){

    }
}
