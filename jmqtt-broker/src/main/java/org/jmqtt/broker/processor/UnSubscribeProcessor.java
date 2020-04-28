package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ClientSession;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.MessageUtil;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.SubscriptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class UnSubscribeProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);

    private SubscriptionMatcher subscriptionMatcher;
    private SubscriptionStore subscriptionStore;

    public UnSubscribeProcessor(SubscriptionMatcher subscriptionMatcher,SubscriptionStore subscriptionStore){
        this.subscriptionMatcher = subscriptionMatcher;
        this.subscriptionStore = subscriptionStore;
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
            subscriptionStore.removeSubscription(clientId,topic);
        });
        MqttUnsubAckMessage unsubAckMessage = MessageUtil.getUnSubAckMessage(MessageUtil.getMessageId(mqttMessage));
        ctx.writeAndFlush(unsubAckMessage);
    }
}
