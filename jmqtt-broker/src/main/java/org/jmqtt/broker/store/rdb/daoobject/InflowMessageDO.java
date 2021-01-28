package org.jmqtt.broker.store.rdb.daoobject;

import java.io.Serializable;
import java.util.Date;

public class InflowMessageDO implements Serializable {

    private static final long serialVersionUID = 12313131231231L;

    private Long id;

    private Integer msgId;

    private String content;

    private Date gmtCreate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
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
}
