package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;
import java.util.Date;

public class OutflowSecMessageDO implements Serializable {

    private static final long serialVersionUID = 543213131231231L;

    private Long id;

    private String clientId;

    private Integer msgId;

    private Date gmtCreate;

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

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
}
