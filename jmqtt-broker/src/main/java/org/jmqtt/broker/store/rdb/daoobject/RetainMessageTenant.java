package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;

public class RetainMessageTenant extends TenantBase implements Serializable {

    private static final long serialVersionUID = 12213131231231L;

    private Long id;

    private String topic;

    private String content;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
