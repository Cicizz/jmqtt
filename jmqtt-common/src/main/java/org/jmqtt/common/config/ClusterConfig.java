package org.jmqtt.common.config;

/**
 * cluster group config
 */
public class ClusterConfig {

    private String currentNodeIp = "";
    private String nodeName = "defaultNode:" + (System.currentTimeMillis() & 0xFFFFFF);
    private int groupServerPort = 8880;
    /**
     * cluster node : ip1:port1;ip2;port2
     */
    private String groupNodes = "";
    private long timeoutMills = 3000L;

    /**
     * if cluster transfer message body size > 4069,compress the message body
     */
    private long compressMaxSize = 4069;

    /** group netty config */
    private int groupSelectorThreadNum = 3;
    private int groupIoThreadNum = 8;
    private int groupTcpBackLog = 1024;
    private boolean groupTcpNoDelay = false;
    private boolean groupTcpReuseAddr = true;
    private boolean groupTcpKeepAlive = false;
    private int groupTcpSndBuf = 65536;
    private int groupTcpRcvBuf = 65536;
    private boolean groupUseEpoll = false;
    private boolean groupPooledByteBufAllocatorEnable = false;

    public int getGroupServerPort() {
        return groupServerPort;
    }

    public void setGroupServerPort(int groupServerPort) {
        this.groupServerPort = groupServerPort;
    }

    public int getGroupSelectorThreadNum() {
        return groupSelectorThreadNum;
    }

    public void setGroupSelectorThreadNum(int groupSelectorThreadNum) {
        this.groupSelectorThreadNum = groupSelectorThreadNum;
    }

    public int getGroupIoThreadNum() {
        return groupIoThreadNum;
    }

    public void setGroupIoThreadNum(int groupIoThreadNum) {
        this.groupIoThreadNum = groupIoThreadNum;
    }

    public int getGroupTcpBackLog() {
        return groupTcpBackLog;
    }

    public void setGroupTcpBackLog(int groupTcpBackLog) {
        this.groupTcpBackLog = groupTcpBackLog;
    }

    public boolean isGroupTcpNoDelay() {
        return groupTcpNoDelay;
    }

    public void setGroupTcpNoDelay(boolean groupTcpNoDelay) {
        this.groupTcpNoDelay = groupTcpNoDelay;
    }

    public boolean isGroupTcpReuseAddr() {
        return groupTcpReuseAddr;
    }

    public void setGroupTcpReuseAddr(boolean groupTcpReuseAddr) {
        this.groupTcpReuseAddr = groupTcpReuseAddr;
    }

    public boolean isGroupTcpKeepAlive() {
        return groupTcpKeepAlive;
    }

    public void setGroupTcpKeepAlive(boolean groupTcpKeepAlive) {
        this.groupTcpKeepAlive = groupTcpKeepAlive;
    }

    public int getGroupTcpSndBuf() {
        return groupTcpSndBuf;
    }

    public void setGroupTcpSndBuf(int groupTcpSndBuf) {
        this.groupTcpSndBuf = groupTcpSndBuf;
    }

    public int getGroupTcpRcvBuf() {
        return groupTcpRcvBuf;
    }

    public void setGroupTcpRcvBuf(int groupTcpRcvBuf) {
        this.groupTcpRcvBuf = groupTcpRcvBuf;
    }

    public boolean isGroupUseEpoll() {
        return groupUseEpoll;
    }

    public void setGroupUseEpoll(boolean groupUseEpoll) {
        this.groupUseEpoll = groupUseEpoll;
    }

    public boolean isGroupPooledByteBufAllocatorEnable() {
        return groupPooledByteBufAllocatorEnable;
    }

    public void setGroupPooledByteBufAllocatorEnable(boolean groupPooledByteBufAllocatorEnable) {
        this.groupPooledByteBufAllocatorEnable = groupPooledByteBufAllocatorEnable;
    }

    public String getCurrentNodeIp() {
        return currentNodeIp;
    }

    public void setCurrentNodeIp(String currentNodeIp) {
        this.currentNodeIp = currentNodeIp;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }


    public String getGroupNodes() {
        return groupNodes;
    }

    public void setGroupNodes(String groupNodes) {
        this.groupNodes = groupNodes;
    }

    public long getTimeoutMills() {
        return timeoutMills;
    }

    public void setTimeoutMills(long timeoutMills) {
        this.timeoutMills = timeoutMills;
    }

    public long getCompressMaxSize() {
        return compressMaxSize;
    }

    public void setCompressMaxSize(long compressMaxSize) {
        this.compressMaxSize = compressMaxSize;
    }

    @Override
    public String toString() {
        return "ClusterConfig{" +
                "currentNodeIp='" + currentNodeIp + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", groupServerPort=" + groupServerPort +
                ", groupNodes='" + groupNodes + '\'' +
                ", timeoutMills=" + timeoutMills +
                ", compressMaxSize=" + compressMaxSize +
                ", groupSelectorThreadNum=" + groupSelectorThreadNum +
                ", groupIoThreadNum=" + groupIoThreadNum +
                ", groupTcpBackLog=" + groupTcpBackLog +
                ", groupTcpNoDelay=" + groupTcpNoDelay +
                ", groupTcpReuseAddr=" + groupTcpReuseAddr +
                ", groupTcpKeepAlive=" + groupTcpKeepAlive +
                ", groupTcpSndBuf=" + groupTcpSndBuf +
                ", groupTcpRcvBuf=" + groupTcpRcvBuf +
                ", groupUseEpoll=" + groupUseEpoll +
                ", groupPooledByteBufAllocatorEnable=" + groupPooledByteBufAllocatorEnable +
                '}';
    }
}
