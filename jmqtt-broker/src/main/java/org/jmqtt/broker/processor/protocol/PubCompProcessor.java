package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出栈消息接收到的qos2消息的pubComp报文：清除缓存的出栈消息
 */
public class PubCompProcessor extends AbstractMessageProcessor implements RequestProcessor {

    private Logger       log = JmqttLogger.messageTraceLog;

    public PubCompProcessor(BrokerController brokerController){
        super(brokerController);
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        boolean flag = releaseOutflowSecMsgId(clientId,messageId);
        LogUtil.debug(log,"[PubComp] -> Receive PubCom and remove the flow message,clientId={},msgId={}",clientId,messageId);
        if(!flag){
            LogUtil.warn(log,"[PubComp] -> The message is not in Flow cache,clientId={},msgId={}",clientId,messageId);
        }
    }
}
