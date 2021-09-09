package org.jmqtt.broker.remoting.session;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.ToString;
import org.jmqtt.broker.store.rdb.daoobject.TenantBase;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * client session data
 */
@ToString
@Data
public class ClientSession extends TenantBase {

    /**
     * clientId uniqu in a cluseter
     */
    private           String                clientId;
    private           boolean               cleanStart;
    private transient ChannelHandlerContext ctx;
    private int version;// mqtt version mqtt3.1.1:3   mqtt5:5


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
