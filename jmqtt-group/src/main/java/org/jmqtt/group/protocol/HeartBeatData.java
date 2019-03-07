package org.jmqtt.group.protocol;

import java.util.Set;

/**
 * send this message
 */
public class HeartBeatData {
    /**
     * current node's ip address
     */
    private String addr;

    /**
     * all node's that current node has connected
     */
    private Set<String> connectAddrs;

    public HeartBeatData(String addr,Set<String> connectAddrs){
        this.addr = addr;
        this.connectAddrs = connectAddrs;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Set<String> getConnectAddrs() {
        return connectAddrs;
    }

    public void setConnectAddrs(Set<String> connectAddrs) {
        this.connectAddrs = connectAddrs;
    }

    @Override
    public String toString() {
        return "HeartBeatData{" +
                "addr='" + addr + '\'' +
                ", connectAddrs=" + connectAddrs +
                '}';
    }
}
