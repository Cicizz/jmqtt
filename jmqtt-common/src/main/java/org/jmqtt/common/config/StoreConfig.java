package org.jmqtt.common.config;

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
}
