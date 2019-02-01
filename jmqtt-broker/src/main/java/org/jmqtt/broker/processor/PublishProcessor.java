package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.acl.PubSubPermission;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.broker.dispatcher.MessageDispatcher;
import org.jmqtt.common.bean.ClientSession;
import org.jmqtt.common.bean.Message;
import org.jmqtt.common.bean.MessageHeader;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.RetainMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PublishProcessor extends AbstractMessageProcessor implements RequestProcessor {
    private Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);

    private FlowMessageStore flowMessageStore;

    private PubSubPermission pubSubPermission;

    public PublishProcessor(BrokerController controller){
        super(controller.getMessageDispatcher(),controller.getRetainMessageStore());
        this.flowMessageStore = controller.getFlowMessageStore();
        this.pubSubPermission = controller.getPubSubPermission();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
        MqttQoS qos = publishMessage.fixedHeader().qosLevel();
        Message innerMsg = new Message();
        String clientId = NettyUtil.getClientId(ctx.channel());
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        String topic = publishMessage.variableHeader().topicName();
        if(!this.pubSubPermission.publishVerfy(clientId,topic)){
            log.warn("[PubMessage] permission is not allowed");
            clientSession.getCtx().close();
            return;
        }
        innerMsg.setClientSession(clientSession);
        innerMsg.setPayload(MessageUtil.readBytesFromByteBuf(publishMessage.payload()));
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
                processQos2(ctx,innerMsg);
                break;
            default:
                log.warn("[PubMessage] -> Wrong mqtt message,clientId={}", clientId);
        }
    }

    private void processQos2(ChannelHandlerContext ctx,Message innerMsg){
        log.debug("[PubMessage] -> Process qos2 message,clientId={}",innerMsg.getClientSession().getClientId());
        boolean flag = flowMessageStore.cacheRecMsg(innerMsg.getClientSession().getClientId(),innerMsg);
        if(!flag){
            log.warn("[PubMessage] -> cache qos2 pub message failure,clientId={}",innerMsg.getClientSession().getClientId());
        }
        MqttMessage pubRecMessage = MessageUtil.getPubRecMessage(innerMsg.getMsgId());
        ctx.writeAndFlush(pubRecMessage);
    }

    private void processQos1(ChannelHandlerContext ctx,Message innerMsg){
        processMessage(innerMsg);
        log.debug("[PubMessage] -> Process qos1 message,clientId={}",innerMsg.getClientSession().getClientId());
        MqttPubAckMessage pubAckMessage = MessageUtil.getPubAckMessage(innerMsg.getMsgId());
        ctx.writeAndFlush(pubAckMessage);
    }

}
