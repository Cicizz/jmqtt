package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.WillMessageStore;
import org.jmqtt.store.rocksdb.utils.RocksMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksWillMessageStore implements WillMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RocksMap rocksMap;

    public RocksWillMessageStore(RocksdbDao rocksdbDao){
        this.rocksMap = rocksdbDao.getRocksMap();
    }

    @Override
    public Message getWillMessage(String clientId) {
        byte[] value = this.rocksMap.get(RocksdbStorePrefix.WILL_MESSAGE+clientId);
        if(value == null){
            log.warn("The will message is not exist,clientId = {}",clientId);
            return null;
        }
        return SerializeHelper.deserialize(value,Message.class);
    }

    @Override
    public boolean hasWillMessage(String clientId) {
        return this.rocksMap.contains(RocksdbStorePrefix.WILL_MESSAGE + clientId);
    }

    @Override
    public void storeWillMessage(String clientId, Message message) {
        this.rocksMap.set(RocksdbStorePrefix.WILL_MESSAGE,SerializeHelper.serialize(message));
    }

    @Override
    public Message removeWillMessage(String clientId) {
        byte[] value = this.rocksMap.get(RocksdbStorePrefix.WILL_MESSAGE+clientId);
        if(value == null){
            log.warn("The will message is not exist,cause = {}",clientId);
            return null;
        }
        return SerializeHelper.deserialize(value,Message.class);
    }
}
