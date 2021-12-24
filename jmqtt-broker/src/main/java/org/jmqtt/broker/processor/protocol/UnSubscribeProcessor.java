package org.jmqtt.broker.processor.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import org.jmqtt.broker.common.log.JmqttLogger;
import org.jmqtt.broker.common.log.LogUtil;
import org.jmqtt.broker.monitor.MonitorHandler;
import org.jmqtt.broker.processor.RequestProcessor;
import org.jmqtt.broker.remoting.session.ClientSession;
import org.jmqtt.broker.remoting.session.ConnectManager;
import org.jmqtt.broker.remoting.util.MessageUtil;
import org.jmqtt.broker.remoting.util.NettyUtil;
import org.jmqtt.broker.store.SessionStore;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * 取消订阅
 */
public class UnSubscribeProcessor implements RequestProcessor {

    private Logger log = JmqttLogger.messageTraceLog;

    private SubscriptionMatcher subscriptionMatcher;
    private SessionStore        sessionStore;
    private MonitorHandler monitorHandler;

    public UnSubscribeProcessor(SubscriptionMatcher subscriptionMatcher,SessionStore sessionStore,MonitorHandler monitorHandler){
        this.subscriptionMatcher = subscriptionMatcher;
        this.sessionStore = sessionStore;
        this.monitorHandler = monitorHandler;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttUnsubscribeMessage unsubscribeMessage = (MqttUnsubscribeMessage) mqttMessage;
        MqttUnsubscribePayload unsubscribePayload = unsubscribeMessage.payload();
        List<String> topics = unsubscribePayload.topics();
        String clientId = NettyUtil.getClientId(ctx.channel());
        monitorHandler.recordActiveClient(clientId);
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        if(Objects.isNull(clientSession)){
            LogUtil.warn(log,"[UnSubscribe] -> The client is not online.clientId={}",clientId);
        }
        topics.forEach( topic -> {
            subscriptionMatcher.unSubscribe(topic,clientId);
            sessionStore.delSubscription(clientId,topic);
        });
        MqttUnsubAckMessage unsubAckMessage = MessageUtil.getUnSubAckMessage(MessageUtil.getMessageId(mqttMessage));
        ctx.writeAndFlush(unsubAckMessage);
    }
}
