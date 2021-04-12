package org.jmqtt.broker.subscribe;

import org.jmqtt.broker.common.model.Message;
import org.jmqtt.broker.common.model.Subscription;

/**
 * 共享订阅事件/消息监听器
 */
public interface GroupSubscriptionAndMessageListener {

    void subscribe(Subscription subscription);

    void unSubscribe(String topic, String clientId);

    void receiveNewMessage(Message message);


}
