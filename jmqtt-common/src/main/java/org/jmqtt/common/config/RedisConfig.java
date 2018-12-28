package org.jmqtt.common.config;

import java.util.ArrayList;
import java.util.Set;

public class RedisConfig {
    private ArrayList<String> hosts = new ArrayList<>();
    private ArrayList<Integer> ports = new ArrayList<>();
    private String password = "123456";
    private Integer maxIdle = 100;
    private Integer maxActive = 600;
    private Integer maxWait = 1000;
    private Integer timeout = 100000;

    public void setHosts(String host){ hosts.add(host); }

    public String getHost(Integer hostId){ return hosts.get(hostId); }

    public Integer getPort(Integer portId) { return ports.get(portId); }

    public void setPorts(Integer port) { ports.add(port); }

    public void setPassword(String password) {
        this.password = password;
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
