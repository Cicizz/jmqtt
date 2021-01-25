package org.jmqtt.broker.common.config;

import java.io.File;

public class BrokerConfig {

    private String jmqttHome = System.getenv("JMQTT_HOME") != null ? System.getenv("JMQTT_HOME") : System.getProperty("user.dir") + File.separator + "jmqtt-distribution";

    private String version = "3.0.0";

    private boolean anonymousEnable = true;

    private int pollThreadNum = Runtime.getRuntime().availableProcessors() * 2;

    private String sessionStoreClass = "";

    private String messageStoreClass = "";

    /**
     * redis相关配置
     */
    private String redisHost = "127.0.0.1";
    private int redisPort = 6379;
    private String redisPassword = "";
    private int maxWaitMills = 60 * 1000;
    private boolean testOnBorrow = true;
    private int minIdle = 20;
    private int maxTotal = 200;
    private int maxIdle = 50;


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
}
