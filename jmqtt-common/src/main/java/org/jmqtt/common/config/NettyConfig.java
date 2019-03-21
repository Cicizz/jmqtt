package org.jmqtt.common.config;

public class NettyConfig {

    private int selectorThreadNum = 3;
    private int ioThreadNum = 8;
    private int tcpBackLog = 1024;
    private boolean tcpNoDelay = false;
    private boolean tcpReuseAddr = true;
    private boolean tcpKeepAlive = false;
    private int tcpSndBuf = 65536;
    private int tcpRcvBuf = 65536;
    private boolean useEpoll = false;
    private boolean pooledByteBufAllocatorEnable = false;

    /**
     * tcp port default 1883
     */
    private int tcpPort = 1883;

    private boolean startWebsocket = true;
    /**
     * websocket port default 1884
     */
    private int websocketPort = 1884;

    /**
     * max mqtt message size
     */
    private int maxMsgSize = 512*1024;

    public int getSelectorThreadNum() {
        return selectorThreadNum;
    }

    public void setSelectorThreadNum(int selectorThreadNum) {
        this.selectorThreadNum = selectorThreadNum;
    }

    public int getIoThreadNum() {
        return ioThreadNum;
    }

    public void setIoThreadNum(int ioThreadNum) {
        this.ioThreadNum = ioThreadNum;
    }

    public int getTcpBackLog() {
        return tcpBackLog;
    }

    public void setTcpBackLog(int tcpBackLog) {
        this.tcpBackLog = tcpBackLog;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isTcpReuseAddr() {
        return tcpReuseAddr;
    }

    public void setTcpReuseAddr(boolean tcpReuseAddr) {
        this.tcpReuseAddr = tcpReuseAddr;
    }

    public boolean isTcpKeepAlive() {
        return tcpKeepAlive;
    }

    public void setTcpKeepAlive(boolean tcpKeepAlive) {
        this.tcpKeepAlive = tcpKeepAlive;
    }

    public int getTcpSndBuf() {
        return tcpSndBuf;
    }

    public void setTcpSndBuf(int tcpSndBuf) {
        this.tcpSndBuf = tcpSndBuf;
    }

    public int getTcpRcvBuf() {
        return tcpRcvBuf;
    }

    public void setTcpRcvBuf(int tcpRcvBuf) {
        this.tcpRcvBuf = tcpRcvBuf;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getMaxMsgSize() {
        return maxMsgSize;
    }

    public void setMaxMsgSize(int maxMsgSize) {
        this.maxMsgSize = maxMsgSize;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public boolean isPooledByteBufAllocatorEnable() {
        return pooledByteBufAllocatorEnable;
    }

    public void setPooledByteBufAllocatorEnable(boolean pooledByteBufAllocatorEnable) {
        this.pooledByteBufAllocatorEnable = pooledByteBufAllocatorEnable;
    }

    public boolean isStartWebsocket() {
        return startWebsocket;
    }

    public void setStartWebsocket(boolean startWebsocket) {
        this.startWebsocket = startWebsocket;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }


}
