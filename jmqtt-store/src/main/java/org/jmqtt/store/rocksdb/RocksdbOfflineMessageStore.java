package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.OfflineMessageStore;
import org.jmqtt.store.rocksdb.utils.RocksList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class RocksdbOfflineMessageStore implements OfflineMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);
    private RocksList rocksList;

    public RocksdbOfflineMessageStore(RocksdbDao rocksdbDao){
        this.rocksList  = rocksdbDao.getRocksList();
    }


    @Override
    public void clearOfflineMsgCache(String clientId) {
        this.rocksList.clear(RocksdbStorePrefix.OFFLINE_MESSAGE+clientId);
    }

    @Override
    public boolean containOfflineMsg(String clientId) {
        return this.rocksList.contains(RocksdbStorePrefix.OFFLINE_MESSAGE + clientId);
    }

    @Override
    public boolean addOfflineMessage(String clientId, Message message) {
        try{
            this.rocksList.add(RocksdbStorePrefix.OFFLINE_MESSAGE+clientId, SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Add Offline message failure,cause={}",ex);
        }
        return false;
    }

    @Override
    public Collection<Message> getAllOfflineMessage(String clientId) {
        Collection<byte[]> values = this.rocksList.values(RocksdbStorePrefix.OFFLINE_MESSAGE+clientId);
        Collection<Message> offlineMessages = new ArrayList<>(values.size());
        for(byte[] value : values){
            offlineMessages.add(SerializeHelper.deserialize(value,Message.class));
        }
        return offlineMessages;
    }
}
