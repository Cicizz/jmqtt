package org.jmqtt.store;

import org.jmqtt.common.bean.Message;

import java.util.concurrent.BlockingQueue;

/**
 * cleansession message
 */
public interface OfflineMessageStore {

    void initOfflneMsgCache(String clientId,int size);

    void clearOfflineMsgCache(String clientId,int size);

    boolean addOfflineMessage(String clientId, Message message);

    BlockingQueue<Message> getAllOfflineMessage(String clientId);

}
