package org.jmqtt.store.rocksdb.db;

public interface RDBStorePrefix {

    String SESSION = "session:";

    String REC_FLOW_MESSAGE = "recFlowMessage:";

    String SEND_FLOW_MESSAGE = "sendFlowMessage:";

    String OFFLINE_MESSAGE = "offlineMessage:";

    String RETAIN_MESSAGE = "retainMessage:";

    String SUBSCRIPTION = "subscription:";

    String WILL_MESSAGE = "willMessage:";
}
