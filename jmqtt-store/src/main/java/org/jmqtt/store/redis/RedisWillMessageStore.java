package org.jmqtt.store.redis;

import org.jmqtt.common.bean.Message;
import org.jmqtt.common.config.RedisConfig;
import org.jmqtt.store.WillMessageStore;

public class RedisWillMessageStore implements WillMessageStore {
    private RedisStoreUtil willTable;

    public RedisWillMessageStore(RedisConfig redisConfig){
        this.willTable = new RedisStoreUtil(redisConfig,"willTable:");
    }

    @Override
    public Message getWillMessage(String clientId) {
        return willTable.<Message>sgetMsg(clientId,Message.class);
    }

    @Override
    public boolean hasWillMessage(String clientId) {
        return willTable.scontain(clientId);
    }

    @Override
    public void storeWillMessage(String clientId, Message message) {
        willTable.sstoreMsg(clientId,message);
    }

    @Override
    public Message removeWillMessage(String clientId) {
        Message message = willTable.<Message>sgetMsg(clientId,Message.class);
        willTable.delete(clientId);
        return message;
    }
}
