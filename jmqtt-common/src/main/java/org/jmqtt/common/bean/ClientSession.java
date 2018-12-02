package org.jmqtt.common.bean;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSession {

    private String clientId;
    private List<Subscription> subscriptions = new ArrayList<>();
    private boolean cleanSession;
    private ChannelHandlerContext ctx;

    private AtomicInteger messageIdCounter = new AtomicInteger(1);


    public ClientSession(){}

    public ClientSession(String clientId, boolean cleanSession){
        this.clientId = clientId;
        this.cleanSession = cleanSession;
    }
    public String getClientId() {
        return clientId;
    }

    public void subscribe(Subscription subscription){
        this.subscriptions.add(subscription);
    }

    public void unSubscribe(String topic){
        Subscription subscription = new Subscription(clientId,topic,1);
        this.subscriptions.remove(subscription);
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public int generateMessageId(){
        int messageId = messageIdCounter.getAndIncrement();
        messageId = Math.abs( messageId % 0xFFFF);
        if(messageId == 0){
            return generateMessageId();
        }
        return messageId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientSession that = (ClientSession) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
