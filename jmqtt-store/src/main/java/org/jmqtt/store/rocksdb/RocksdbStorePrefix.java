package org.jmqtt.store.rocksdb;

public interface RocksdbStorePrefix {

    String SESSION = "session:";

    String FLOW_MESSAGE = "flowMessage:";

    String OFFLINE_MESSAGE = "offlineMessage:";

    String RETAIN_MESSAGE = "retainMessage:";

    String SUBSCRIPTION = "subscription:";

    String WILL_MESSAGE = "willMessage:";
}
