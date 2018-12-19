package org.jmqtt.common.config;

public class RedisConfig {
    private String host1 = "127.0.0.1";
    private Integer port1 = 6379;
    private String host2 = "127.0.0.1";
    private Integer port2 = 6379;
    private String host3 = "127.0.0.1";
    private Integer port3 = 6379;
    private String host4 = "127.0.0.1";
    private Integer port4 = 6379;
    private String password = "123456";
    private Integer maxIdle = 100;
    private Integer maxActive = 300;
    private Integer maxWait = 1000;
    private Integer timeout = 100000;

    public String getHost1() {
        return host1;
    }

    public void setHost1(String host1) {
        this.host1 = host1;
    }

    public Integer getPort1() {
        return port1;
    }

    public String getHost2() {
        return host2;
    }

    public void setHost2(String host2) {
        this.host2 = host2;
    }

    public Integer getPort2() {
        return port2;
    }

    public void setPort2(Integer port2) {
        this.port2 = port2;
    }

    public String getHost3() {
        return host3;
    }

    public void setHost3(String host3) {
        this.host3 = host3;
    }

    public Integer getPort3() {
        return port3;
    }

    public void setPort3(Integer port3) {
        this.port3 = port3;
    }

    public String getHost4() {
        return host4;
    }

    public void setHost4(String host4) {
        this.host4 = host4;
    }

    public Integer getPort4() {
        return port4;
    }

    public void setPort4(Integer port4) {
        this.port4 = port4;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setPort1(Integer port1) {
        this.port1 = port1;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

}
