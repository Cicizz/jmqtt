package org.jmqtt.common.config;

public class StoreConfig {

    /* rocksdb store configuration start */
    private String rocksDbPath = "rocksdb.db";

    /* rocksdb store configuration end */

    public String getRocksDbPath() {
        return rocksDbPath;
    }

    public void setRocksDbPath(String rocksDbPath) {
        this.rocksDbPath = rocksDbPath;
    }
}
