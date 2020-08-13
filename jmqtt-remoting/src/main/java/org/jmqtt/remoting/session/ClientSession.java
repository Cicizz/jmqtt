package org.jmqtt.remoting.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSession {

    private String clientId;
    private boolean cleanSession;
    private transient ChannelHandlerContext ctx;

    private transient AtomicInteger messageIdCounter = new AtomicInteger(1);

    public ClientSession(){}

    public ClientSession(String clientId, boolean cleanSession){
        this.clientId = clientId;
        this.cleanSession = cleanSession;
    }

    public ClientSession(String clientId, boolean cleanSession,ChannelHandlerContext ctx){
        this.clientId = clientId;
        this.cleanSession = cleanSession;
        this.ctx = ctx;
    }

    public String getClientId() {
        return clientId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
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
