package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.RetainMessageStore;

import java.util.Collection;

public class RedisRetainMessageStore implements RetainMessageStore {
    private RedisStoreUtil retainTable;

    public RedisRetainMessageStore(RedisConfig redisConfig){
        this.retainTable = new RedisStoreUtil(redisConfig,"retainTable");
    }

    @Override
    public Collection<Message> getAllRetainMessage() { return retainTable.sgetAllMsg(); }

    @Override
    public void storeRetainMessage(String topic, Message message) {
        retainTable.sstoreMsg(topic,message);
    }

    @Override
    public void removeRetainMessage(String topic) {
        retainTable.delete(topic);
    }
}
