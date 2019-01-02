package org.jmqtt.store.rocksdb;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private StoreConfig storeConfig;

    public RocksdbStore(StoreConfig storeConfig){
        this.storeConfig = storeConfig;
    }

    private void init(){

    }

}
