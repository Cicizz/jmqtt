package org.jmqtt.store.rocksdb;

import org.jmqtt.common.model.Message;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.WillMessageStore;
import org.jmqtt.store.rocksdb.db.RDB;
import org.jmqtt.store.rocksdb.db.RDBStorePrefix;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class RDBWillMessageStore implements WillMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);

    private RDB rdb;

    public RDBWillMessageStore(RDB rdb){
        this.rdb = rdb;
    }

    @Override
    public Message getWillMessage(String clientId) {
        byte[] value = this.rdb.get(columnFamilyHandle(),key(clientId));
        if(value == null){
            log.warn("The will message is not exist,clientId = {}",clientId);
            return null;
        }
        return SerializeHelper.deserialize(value,Message.class);
    }

    @Override
    public boolean hasWillMessage(String clientId) {
        return this.rdb.get(columnFamilyHandle(),key(clientId)) != null;
    }

    @Override
    public void storeWillMessage(String clientId, Message message) {
        this.rdb.putSync(columnFamilyHandle(),key(clientId),SerializeHelper.serialize(message));
    }

    @Override
    public Message removeWillMessage(String clientId) {
        byte[] key = key(clientId);
        byte[] value = this.rdb.get(columnFamilyHandle(),key);
        if(value == null){
            log.warn("The will message is not exist,cause = {}",clientId);
            return null;
        }
        this.rdb.delete(columnFamilyHandle(),key);
        return SerializeHelper.deserialize(value,Message.class);
    }

    private byte[] key(String clientId){
        return (RDBStorePrefix.WILL_MESSAGE + clientId).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(RDBStorePrefix.WILL_MESSAGE);
    }
}
