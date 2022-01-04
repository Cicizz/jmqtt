package org.jmqtt.bus.model;

import java.util.Date;
import java.util.Map;

public class DeviceSubscription {

    private String clientId;

    private String topic;

    private Date subscribeTime;

    private Map<String,Object> properties;


    public <T> T getProperty(String key){
        if (properties == null) {
            return null;
        }
        return (T) properties.get(key);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getSubscribeTime() {
        return subscribeTime;
    }

    public void setSubscribeTime(Date subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
