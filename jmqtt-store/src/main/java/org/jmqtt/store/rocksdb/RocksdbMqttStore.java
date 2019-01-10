package org.jmqtt.store.rocksdb;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.store.*;

public class RocksdbMqttStore extends AbstractMqttStore {

    private StoreConfig storeConfig;
    private RocksdbDao rocksdbDao;


    public RocksdbMqttStore(StoreConfig storeConfig){
        this.storeConfig = storeConfig;
    }

    @Override
    public void init() throws Exception {
        this.rocksdbDao = new RocksdbDao(storeConfig);
        this.flowMessageStore = new RocksdbFlowMessageStore(rocksdbDao);
        this.willMessageStore = new RocksWillMessageStore(rocksdbDao);
        this.retainMessageStore = new RocksdbRetainMessageStore(rocksdbDao);
        this.offlineMessageStore = new RocksdbOfflineMessageStore(rocksdbDao);
        this.subscriptionStore = new RocksdbSubscriptionStore(rocksdbDao);
        this.sessionStore = new RocksdbSessionStore(rocksdbDao);
    }

    @Override
    public void close() {
        this.rocksdbDao.close();
    }


}
