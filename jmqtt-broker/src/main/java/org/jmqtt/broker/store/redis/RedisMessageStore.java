package org.jmqtt.broker.store.redis;

import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.MessageStore;

import java.util.Collection;

public class RedisMessageStore implements MessageStore {
    @Override
    public void storeWillMessage(String clientId, Message message) {

    }

    @Override
    public void clearWillMessage(String clientId) {

    }

    @Override
    public Message getWillMessage(String clientId) {
        return null;
    }

    @Override
    public void storeRetainMessage(String topic, Message message) {

    }

    @Override
    public void clearRetaionMessage(String topic) {

    }

    @Override
    public Collection<Message> getAllRetainMsg() {
        return null;
    }
}
