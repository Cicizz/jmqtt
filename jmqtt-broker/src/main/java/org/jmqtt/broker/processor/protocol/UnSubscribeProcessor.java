package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import org.jmqtt.broker.common.log.LoggerName;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * 取消订阅
 */
public class UnSubscribeProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);

    private SubscriptionMatcher subscriptionMatcher;
    private SessionStore        sessionStore;

    public UnSubscribeProcessor(SubscriptionMatcher subscriptionMatcher,SessionStore sessionStore){
        this.subscriptionMatcher = subscriptionMatcher;
        this.sessionStore = sessionStore;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttUnsubscribeMessage unsubscribeMessage = (MqttUnsubscribeMessage) mqttMessage;
        MqttUnsubscribePayload unsubscribePayload = unsubscribeMessage.payload();
        List<String> topics = unsubscribePayload.topics();
        String clientId = NettyUtil.getClientId(ctx.channel());
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        if(Objects.isNull(clientSession)){
            log.warn("[UnSubscribe] -> The client is not online.clientId={}",clientId);
        }
        topics.forEach( topic -> {
            subscriptionMatcher.unSubscribe(topic,clientId);
            sessionStore.delSubscription(clientId,topic);
        });
        MqttUnsubAckMessage unsubAckMessage = MessageUtil.getUnSubAckMessage(MessageUtil.getMessageId(mqttMessage));
        ctx.writeAndFlush(unsubAckMessage);
    }
}
