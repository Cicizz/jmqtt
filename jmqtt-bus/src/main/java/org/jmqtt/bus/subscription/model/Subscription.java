package org.jmqtt.bus.subscription.model;

import java.util.Map;
import java.util.Objects;

/**
 * 订阅关系
 */
public class Subscription {
    private String             clientId;
    private String             topic;
    private Map<String,Object> properties;

    public Subscription(String clientId,String topic){
        this.clientId = clientId;
        this.topic = topic;
    }

    public Subscription(String clientId,String topic,Map<String,Object> properties){
        this.clientId = clientId;
        this.topic = topic;
        this.properties = properties;
    }

    public Subscription(Subscription origin){
        this.clientId = origin.getClientId();
        this.topic = origin.getTopic();
        this.properties = origin.getProperties();
    }

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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, topic);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "clientId='" + clientId + '\'' +
                ", topic='" + topic + '\'' +
                ", properties=" + properties +
                '}';
    }
}
