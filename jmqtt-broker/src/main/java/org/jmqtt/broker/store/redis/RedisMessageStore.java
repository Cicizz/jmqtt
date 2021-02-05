package org.jmqtt.broker.store.redis;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.MessageStore;

import java.util.Collection;

public class RedisMessageStore implements MessageStore {

    private RedisTemplate redisTemplate;
    @Override
    public void start(BrokerConfig brokerConfig) {
        this.redisTemplate = new RedisTemplate(brokerConfig);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean storeWillMessage(String clientId, Message message) {

        return false;
    }

    @Override
    public boolean clearWillMessage(String clientId) {

        return false;
    }

    @Override
    public Message getWillMessage(String clientId) {
        return null;
    }

    @Override
    public boolean storeRetainMessage(String topic, Message message) {

        return false;
    }

    @Override
    public boolean clearRetaionMessage(String topic) {

        return false;
    }

    @Override
    public Collection<Message> getAllRetainMsg() {
        return null;
    }
}
