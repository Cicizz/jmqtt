package org.jmqtt.support.config;

import java.io.File;

public class BrokerConfig {

    // 配置conf文件的所在位置，logback，properties文件等所在位置
    private String jmqttHome = System.getenv("JMQTT_HOME") != null ? System.getenv("JMQTT_HOME") : System.getProperty("user.dir") +
            File.separator + "jmqtt-broker" + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    private String logLevel = "INFO";

    private String  version         = "3.0.2";
    private boolean anonymousEnable = true;

    private int pollThreadNum = Runtime.getRuntime().availableProcessors() * 2;

    // 采用拉消息方式时，一次最多拉的消息数目
    private int maxPollEventNum  = 10;
    private int pollWaitInterval = 10;//ms

    // plugin class config
    private String sessionStoreClass        = "org.jmqtt.broker.store.rdb.RDBSessionStore";
    private String messageStoreClass        = "org.jmqtt.broker.store.rdb.RDBMessageStore";
    private String authValidClass           = "org.jmqtt.broker.acl.impl.DefaultAuthValid";
    private String clusterEventHandlerClass = "org.jmqtt.broker.processor.dispatcher.rdb.RDBClusterEventHandler";

    /* redis相关配置 */
    private String  redisHost     = "127.0.0.1";
    private int     redisPort     = 6379;
    private String  redisPassword = "";
    private int     maxWaitMills  = 60 * 1000;
    private boolean testOnBorrow  = true;
    private int     minIdle       = 20;
    private int     maxTotal      = 200;
    private int     maxIdle       = 50;

    /* db相关配置 */
    private String driver   = "com.mysql.jdbc.Driver";
    private String url
                            = "jdbc:mysql://localhost:3306/jmqtt?characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false"
            + "&maxReconnects=10&useSSL=false";
    private String username = "root";
    private String password = "123456";

    // 是否启用高性能模式，高性能模式下：入栈消息，出栈消息等过程消息都会默认采用内存缓存，若为false，则会用具体实现的存储缓存这一阶段的消息
    private boolean highPerformance = true;

    private String akkaConfigName = "akka";

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public int getMaxWaitMills() {
        return maxWaitMills;
    }

    public void setMaxWaitMills(int maxWaitMills) {
        this.maxWaitMills = maxWaitMills;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getPollThreadNum() {
        return pollThreadNum;
    }

    public void setPollThreadNum(int pollThreadNum) {
        this.pollThreadNum = pollThreadNum;
    }

    public String getJmqttHome() {
        return jmqttHome;
    }

    public void setJmqttHome(String jmqttHome) {
        this.jmqttHome = jmqttHome;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean getAnonymousEnable() {
        return anonymousEnable;
    }

    public void setAnonymousEnable(boolean anonymousEnable) {
        this.anonymousEnable = anonymousEnable;
    }

    public String getSessionStoreClass() {
        return sessionStoreClass;
    }

    public void setSessionStoreClass(String sessionStoreClass) {
        this.sessionStoreClass = sessionStoreClass;
    }

    public String getMessageStoreClass() {
        return messageStoreClass;
    }

    public void setMessageStoreClass(String messageStoreClass) {
        this.messageStoreClass = messageStoreClass;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAnonymousEnable() {
        return anonymousEnable;
    }

    public boolean isHighPerformance() {
        return highPerformance;
    }

    public void setHighPerformance(boolean highPerformance) {
        this.highPerformance = highPerformance;
    }

    public String getAuthValidClass() {
        return authValidClass;
    }

    public void setAuthValidClass(String authValidClass) {
        this.authValidClass = authValidClass;
    }

    public String getClusterEventHandlerClass() {
        return clusterEventHandlerClass;
    }

    public void setClusterEventHandlerClass(String clusterEventHandlerClass) {
        this.clusterEventHandlerClass = clusterEventHandlerClass;
    }

    public int getMaxPollEventNum() {
        return maxPollEventNum;
    }

    public void setMaxPollEventNum(int maxPollEventNum) {
        this.maxPollEventNum = maxPollEventNum;
    }

    public int getPollWaitInterval() {
        return pollWaitInterval;
    }

    public void setPollWaitInterval(int pollWaitInterval) {
        this.pollWaitInterval = pollWaitInterval;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getAkkaConfigName() {
        return akkaConfigName;
    }

    public void setAkkaConfigName(String akkaConfigName) {
        this.akkaConfigName = akkaConfigName;
    }
}
