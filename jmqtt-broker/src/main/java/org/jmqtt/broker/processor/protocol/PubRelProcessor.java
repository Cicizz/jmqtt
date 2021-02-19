package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.remoting.util.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 客户端的pubRel报文：入栈消息qos2的第二阶段：
 * 1. 消息所有者转换为broker,开始分发消息
 * 2. 返回pubCom报文
 */
public class PubRelProcessor extends AbstractMessageProcessor implements RequestProcessor {

    private static final Logger log = JmqttLogger.messageTraceLog;

    public PubRelProcessor(BrokerController controller) {
        super(controller);
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int msgId = MessageUtil.getMessageId(mqttMessage);
        if(ConnectManager.getInstance().containClient(clientId)){
            Message message = releaseInflowMsg(clientId,msgId);
            if(Objects.nonNull(message)){
                super.processMessage(message);
            }else{
                LogUtil.warn(log,"[PubRelMessage] -> the message is not exist,clientId={},messageId={}.",clientId,msgId);
            }
            MqttMessage pubComMessage = MessageUtil.getPubComMessage(msgId);
            ctx.writeAndFlush(pubComMessage);
        }else{
            LogUtil.warn(log,"[PubRelMessage] -> the client：{} disconnect to this server.",clientId);
            RemotingHelper.closeChannel(ctx.channel());
        }
    }
}
