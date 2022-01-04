
package org.jmqtt.bus.model;

import org.jmqtt.bus.enums.MessageSourceEnum;

import java.util.Date;
import java.util.Map;

public class DeviceMessage {

    private Long id;

    private byte[] content;

    private MessageSourceEnum source;

    private String fromClientId;

    private Date storedTime;

    private String topic;

    private Map<String,Object> properties;

    public <T> T  getProperty(String key){
        if (properties == null || properties.containsKey(key)) {
            return (T) properties.get(key);
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public MessageSourceEnum getSource() {
        return source;
    }

    public void setSource(MessageSourceEnum source) {
        this.source = source;
    }

    public Date getStoredTime() {
        return storedTime;
    }

    public void setStoredTime(Date storedTime) {
        this.storedTime = storedTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getFromClientId() {
        return fromClientId;
    }

    public void setFromClientId(String fromClientId) {
        this.fromClientId = fromClientId;
    }
}
