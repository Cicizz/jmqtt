package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.common.model.Message;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.remoting.util.RemotingHelper;
import org.jmqtt.store.FlowMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PubRelProcessor extends AbstractMessageProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.MESSAGE_TRACE);

    private FlowMessageStore flowMessageStore;

    public PubRelProcessor(BrokerController controller) {
        super(controller.getMessageDispatcher(),controller.getRetainMessageStore(),controller.getClusterMessageTransfer());
        this.flowMessageStore = controller.getFlowMessageStore();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        if(ConnectManager.getInstance().containClient(clientId)){
            Message message = flowMessageStore.releaseRecMsg(clientId,messageId);
            if(Objects.nonNull(message)){
                super.processMessage(message);
            }else{
                log.warn("[PubRelMessage] -> the message is not exist,clientId={},messageId={}.",clientId,messageId);
            }
            MqttMessage pubComMessage = MessageUtil.getPubComMessage(messageId);
            ctx.writeAndFlush(pubComMessage);
        }else{
            log.warn("[PubRelMessage] -> the client：{} disconnect to this server.",clientId);
            RemotingHelper.closeChannel(ctx.channel());
        }
    }
}
