package org.jmqtt.broker.processor.dispatcher.event;

import lombok.Data;
import lombok.ToString;
import org.jmqtt.broker.store.rdb.daoobject.TenantBase;

import java.io.Serializable;

/**
 * cluster event model
 */
@ToString
@Data
public class Event extends TenantBase implements Serializable {

    private static final long serialVersionUID = -12893791131231231L;

    /**
     * {@link EventCode}
     */
    private int eventCode;

    private String body;

    private long sendTime;

    private String fromIp;

    public Event(int eventCode, String body,long sendTime,String fromIp) {
        this.eventCode = eventCode;
        this.body = body;
        this.sendTime = sendTime;
        this.fromIp = fromIp;
    }
}
