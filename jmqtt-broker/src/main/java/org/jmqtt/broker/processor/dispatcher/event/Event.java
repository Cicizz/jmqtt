package org.jmqtt.broker.processor.dispatcher.event;

import java.io.Serializable;
import org.jmqtt.broker.common.model.AkkaDefaultSerializable;

/**
 * cluster event model
 */
public class Event implements Serializable {

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

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getFromIp() {
        return fromIp;
    }

    public void setFromIp(String fromIp) {
        this.fromIp = fromIp;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventCode=" + eventCode +
                ", body='" + body + '\'' +
                '}';
    }
}
