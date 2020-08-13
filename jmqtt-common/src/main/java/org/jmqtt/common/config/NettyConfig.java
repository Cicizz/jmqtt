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
    private boolean startTcp = true;
    private int tcpPort = 1883;

    /**
     * websocket port default 1884
     */
    private boolean startWebsocket = true;
    private int websocketPort = 1884;

    /**
     * tcp port with ssl default 8883
     */
    private boolean startSslTcp = true;
    private int SslTcpPort = 8883;

    /**
     * websocket port with ssl default 8884
     */
    private boolean startSslWebsocket = true;
    private int SslWebsocketPort = 8884;

    /**
     * SSL setting
     */
    private boolean useClientCA = false;
    private String sslKeyStoreType = "PKCS12";
    private String sslKeyFilePath = "/conf/server.pfx";
    private String sslManagerPwd = "654321";
    private String sslStorePwd = "654321";


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

    public boolean isStartTcp() {
        return startTcp;
    }

    public void setStartTcp(boolean startTcp) {
        this.startTcp = startTcp;
    }


    public boolean isStartSslTcp() {
        return startSslTcp;
    }

    public void setStartSslTcp(boolean startSslTcp) {
        this.startSslTcp = startSslTcp;
    }


    public int getSslTcpPort() {
        return SslTcpPort;
    }

    public void setSslTcpPort(int sslTcpPort) {
        SslTcpPort = sslTcpPort;
    }

    public boolean isStartSslWebsocket() {
        return startSslWebsocket;
    }

    public void setStartSslWebsocket(boolean startSslWebsocket) {
        this.startSslWebsocket = startSslWebsocket;
    }

    public int getSslWebsocketPort() {
        return SslWebsocketPort;
    }

    public void setSslWebsocketPort(int sslWebsocketPort) {
        SslWebsocketPort = sslWebsocketPort;
    }

    public boolean isUseClientCA() {
        return useClientCA;
    }

    public void setUseClientCA(boolean useClientCA) {
        this.useClientCA = useClientCA;
    }

    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    public void setSslKeyStoreType(String sslKeyStoreType) {
        this.sslKeyStoreType = sslKeyStoreType;
    }

    public String getSslKeyFilePath() {
        return sslKeyFilePath;
    }

    public void setSslKeyFilePath(String sslKeyFilePath) {
        this.sslKeyFilePath = sslKeyFilePath;
    }

    public String getSslManagerPwd() {
        return sslManagerPwd;
    }

    public void setSslManagerPwd(String sslManagerPwd) {
        this.sslManagerPwd = sslManagerPwd;
    }

    public String getSslStorePwd() {
        return sslStorePwd;
    }

    public void setSslStorePwd(String sslStorePwd) {
        this.sslStorePwd = sslStorePwd;
    }
}
