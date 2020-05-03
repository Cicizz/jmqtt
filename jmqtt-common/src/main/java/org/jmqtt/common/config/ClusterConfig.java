package org.jmqtt.common.config;

/**
 * cluster group config
 */
public class ClusterConfig {

    private String redisIp;
    private int maxWaitMills = 60*1000;
    private boolean testOnBorrow = true;
    private int minIdle = 20;
    private int maxTotal = 200;
    private int maxIdle = 50;

    public String getRedisIp() {
        return redisIp;
    }

    public void setRedisIp(String redisIp) {
        this.redisIp = redisIp;
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

    @Override
    public String toString() {
        return "ClusterConfig{" +
                "redisIp='" + redisIp + '\'' +
                ", maxWaitMills=" + maxWaitMills +
                ", testOnBorrow=" + testOnBorrow +
                ", minIdle=" + minIdle +
                ", maxTotal=" + maxTotal +
                ", maxIdle=" + maxIdle +
                '}';
    }
}
