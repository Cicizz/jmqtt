package org.jmqtt.common.bean;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class ClientSession {

    private String clientId;
    private List<Subscription> subscriptions;
    private boolean cleanSession;
    private ChannelHandlerContext ctx;



    public ClientSession(){}

    public ClientSession(String clientId, boolean cleanSession){
        this.clientId = clientId;
        this.cleanSession = cleanSession;
    }
    public String getClientId() {
        return clientId;
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
}
