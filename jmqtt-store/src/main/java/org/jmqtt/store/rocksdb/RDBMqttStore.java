package org.jmqtt.store.rocksdb;

import org.jmqtt.common.config.StoreConfig;
import org.jmqtt.store.*;
import org.jmqtt.store.rocksdb.db.RDB;

public class RDBMqttStore extends AbstractMqttStore {

    private StoreConfig storeConfig;

    private RDB rdb;


    public RDBMqttStore(StoreConfig storeConfig){
        this.storeConfig = storeConfig;
        this.rdb = new RDB(storeConfig);
    }

    @Override
    public void init() {
        this.rdb.init();
        this.flowMessageStore = new RDBFlowMessageStore(rdb);
        this.willMessageStore = new RDBWillMessageStore(rdb);
        this.retainMessageStore = new RDBRetainMessageStore(rdb);
        this.offlineMessageStore = new RDBOfflineMessageStore(rdb);
        this.subscriptionStore = new RDBSubscriptionStore(rdb);
        this.sessionStore = new RDBSessionStore(rdb);
    }

    @Override
    public void shutdown() {
        this.rdb.close();
    }


}
