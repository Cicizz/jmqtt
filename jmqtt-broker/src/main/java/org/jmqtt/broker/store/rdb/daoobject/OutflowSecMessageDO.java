package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;

public class OutflowSecMessageDO implements Serializable {

    private static final long serialVersionUID = 543213131231231L;

    private Long id;

    private String clientId;

    private Integer msgId;

    private Long gmtCreate;

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

    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
}
