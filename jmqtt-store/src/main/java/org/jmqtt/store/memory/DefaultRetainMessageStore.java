package org.jmqtt.store.memory;

import org.jmqtt.common.model.Message;
import org.jmqtt.store.RetainMessageStore;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRetainMessageStore implements RetainMessageStore {

    private Map<String/*Topic*/,Message> retainTable = new ConcurrentHashMap<>();

    @Override
    public Collection<Message> getAllRetainMessage() {
        return retainTable.values();
    }

    @Override
    public void storeRetainMessage(String topic, Message message) {
        this.retainTable.put(topic,message);
    }

    @Override
    public void removeRetainMessage(String topic) {
        this.retainTable.remove(topic);
    }
}
