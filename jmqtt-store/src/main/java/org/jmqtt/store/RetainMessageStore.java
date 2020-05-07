package org.jmqtt.store;


import org.jmqtt.common.model.Message;

import java.util.Collection;

public interface RetainMessageStore {

    /**
     * 获取所有的retain消息
     * @return
     */
    Collection<Message> getAllRetainMessage();

    /**
     * 存储retain消息
     * @param topic
     * @param message
     */
    void storeRetainMessage(String topic,Message message);

    /**
     * 移除retain消息
     * @param topic
     */
    void removeRetainMessage(String topic);
}
