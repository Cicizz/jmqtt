package org.jmqtt.store.luffydb;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.log.LoggerName;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuffyStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private StoreConfig storeConfig;

    public LuffyStore(StoreConfig storeConfig){
        this.storeConfig = storeConfig;
    }

    private void init(){

    }

}
