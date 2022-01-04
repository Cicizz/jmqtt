package org.jmqtt.bus.store.daoobject;

import java.io.Serializable;
import java.util.Date;

public class SubscriptionDO implements Serializable {

    private static final long serialVersionUID = 12213131231231L;

    private Long id;

    private String clientId;

    private String topic;

    private Date subscribeTime;

    private String properties;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }
}
