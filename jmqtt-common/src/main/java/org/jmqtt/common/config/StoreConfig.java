package org.jmqtt.common.config;

import java.util.ArrayList;

public class StoreConfig {

    /**
     * store type default 1:rocksdb  2.memory  3.redis
     */
    private int storeType = 1;


    /* rocksdb store configuration start */
    private String rocksDbPath = "rocksdb.db";

    /* rocksdb store configuration end */

    public String getRocksDbPath() {
        return rocksDbPath;
    }

    public void setRocksDbPath(String rocksDbPath) {
        this.rocksDbPath = rocksDbPath;
    }

    public int getStoreType() {
        return storeType;
    }

    public void setStoreType(int storeType) {
        this.storeType = storeType;
    }

    /*redis store configuration start */
    private String nodes;
    private String password;
    private Integer maxIdle = 100;
    private Integer maxActive = 600;
    private Integer timeout = 100000;



    /*redis store configuration end */

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public String getPassword() {
        return password;
    }

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

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
