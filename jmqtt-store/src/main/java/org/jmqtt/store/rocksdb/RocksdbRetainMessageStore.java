package org.jmqtt.store.rocksdb;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.RetainMessageStore;
import org.jmqtt.store.rocksdb.utils.RocksMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class RocksdbRetainMessageStore implements RetainMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);
    private RocksMap rocksMap;

    public RocksdbRetainMessageStore(RocksdbDao rocksdbDao){
        this.rocksMap = rocksdbDao.getRocksMap();
    }

    @Override
    public Collection<Message> getAllRetainMessage() {
        Collection<byte[]> values = this.rocksMap.values(RocksdbStorePrefix.RETAIN_MESSAGE);
        Collection<Message> retainMessages = new ArrayList<>();
        for(byte[] value : values){
            Message retainMsg = SerializeHelper.deserialize(value,Message.class);
            if(Objects.nonNull(retainMsg)){
                retainMessages.add(retainMsg);
            }
        }
        return retainMessages;
    }

    @Override
    public void storeRetainMessage(String topic, Message message) {
        this.rocksMap.set(RocksdbStorePrefix.RETAIN_MESSAGE+topic,SerializeHelper.serialize(message));
    }

    @Override
    public void removeRetainMessage(String topic) {
        this.rocksMap.remove(RocksdbStorePrefix.RETAIN_MESSAGE + topic);
    }
}
