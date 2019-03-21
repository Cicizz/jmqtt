package org.jmqtt.group.protocol.node;


/**
 * save jmqtt server node data
 */
public class ServerNode {

    private String nodeName;
    /**
     * ip:port
     */
    private String addr;
    private transient long lastUpdateTime;
    private transient boolean active;

    public ServerNode(String nodeName, String addr) {
        this.nodeName = nodeName;
        this.addr = addr;
    }


    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ServerNode{" +
                "nodeName='" + nodeName + '\'' +
                ", addr='" + addr + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", active=" + active +
                '}';
    }
}
