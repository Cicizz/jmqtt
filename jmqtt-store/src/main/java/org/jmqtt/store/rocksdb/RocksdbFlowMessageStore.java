package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.FlowMessageStore;
import org.jmqtt.store.rocksdb.utils.RocksHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RocksdbFlowMessageStore implements FlowMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksHash rocksHash;

    public RocksdbFlowMessageStore(RocksHash rocksHash){
        this.rocksHash = rocksHash;
    }

    @Override
    public void clearClientFlowCache(String clientId) {
        this.rocksHash.clear(RocksdbStorePrefix.FLOW_MESSAGE + clientId);
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        return null;
    }

    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) {
        return null;
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        return false;
    }

    @Override
    public Collection<Message> getAllSendMsg(String clientId) {
        return null;
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        return false;
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return false;
    }
}
