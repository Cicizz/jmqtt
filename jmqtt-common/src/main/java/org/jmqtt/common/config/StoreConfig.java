package org.jmqtt.common.config;

import java.io.File;
import java.util.ArrayList;

public class StoreConfig {


    /**
     * store type default 1:rocksdb  2.redis  3.in memory
     */
    private int storeType = 3;


    /* rocksdb store configuration start */
    private String rocksDbPath = System.getProperty("user.home",System.getenv("user.home"))+ File.separator + "rocksdb";
    private int maxBackgroundFlushes = 10;
    private int maxBackgroundCompactions = 10;
    private int maxOpenFiles = 2048;
    private int maxSubcompactions = 10;
    private int baseBackGroundCompactions = 10;
    private int useFixedLengthPrefixExtractor = 10;
    private int writeBufferSize = 128;
    private int maxWriteBufferNumber = 10;
    private int level0SlowdownWritesTrigger = 30;
    private int level0StopWritesTrigger = 50;
    private int maxBytesForLevelBase = 512;
    private int targetFileSizeBase = 128;
    private int delayedWriteRate = 64;
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

    public int getMaxBackgroundFlushes() {
        return maxBackgroundFlushes;
    }

    public void setMaxBackgroundFlushes(int maxBackgroundFlushes) {
        this.maxBackgroundFlushes = maxBackgroundFlushes;
    }

    public int getMaxBackgroundCompactions() {
        return maxBackgroundCompactions;
    }

    public void setMaxBackgroundCompactions(int maxBackgroundCompactions) {
        this.maxBackgroundCompactions = maxBackgroundCompactions;
    }

    public int getMaxOpenFiles() {
        return maxOpenFiles;
    }

    public void setMaxOpenFiles(int maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
    }

    public int getMaxSubcompactions() {
        return maxSubcompactions;
    }

    public void setMaxSubcompactions(int maxSubcompactions) {
        this.maxSubcompactions = maxSubcompactions;
    }

    public int getBaseBackGroundCompactions() {
        return baseBackGroundCompactions;
    }

    public void setBaseBackGroundCompactions(int baseBackGroundCompactions) {
        this.baseBackGroundCompactions = baseBackGroundCompactions;
    }

    public int getUseFixedLengthPrefixExtractor() {
        return useFixedLengthPrefixExtractor;
    }

    public void setUseFixedLengthPrefixExtractor(int useFixedLengthPrefixExtractor) {
        this.useFixedLengthPrefixExtractor = useFixedLengthPrefixExtractor;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public int getMaxWriteBufferNumber() {
        return maxWriteBufferNumber;
    }

    public void setMaxWriteBufferNumber(int maxWriteBufferNumber) {
        this.maxWriteBufferNumber = maxWriteBufferNumber;
    }

    public int getLevel0SlowdownWritesTrigger() {
        return level0SlowdownWritesTrigger;
    }

    public void setLevel0SlowdownWritesTrigger(int level0SlowdownWritesTrigger) {
        this.level0SlowdownWritesTrigger = level0SlowdownWritesTrigger;
    }

    public int getLevel0StopWritesTrigger() {
        return level0StopWritesTrigger;
    }

    public void setLevel0StopWritesTrigger(int level0StopWritesTrigger) {
        this.level0StopWritesTrigger = level0StopWritesTrigger;
    }

    public int getMaxBytesForLevelBase() {
        return maxBytesForLevelBase;
    }

    public void setMaxBytesForLevelBase(int maxBytesForLevelBase) {
        this.maxBytesForLevelBase = maxBytesForLevelBase;
    }

    public int getTargetFileSizeBase() {
        return targetFileSizeBase;
    }

    public void setTargetFileSizeBase(int targetFileSizeBase) {
        this.targetFileSizeBase = targetFileSizeBase;
    }

    public int getDelayedWriteRate() {
        return delayedWriteRate;
    }

    public void setDelayedWriteRate(int delayedWriteRate) {
        this.delayedWriteRate = delayedWriteRate;
    }
}
