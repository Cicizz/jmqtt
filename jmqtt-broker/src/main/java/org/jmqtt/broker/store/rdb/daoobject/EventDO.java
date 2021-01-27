package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;
import java.util.Date;

public class EventDO implements Serializable {

    private static final long serialVersionUID = 12213213131231231L;

    private long id;

    private String content;

    private Date gmtCreate;

    private String jmqttIp;

    private String eventCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
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
