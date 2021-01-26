
package org.jmqtt.broker.store;

import org.jmqtt.broker.common.model.Message;

import java.util.Collection;

/**
 * 设备消息处理：
 * will消息（遗嘱消息）
 * retain消息（保留消息）
 * TODO 待实现
 */
public interface MessageStore {

    /**
     * 存储clientId的遗嘱消息
     */
    void storeWillMessage(String clientId, Message message);

    /**
     * 清理该clientId的遗嘱消息
     */
    void clearWillMessage(String clientId);

    /**
     * 获取will消息
     */
    Message getWillMessage(String clientId);

    /**
     * 存储retain消息
     */
    void storeRetainMessage(String topic,Message message);

    /**
     * 清理该topic的 retain消息
     */
    void clearRetaionMessage(String topic);

    /**
     * 获取所有retain消息
     */
    Collection<Message> getAllRetainMsg();
}
