package org.jmqtt.support.config;

import java.io.File;

public class BrokerConfig {

    // 配置conf文件的所在位置，logback，properties文件等所在位置
    private String jmqttHome = System.getenv("JMQTT_HOME") != null ? System.getenv("JMQTT_HOME") : System.getProperty("user.dir") +
            File.separator + "jmqtt-broker" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "conf";
    private String logLevel = "INFO";

    private String  version         = "3.0.2";
    private boolean anonymousEnable = true;

    private int pollThreadNum = Runtime.getRuntime().availableProcessors() * 2;

    // 采用拉消息方式时，一次最多拉的消息数目
    private int maxPollEventNum  = 10;
    private int pollWaitInterval = 10;//ms

    /* db相关配置 */
    private String driver   = "com.mysql.jdbc.Driver";
    private String url
                            = "jdbc:mysql://localhost:3306/jmqtt?characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false"
            + "&maxReconnects=10&useSSL=false";
    private String username = "root";
    private String password = "CallmeZ2013";

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

}
