package org.jmqtt.store.rocksdb;

import org.jmqtt.common.model.Message;
import org.jmqtt.common.helper.SerializeHelper;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.RetainMessageStore;
import org.jmqtt.store.rocksdb.db.RDB;
import org.jmqtt.store.rocksdb.db.RDBStorePrefix;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class RDBRetainMessageStore implements RetainMessageStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE);
    private RDB rdb;

    public RDBRetainMessageStore(RDB rdb){
        this.rdb = rdb;
    }

    @Override
    public Collection<Message> getAllRetainMessage() {
        Collection<byte[]> values = this.rdb.getByPrefix(columnFamilyHandle(),key(""));
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
        this.rdb.putSync(columnFamilyHandle(),key(topic),SerializeHelper.serialize(message));
    }

    @Override
    public void removeRetainMessage(String topic) {
        this.rdb.delete(columnFamilyHandle(),key(topic));
    }

    private byte[] key(String topic){
        return (RDBStorePrefix.RETAIN_MESSAGE + topic).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(RDBStorePrefix.RETAIN_MESSAGE);
    }
}
