package org.jmqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.jmqtt.broker.BrokerController;
import org.jmqtt.broker.subscribe.SubscriptionMatcher;
import org.jmqtt.common.model.Subscription;
import org.jmqtt.remoting.session.ClientSession;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.remoting.netty.RequestProcessor;
import org.jmqtt.remoting.session.ConnectManager;
import org.jmqtt.remoting.util.NettyUtil;
import org.jmqtt.store.SessionStore;
import org.jmqtt.store.SubscriptionStore;
import org.jmqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DisconnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);
    private WillMessageStore willMessageStore;
    private SessionStore sessionStore;
    private SubscriptionStore subscriptionStore;
    private SubscriptionMatcher subscriptionMatcher;

    public DisconnectProcessor(BrokerController brokerController){
        this.willMessageStore = brokerController.getWillMessageStore();
        this.sessionStore = brokerController.getSessionStore();
        this.subscriptionStore = brokerController.getSubscriptionStore();
        this.subscriptionMatcher = brokerController.getSubscriptionMatcher();
    }
    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        if(!ConnectManager.getInstance().containClient(clientId)){
            log.warn("[DISCONNECT] -> {} hasn't connect before",clientId);
        }
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        clearSession(clientSession);
        clearSubscriptions(clientSession);
        clearWillMessage(clientSession.getClientId());
        ConnectManager.getInstance().removeClient(clientId);
        ctx.close();
    }

    private void clearSubscriptions(ClientSession clientSession){
        if(clientSession.isCleanSession()){
            Collection<Subscription> subscriptions = subscriptionStore.getSubscriptions(clientSession.getClientId());
            for(Subscription subscription : subscriptions){
                this.subscriptionMatcher.unSubscribe(subscription.getTopic(),clientSession.getClientId());
            }
            subscriptionStore.clearSubscription(clientSession.getClientId());
        }
    }

    private void clearSession(ClientSession clientSession){
        if(clientSession.isCleanSession()){
            this.sessionStore.clearSession(clientSession.getClientId());
        }else{
            this.sessionStore.setSession(clientSession.getClientId(),System.currentTimeMillis());
        }
    }

    private void clearWillMessage(String clientId){
        if(willMessageStore.hasWillMessage(clientId)){
            willMessageStore.removeWillMessage(clientId);
        }
    }


}
