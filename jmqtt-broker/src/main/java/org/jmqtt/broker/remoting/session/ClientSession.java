package org.jmqtt.broker.remoting.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本服务连接的设备会话信息
 */
public class ClientSession {

    /**
     * clientId uniqu in a cluseter
     */
    private           String                clientId;
    private           boolean               cleanStart;
    private transient ChannelHandlerContext ctx;

    private transient AtomicInteger messageIdCounter = new AtomicInteger(1);

    public ClientSession() {}

    public ClientSession(String clientId, boolean cleanStart) {
        this.clientId = clientId;
        this.cleanStart = cleanStart;
    }

    public ClientSession(String clientId, boolean cleanStart, ChannelHandlerContext ctx) {
        this.clientId = clientId;
        this.cleanStart = cleanStart;
        this.ctx = ctx;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isCleanStart() {
        return cleanStart;
    }

    public void setCleanStart(boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public int generateMessageId() {
        int messageId = messageIdCounter.getAndIncrement();
        messageId = Math.abs(messageId % 0xFFFF);
        if (messageId == 0) {
            return generateMessageId();
        }
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ClientSession that = (ClientSession) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
