package org.jmqtt.store.memory;

import org.jmqtt.common.model.Message;
import org.jmqtt.store.WillMessageStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultWillMessageStore implements WillMessageStore {

    private final Map<String /*clientId*/,Message> willTable = new ConcurrentHashMap<>();

    public DefaultWillMessageStore(){ }

    @Override
    public Message getWillMessage(String clientId) {
        return this.willTable.get(clientId);
    }

    @Override
    public boolean hasWillMessage(String clientId) {
        return this.willTable.containsKey(clientId);
    }

    @Override
    public void storeWillMessage(String clientId, Message message) {
        this.willTable.put(clientId,message);
    }

    @Override
    public Message removeWillMessage(String clientId) {
        return this.willTable.remove(clientId);
    }
}
