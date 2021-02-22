package org.jmqtt.broker.store.mem;

import org.jmqtt.broker.common.config.BrokerConfig;
import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.store.MessageStore;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MemMessageStore extends AbstractMemStore implements MessageStore {
    /**
     * 遗嘱消息
     * **/
    private Map<String /*clientId*/, Message> willTable = new ConcurrentHashMap<>();
    /**
     * 保持消息
     */
    private Map<String/*Topic*/, Message> retainTable = new ConcurrentHashMap<>();

    @Override
    public void start(BrokerConfig brokerConfig) {
        super.start(brokerConfig);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public boolean storeWillMessage(String clientId, Message message) {
        willTable.put(clientId,message);
        return true;
    }

    @Override
    public boolean clearWillMessage(String clientId) {
        willTable.remove(clientId);
        return true;
    }

    @Override
    public Message getWillMessage(String clientId) {
        return willTable.get(clientId);
    }

    @Override
    public boolean storeRetainMessage(String topic, Message message) {
        retainTable.put(topic, message);
        return true;
    }

    @Override
    public boolean clearRetainMessage(String topic) {
        retainTable.remove(topic);
        return true;
    }

    @Override
    public Collection<Message> getAllRetainMsg() {
        return retainTable.values();
    }
}
