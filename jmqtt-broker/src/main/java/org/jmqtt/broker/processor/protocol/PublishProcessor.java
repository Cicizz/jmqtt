package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.ReferenceCountUtil;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.acl.AuthValid;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.MessageHeader;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端publish消息到jmqtt broker
 */
public class PublishProcessor extends AbstractMessageProcessor implements RequestProcessor {
    private static final Logger log     = JmqttLogger.messageTraceLog;

    private AuthValid authValid;

    public PublishProcessor(BrokerController controller) {
        super(controller);
        this.authValid = controller.getAuthValid();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        try {
            MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
            MqttQoS qos = publishMessage.fixedHeader().qosLevel();
            Message innerMsg = new Message();
            String clientId = NettyUtil.getClientId(ctx.channel());
            ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
            String topic = publishMessage.variableHeader().topicName();
            if (!this.authValid.publishVerify(clientId, topic)) {
                LogUtil.warn(log, "[PubMessage] permission is not allowed");
                clientSession.getCtx().close();
                return;
            }
            innerMsg.setPayload(MessageUtil.readBytesFromByteBuf(((MqttPublishMessage) mqttMessage).payload()));
            innerMsg.setClientId(clientId);
            innerMsg.setType(Message.Type.valueOf(mqttMessage.fixedHeader().messageType().value()));
            Map<String, Object> headers = new HashMap<>();
            headers.put(MessageHeader.TOPIC, publishMessage.variableHeader().topicName());
            headers.put(MessageHeader.QOS, publishMessage.fixedHeader().qosLevel().value());
            headers.put(MessageHeader.RETAIN, publishMessage.fixedHeader().isRetain());
            headers.put(MessageHeader.DUP, publishMessage.fixedHeader().isDup());
            innerMsg.setHeaders(headers);
            innerMsg.setMsgId(publishMessage.variableHeader().packetId());
            innerMsg.setBizCode(clientSession.getBizCode());
            innerMsg.setTenantCode(clientSession.getTenantCode());

            switch (qos) {
                case AT_MOST_ONCE:
                    processMessage(innerMsg);
                    break;
                case AT_LEAST_ONCE:
                    processQos1(ctx, innerMsg);
                    break;
                case EXACTLY_ONCE:
                    processQos2(ctx, innerMsg);
                    break;
                default:
                    LogUtil.warn(log, "[PubMessage] -> Wrong mqtt message,clientId={}", clientId);
            }
        } catch (Throwable tr) {
            LogUtil.warn(log, "[PubMessage] -> Solve mqtt pub message exception:{}", tr.getMessage());
        } finally {
            ReferenceCountUtil.release(mqttMessage.payload());
        }
    }


    private void processQos2(ChannelHandlerContext ctx, Message innerMsg) {
        int originMessageId = innerMsg.getMsgId();
        LogUtil.debug(log, "[PubMessage] -> Process qos2 message,clientId={}", innerMsg.getClientId());
        boolean flag = cacheInflowMsg(innerMsg.getClientId(), innerMsg);
        if (!flag) {
            LogUtil.warn(log, "[PubMessage] -> cache qos2 pub message failure,clientId={}", innerMsg.getClientId());
        }
        MqttMessage pubRecMessage = MessageUtil.getPubRecMessage(originMessageId);
        ctx.writeAndFlush(pubRecMessage);
    }

    private void processQos1(ChannelHandlerContext ctx, Message innerMsg) {
        int originMessageId = innerMsg.getMsgId();
        processMessage(innerMsg);
        LogUtil.info(log, "[PubMessage] -> Process qos1 message,clientId={}", innerMsg.getClientId());
        MqttPubAckMessage pubAckMessage = MessageUtil.getPubAckMessage(originMessageId);
        ctx.writeAndFlush(pubAckMessage);
    }

}
