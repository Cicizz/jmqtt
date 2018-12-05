package org.jmqtt.store.memory;

import org.jmqtt.common.bean.Message;
import org.jmqtt.store.OfflineMessageStore;

import java.util.concurrent.BlockingQueue;

public class DefaultOfflineMessageStore implements OfflineMessageStore {
    @Override
    public void initOfflneMsgCache(String clientId, int size) {

    }

    @Override
    public void clearOfflineMsgCache(String clientId, int size) {

    }

    @Override
    public boolean addOfflineMessage(String clientId, Message message) {
        return false;
    }

    @Override
    public BlockingQueue<Message> getAllOfflineMessage(String clientId) {
        return null;
    }
}
