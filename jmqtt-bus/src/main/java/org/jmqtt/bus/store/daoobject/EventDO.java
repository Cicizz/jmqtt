package org.jmqtt.bus.store.daoobject;

import java.io.Serializable;
import java.util.Date;

public class EventDO implements Serializable {

    private static final long serialVersionUID = 12213213131231231L;

    private Long id;

    private String content;

    private Date gmtCreate;

    private String nodeIp;

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

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }
}
