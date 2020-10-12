package org.jmqtt.common.config;

/**
 * cluster group config
 */
public class ClusterConfig {

    /**
     * 其实就是一个集群开关
     * 模式:single 单机模式;cluster 集群模式
     */
    private String mode = "single";
    /**
     * 集群组件名称
     */
    private String clusterComponentName = "local";
    private String clusterMember;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getClusterComponentName() {
        return clusterComponentName;
    }

    public void setClusterComponentName(String clusterComponentName) {
        this.clusterComponentName = clusterComponentName;
    }

    public String getClusterMember() {
        return clusterMember;
    }

    public void setClusterMember(String clusterMember) {
        this.clusterMember = clusterMember;
    }
}
