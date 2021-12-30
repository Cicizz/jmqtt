package org.jmqtt.bus.store.daoobject;

import java.io.Serializable;

public class RetainMessageDO implements Serializable {

    private static final long serialVersionUID = 12213131231231L;

    private Long id;

    private String topic;

    private Long messageId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
