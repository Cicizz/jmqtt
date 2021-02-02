package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出栈消息接收到的客户端 pubAck报文：释放缓存的出栈消息
 */
public class PubAckProcessor extends AbstractMessageProcessor implements RequestProcessor {

    private Logger       log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);

    public PubAckProcessor(BrokerController brokerController) {
        super(brokerController);
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        log.info("[PubAck] -> Recieve PubAck message,clientId={},msgId={}", clientId, messageId);
        releaseOutflowMsg(clientId, messageId);
    }
}
