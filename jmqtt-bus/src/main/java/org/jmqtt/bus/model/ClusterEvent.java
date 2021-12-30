package org.jmqtt.bus.model;

import org.jmqtt.bus.enums.ClusterEventCodeEnum;

import java.util.Date;

public class ClusterEvent {

    private Long id;

    private String content;

    private Date gmtCreate;

    private String nodeIp;

    private ClusterEventCodeEnum clusterEventCode;

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

    public ClusterEventCodeEnum getClusterEventCode() {
        return clusterEventCode;
    }

    public void setClusterEventCode(ClusterEventCodeEnum clusterEventCode) {
        this.clusterEventCode = clusterEventCode;
    }
}
