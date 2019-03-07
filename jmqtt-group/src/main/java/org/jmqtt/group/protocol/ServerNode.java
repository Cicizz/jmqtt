package org.jmqtt.group.protocol;

import io.netty.channel.Channel;

import java.util.Date;

/**
 * save jmqtt server node data
 */
public class ServerNode {

    private String nodeName;
    private String addr;
    private transient Channel channel;
    private long lastUpdateTime;

    public ServerNode(String nodeName,String addr){
        this.nodeName = nodeName;
        this.addr = addr;
    }

    public ServerNode(String nodeName,String addr,Channel channel){
        this.nodeName = nodeName;
        this.addr = addr;
        this.channel = channel;
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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "ServerNode{" +
                "nodeName='" + nodeName + '\'' +
                ", addr='" + addr + '\'' +
                ", channel=" + channel +
                ", lastUpdateTime=" + new Date(lastUpdateTime) +
                '}';
    }
}
