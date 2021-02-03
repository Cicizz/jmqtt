package org.jmqtt.broker.processor.dispatcher.event;

import java.io.Serializable;

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

    public Event(int eventCode, String body,long sendTime) {
        this.eventCode = eventCode;
        this.body = body;
        this.sendTime = sendTime;
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



    @Override
    public String toString() {
        return "Event{" +
                "eventCode=" + eventCode +
                ", body='" + body + '\'' +
                '}';
    }
}
