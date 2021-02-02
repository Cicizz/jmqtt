package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;

public class EventDO implements Serializable {

    private static final long serialVersionUID = 12213213131231231L;

    private Long id;

    private String content;

    private Long gmtCreate;

    private String jmqttIp;

    private String eventCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getJmqttIp() {
        return jmqttIp;
    }

    public void setJmqttIp(String jmqttIp) {
        this.jmqttIp = jmqttIp;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }
}
