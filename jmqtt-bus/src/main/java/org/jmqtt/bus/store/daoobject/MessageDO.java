package org.jmqtt.bus.store.daoobject;

import java.io.Serializable;
import java.util.Date;

public class MessageDO implements Serializable {

    private static final long serialVersionUID = 12313131231231L;

    private Long id;

    private String source;

    private byte[] content;

    private String topic;

    private String fromClientId;

    private Date storedTime;

    private String properties;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFromClientId() {
        return fromClientId;
    }

    public void setFromClientId(String fromClientId) {
        this.fromClientId = fromClientId;
    }

    public Date getStoredTime() {
        return storedTime;
    }

    public void setStoredTime(Date storedTime) {
        this.storedTime = storedTime;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }
}
